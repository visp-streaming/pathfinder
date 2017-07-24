package net.knasmueller.pathfinder.service;

import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This component is automatically executed on a pathFinder instance's construction
 * It performs initialization code that connects the instance to a previously set up pathfinder network
 */
@Component
public class Initializer {

    @Autowired
    VispCommunicator vispCommunicator;

    private static final Logger LOG = LoggerFactory.getLogger(Initializer.class);

    @PostConstruct
    public void init() {
        LOG.debug("Running initialization procedure");

        for (VispRuntimeIdentifier rt : vispCommunicator.getVispRuntimeIdentifiers(true)) {
            vispCommunicator.pingAndMaybeDeleteRuntime(rt);
        }

        vispCommunicator.setInitialized(true);

        LOG.info("Initialization complete");


    }

}