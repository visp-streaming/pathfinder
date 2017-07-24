package net.knasmueller.pathfinder.service;

import net.knasmueller.pathfinder.domain.ICircuitBreakerStatusProvider;
import net.knasmueller.pathfinder.domain.IDataFlowProvider;
import net.knasmueller.pathfinder.domain.IMessageFlowSwitcher;
import net.knasmueller.pathfinder.domain.impl.CircuitBreakerStatusProvider;
import net.knasmueller.pathfinder.domain.impl.DataFlowProvider;
import net.knasmueller.pathfinder.domain.impl.VispMessageFlowSwitcher;
import net.knasmueller.pathfinder.entities.TopologyStability;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.entities.operator_statistics.OperatorStatisticsResponse;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.exceptions.VispRuntimeUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Scheduling component that automates several tasks
 * Main functionality: querying VISP runtimes for up-to-date statistics and topologies
 */
@Component
public class Scheduler {
    private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);
    @Autowired
    VispCommunicator vispCommunicator;

    @Autowired
    ProcessingOperatorHealth poh;

    @Autowired
    @Lazy // TODO: rethink design, remove circular dependency
            SplitDecisionService sds;

    /**
     * Queries all currently known VISP runtimes and asks for up-to-date statistics
     * Statistics are then persisted to a database
     */
    @Scheduled(fixedDelay = 15000)
    public void getStatisticsFromAllRuntimes() {
        maybePullTopologyUpdate();

        List<VispRuntimeIdentifier> currentlyKnownVispRuntimeIdentifiers = vispCommunicator.getVispRuntimeIdentifiers();
        LOG.info("Size: " + currentlyKnownVispRuntimeIdentifiers.size());
        if (currentlyKnownVispRuntimeIdentifiers.size() != 0) {
            for (VispRuntimeIdentifier rt : currentlyKnownVispRuntimeIdentifiers) {
                LOG.info("Querying VISP runtime " + rt);
                OperatorStatisticsResponse response = vispCommunicator.getStatisticsFromVisp(rt);
                vispCommunicator.persistStatisticEntries(response);
            }
            poh.postStatisticsUpdate();
        } else {
            LOG.debug("No VISP instances to query");
        }
    }

    /**
     * Compute topology stability metrics and write to database
     */
    @Scheduled(fixedDelay = 15000)
    public void updateTopologyStability() {
        try {
            sds.updateTopologyStability();
        } catch (EmptyTopologyException e) {
        }
    }


    /**
     * Contacts the first VISP runtime and checks whether the locally stored topology is still the same as the one
     * managed by VISP. If not, update the local one
     */
    public void maybePullTopologyUpdate() {
        if (vispCommunicator.getVispRuntimeIdentifiers().size() < 1) {
            LOG.debug("No known VISP instances - could not grab topology");
            return;
        }
        LOG.debug("maybePullTopologyUpdate()");
        // TODO: do not always contact the first one - either random or vote
        String topology = null;
        VispRuntimeIdentifier rt = vispCommunicator.getVispRuntimeIdentifiers().get(0);
        try {
            topology = vispCommunicator.getTopologyFromVisp(rt);
        } catch (VispRuntimeUnavailableException e) {
            vispCommunicator.vriRepo.deleteByIpAndPort(rt.getIp(), rt.getPort());
        }
        if (!vispCommunicator.getCachedTopologyString().equals(topology)) {
            LOG.debug("Updating topology");
            vispCommunicator.setCachedTopologyString(topology);
            vispCommunicator.updateStoredTopology(topology);

            // notify other services about topology change
            try {
                VispTopology top = vispCommunicator.getVispTopology();
                if (top != null && top.topology != null && poh != null) {
                    poh.topologyUpdate(top.topology);
                    sds.updateSplitOperatorsFromTopology();

                }
            } catch (EmptyTopologyException e) {
                LOG.warn("Received empty topology after update");
            }

        } else {
            LOG.debug("No topology update necessary");
        }
    }

    /**
     * Checks whether any operator availabilities have changed and initiate circuitBreaker transitions accordingly
     */
    @Scheduled(fixedDelay = 1000)
    public void updateCircuits() {
        try {
            sds.updateCircuits();

            if (sds.getCircuitBreakerMap() == null) {
                return;
            }
            // replace this dummy call once VISP has implemented this on its own
            List<String> paths = new ArrayList<>();
            for (String s : sds.getAlternativePaths().keySet()) {
                for (String o : sds.getAlternativePaths().get(s)) {
                    paths.add(o);
                }
            }
            Map<String, String> currentFlows = vispCommunicator.getCurrentMessageFlows(paths);

            IDataFlowProvider dataFlowProvider = new DataFlowProvider(currentFlows);
            ICircuitBreakerStatusProvider circuitBreakerStatusProvider = new CircuitBreakerStatusProvider(sds);
            IMessageFlowSwitcher messageFlowSwitcher = new VispMessageFlowSwitcher(vispCommunicator);
            sds.updateMessageFlowAfterCircuitBreakerUpdate(dataFlowProvider, circuitBreakerStatusProvider, messageFlowSwitcher);
        } catch (EmptyTopologyException e) {
        }
    }
}
