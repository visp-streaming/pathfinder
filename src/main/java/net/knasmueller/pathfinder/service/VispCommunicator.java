package net.knasmueller.pathfinder.service;

import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all VISP-specific communication
 */
@Service
public class VispCommunicator {
    private static final Logger LOG = LoggerFactory.getLogger(VispCommunicator.class);

    List<VispRuntimeIdentifier> vispRuntimeIdentifiers = new ArrayList<>();

    public synchronized void addVispRuntime(VispRuntimeIdentifier endpoint) {
        if (endpoint == null || "".equals(endpoint)) {
            LOG.error("Invalid endpoint");
        } else {
            vispRuntimeIdentifiers.add(endpoint);
            getTopology(endpoint);
            LOG.debug("Added endpoint " + endpoint);
        }
    }

    public synchronized void removeVispRuntime(VispRuntimeIdentifier endpoint) {
        if (endpoint == null || "".equals(endpoint)) {
            LOG.error("Invalid endpoint");
        } else {
            vispRuntimeIdentifiers.remove(endpoint);
        }
        LOG.debug("Removed endpoint " + endpoint);
    }


    public List<VispRuntimeIdentifier> getVispRuntimeIdentifiers() {
        return vispRuntimeIdentifiers;
    }

    public String getTopology(VispRuntimeIdentifier rt) {
        RestTemplate restTemplate = new RestTemplate();
        URI targetUrl = UriComponentsBuilder.fromUriString("http://" + rt)
                .path("/getTopology")
                .build()
                .toUri();
        LOG.debug("targetUrl for getTopology call: " + targetUrl);
        String topology = restTemplate.getForObject(targetUrl, String.class);
        LOG.debug("Topology equals to: ");
        LOG.debug(topology);

        return topology;
    }
}
