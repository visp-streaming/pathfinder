package net.knasmueller.pathfinder.service;

import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Scheduler {
    private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);
    @Autowired
    VispCommunicator vispCommunicator;

    @Scheduled(fixedRate = 30000)
    public void queryVispRuntimes() {
        checkForTopologyUpdate();

        List<VispRuntimeIdentifier> currentlyKnownVispRuntimeIdentifiers = vispCommunicator.getVispRuntimeIdentifiers();
        LOG.info("Size: " + currentlyKnownVispRuntimeIdentifiers.size());
        if (currentlyKnownVispRuntimeIdentifiers.size() != 0) {
            for (VispRuntimeIdentifier rt : currentlyKnownVispRuntimeIdentifiers) {
                LOG.info("Querying VISP runtime " + rt);
                vispCommunicator.getStatisticsFromVisp(rt);
            }
        } else {
            LOG.debug("No VISP instances to query");
        }
    }

    public void checkForTopologyUpdate() {
        if(vispCommunicator.getVispRuntimeIdentifiers().size() < 1) {
            LOG.debug("No known VISP instances - could not grab topology");
            return;
        }
        LOG.debug("checkForTopologyUpdate()");
        String topology = vispCommunicator.getTopologyFromVisp(vispCommunicator.getVispRuntimeIdentifiers().get(0));
        if(!vispCommunicator.getCachedTopologyString().equals(topology)) {
            LOG.debug("Updating topology");
            vispCommunicator.setCachedTopologyString(topology);
            vispCommunicator.updateStoredTopology(topology);
        } else {
            LOG.debug("No topology update necessary");
        }
    }

}