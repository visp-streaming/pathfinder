package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.topologyParser.TopologyParser;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.entities.operator_statistics.OperatorStatisticsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    VispTopology vispTopology;

    public String cachedTopologyString = "";

    public String getCachedTopologyString() {
        return cachedTopologyString;
    }

    public void setCachedTopologyString(String cachedTopologyString) {
        this.cachedTopologyString = cachedTopologyString;
    }

    TopologyParser topologyParser = new TopologyParser();

    public VispTopology getVispTopology() {
        return vispTopology;
    }

    public synchronized void addVispRuntime(VispRuntimeIdentifier endpoint) {
        if (endpoint == null || "".equals(endpoint)) {
            LOG.error("Invalid endpoint");
        } else {
            vispRuntimeIdentifiers.add(endpoint);
//            String topology = getTopologyFromVisp(endpoint);
//            if(!this.getCachedTopologyString().equals(topology)) {
//                this.setCachedTopologyString(topology);
//                updateStoredTopology(topology);
//            }
//            LOG.debug("Added endpoint " + endpoint);
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

    public String getTopologyFromVisp(VispRuntimeIdentifier rt) {
        RestTemplate restTemplate = new RestTemplate();
        URI targetUrl = UriComponentsBuilder.fromUriString("http://" + rt)
                .path("/getTopologyFromVisp")
                .build()
                .toUri();
        String topology = restTemplate.getForObject(targetUrl, String.class);
        return topology;
    }

    public void updateStoredTopology(String newTopology) {
        LOG.debug("UPDATING stored VISP topology");
        vispTopology.setTopology(topologyParser.parseTopologyFromString(newTopology).topology);

        // TODO: actually react to the changed topology
    }

    public void getStatisticsFromVisp(VispRuntimeIdentifier rt) {
        RestTemplate restTemplate = new RestTemplate();
        URI targetUrl = UriComponentsBuilder.fromUriString("http://" + rt)
                .path("/getAllStatistics")
                .build()
                .toUri();
        OperatorStatisticsResponse allStatistics = restTemplate.getForObject(targetUrl, OperatorStatisticsResponse.class);
        LOG.debug("All statistics: ");
        LOG.debug(allStatistics.toString());
    }
}
