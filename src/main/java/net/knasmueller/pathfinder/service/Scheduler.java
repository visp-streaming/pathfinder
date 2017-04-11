package net.knasmueller.pathfinder.service;

import net.knasmueller.pathfinder.entities.VispRuntime;
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
        LOG.info("Querying VISP Runtimes");
        List<VispRuntime> currentlyKnownVispRuntimes = vispCommunicator.getVispRuntimes();
        LOG.info("Size: " + currentlyKnownVispRuntimes.size());
        if (currentlyKnownVispRuntimes.size() != 0) {
            for (VispRuntime rt : currentlyKnownVispRuntimes) {
                LOG.info("Querying VISP runtime " + rt);
            }
        }
    }

}
