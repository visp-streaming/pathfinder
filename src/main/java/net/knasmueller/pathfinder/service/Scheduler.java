package net.knasmueller.pathfinder.service;

import net.knasmueller.pathfinder.entities.TopologyStability;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.entities.operator_statistics.OperatorStatisticsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduling component that automates several tasks
 * Main functionality: querying VISP runtimes for up-to-date statistics and topologies
 */
@Component
public class Scheduler {
    private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);
    @Autowired @Lazy // TODO: rethink design, remove circular dependency
    VispCommunicator vispCommunicator;

    @Autowired
    ProcessingOperatorHealth poh;

    @Autowired @Lazy // TODO: rethink design, remove circular dependency
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
        sds.updateTopologyStability();
    }


    /**
     * Contacts the first VISP runtime and checks whether the locally stored topology is still the same as the one
     * managed by VISP. If not, update the local one
     */
    public void maybePullTopologyUpdate() {
        if(vispCommunicator.getVispRuntimeIdentifiers().size() < 1) {
            LOG.debug("No known VISP instances - could not grab topology");
            return;
        }
        LOG.debug("maybePullTopologyUpdate()");
        // TODO: do not always contact the first one - either random or vote
        String topology = vispCommunicator.getTopologyFromVisp(vispCommunicator.getVispRuntimeIdentifiers().get(0));
        if(!vispCommunicator.getCachedTopologyString().equals(topology)) {
            LOG.debug("Updating topology");
            vispCommunicator.setCachedTopologyString(topology);
            vispCommunicator.updateStoredTopology(topology);
        } else {
            LOG.debug("No topology update necessary");
        }
    }

    /**
     * Checks whether any operator availabilities have changed and initiate circuitBreaker transitions accordingly
     */
    @Scheduled(fixedDelay = 1000)
    public void updateCircuits() {
        sds.updateCircuits();
    }
}
