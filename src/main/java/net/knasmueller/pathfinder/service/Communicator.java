package net.knasmueller.pathfinder.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Communicator {
    private static final Logger LOG = LoggerFactory.getLogger(Communicator.class);

    List<String> siblingPathfinders = new ArrayList<>();

    public synchronized void addSibling(String endpoint) {
        if (endpoint == null || "".equals(endpoint)) {
            LOG.error("Invalid endpoint");
        } else {
            siblingPathfinders.add(endpoint);
            LOG.debug("Added endpoint " + endpoint);
        }
    }

    public synchronized void removeSibling(String endpoint) {
        if (endpoint == null || "".equals(endpoint)) {
            LOG.error("Invalid endpoint");
        } else {
            siblingPathfinders.remove(endpoint);
        }
        LOG.debug("Removed endpoint " + endpoint);
    }


    public List<String> getSiblings() {
        return siblingPathfinders;
    }
}
