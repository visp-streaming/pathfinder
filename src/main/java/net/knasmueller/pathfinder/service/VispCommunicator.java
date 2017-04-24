package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.common.operators.Join;
import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.common.operators.ProcessingOperator;
import ac.at.tuwien.infosys.visp.topologyParser.TopologyParser;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.entities.operator_statistics.OperatorStatisticsResponse;
import net.knasmueller.pathfinder.entities.operator_statistics.SingleOperatorStatistics;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.repository.SingleOperatorStatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

/**
 * This class contains all VISP-specific communication
 */
@Service
public class VispCommunicator {
    private static final Logger LOG = LoggerFactory.getLogger(VispCommunicator.class);

    List<VispRuntimeIdentifier> vispRuntimeIdentifiers = new ArrayList<>();

    @Autowired
    VispTopology vispTopology;

    @Autowired
    private ProcessingOperatorManagement processingOperatorManagement;

    @Autowired
    SingleOperatorStatisticsRepository singleOperatorStatisticsRepository;

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
                .path("/getTopology")
                .build()
                .toUri();
        String topology = restTemplate.getForObject(targetUrl, String.class);
        return topology;
    }

    public void updateStoredTopology(String newTopology) {
        LOG.debug("UPDATING stored VISP topology");
        Map<String, Operator> topology = topologyParser.parseTopologyFromString(newTopology).topology;
        vispTopology.setTopology(topology);
        processingOperatorManagement.topologyUpdate(topology);
    }

    public void clearStoredTopology() {
        vispTopology.setTopology(null);
        processingOperatorManagement.topologyUpdate(null);
    }

    public OperatorStatisticsResponse getStatisticsFromVisp(VispRuntimeIdentifier rt) {
        RestTemplate restTemplate = new RestTemplate();
        URI targetUrl = UriComponentsBuilder.fromUriString("http://" + rt)
                .path("/getAllStatistics")
                .build()
                .toUri();
        OperatorStatisticsResponse allStatistics;
        allStatistics = restTemplate.getForObject(targetUrl, OperatorStatisticsResponse.class);
        LOG.debug("For step1:");
        LOG.debug(allStatistics.get("step1").toString());

        return allStatistics;
    }

    public void persistStatisticEntries(Map<String, SingleOperatorStatistics> allStatistics) {
        for(Map.Entry<String, SingleOperatorStatistics> e : allStatistics.entrySet()) {
            SingleOperatorStatistics s = e.getValue();
            s.setOperatorName(e.getKey());
            singleOperatorStatisticsRepository.save(s);
        }
    }

    public Set<String> getAffectedSplitOperators(String operatorId) throws EmptyTopologyException {
        /** returns the IDs of the affected split operators for a given processing operator.
         * A split operator is affected if the processing operator's failure would cause the
         * split operator to switch to a different fallback path
         */

        // ASSUMPTION (for now): no nested split/join (this would get quite complicated)

        if(getVispTopology() == null || getVispTopology().getTopology() == null || getVispTopology().getTopology().isEmpty()) {
            throw new EmptyTopologyException("Could not return list of affected operators due to empty topology");
        }

        Set<String> affectedSplitOperators = new HashSet<>();
        VispTopology topology = getVispTopology();

        Operator op = topology.getTopology().get(operatorId);

        Set<String> allSplitOperators = ProcessingOperatorManagement.getAlternativePaths(topology.getTopology()).keySet();

        LOG.info("All split operators: " + String.join(", ", allSplitOperators));

        for(String split : allSplitOperators) {
            String currentOp = split;
            Queue<String> q = new LinkedList<>();
            q.add(currentOp);
            while(!q.isEmpty()) {
                currentOp = q.remove();
                LOG.info("Handling " + currentOp);
                if(currentOp.equals(operatorId)) {
                    if(!affectedSplitOperators.contains(split)) {
                        LOG.info("Adding operator to result list: " + split);
                        affectedSplitOperators.add(split);
                    }
                }
                Set<String> downstreamOperators = ProcessingOperatorManagement.getDownstreamOperators(getVispTopology().getTopology(), currentOp);
                LOG.info("Downstream operators: " + String.join(", ", downstreamOperators));
                if(downstreamOperators.isEmpty()) {
                    continue;
                } else {
                    for(String downstreamOp : downstreamOperators) {
                        if(!(topology.getTopology().get(downstreamOp) instanceof Join)) {
                            LOG.info("Adding to queue: " + downstreamOp);
                            q.add(downstreamOp);
                        }
                    }
                }
            }
        }


        return affectedSplitOperators;
    }

}
