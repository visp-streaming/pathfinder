package net.knasmueller.pathfinder.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class Communicator {
    private static final Logger LOG = LoggerFactory.getLogger(Communicator.class);

    @Value("#{'${pathfinder.runtime.ip:127.0.0.1}'}")
    private String pathfinderRuntimeIp;

    @Value("#{'${server.port:9000}'}")
    private String pathfinderRuntimePort;


    List<String> siblingPathfinders = new ArrayList<>();

    public synchronized void addSibling(String endpoint) {
        if (endpoint == null || "".equals(endpoint)) {
            LOG.error("Invalid endpoint");
        } else {
            if(!siblingPathfinders.contains(endpoint)) {
                siblingPathfinders.add(endpoint);
            } else {
                LOG.warn("Endpoint " + endpoint + " is already known and will not be added again");
            }
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


    public void propagateOperatorStatus(String operatorId, String status) {
        for(String pathfinder : siblingPathfinders) {
            if(pathfinder.equals(pathfinderRuntimeIp + ":" + pathfinderRuntimePort)) {
                LOG.debug("Skipping own pathfinder instance " + pathfinder);
                continue;
            }
            sendOperatorStatusUpdateToSibling(operatorId, status, pathfinder);
        }
    }

    public void sendOperatorStatusUpdateToSibling(String operatorId, String status, String pathfinderInstance) {
        RestTemplate restTemplate = new RestTemplate();
        URI targetUrl= UriComponentsBuilder.fromUriString("http://" + pathfinderInstance)
                .path("/operator/setOperatorStatus")
                .queryParam("operatorId", operatorId)
                .queryParam("status", status)
                .build()
                .toUri();
        LOG.debug("targetUrl: " + targetUrl);
        restTemplate.getForObject(targetUrl, String.class);
    }
}
