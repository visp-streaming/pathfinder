package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.common.operators.Join;
import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.common.operators.Split;
import ac.at.tuwien.infosys.visp.topologyParser.TopologyParser;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.entities.operator_statistics.OperatorStatisticsResponse;
import net.knasmueller.pathfinder.entities.operator_statistics.SingleOperatorStatistics;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.repository.SingleOperatorStatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
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
    private ProcessingOperatorHealth processingOperatorHealth;

    @Autowired
    SingleOperatorStatisticsRepository singleOperatorStatisticsRepository;

    @Autowired
    SplitDecisionService sds;

    /**
     * The current VISP topology file as a string
     */
    public String cachedTopologyString = "";

    public String getCachedTopologyString() {
        return cachedTopologyString;
    }

    public void setCachedTopologyString(String cachedTopologyString) {
        this.cachedTopologyString = cachedTopologyString;
    }

    /**
     * Used to transform a topology file into a queriable hashmap
     */
    TopologyParser topologyParser = new TopologyParser();

    public VispTopology getVispTopology() {
        return vispTopology;
    }

    /**
     * Since there can be multiple VISP runtime instances in parallel, this method adds a new instance to the local
     * list of instances
     * @param endpoint
     */
    public synchronized void addVispRuntime(VispRuntimeIdentifier endpoint) {
        if (endpoint == null || "".equals(endpoint)) {
            LOG.error("Invalid endpoint");
        } else {
            vispRuntimeIdentifiers.add(endpoint);
            if(vispRuntimeIdentifiers.size() == 1) {
                // added the first runtime - fetch topology
//                String topology = getTopologyFromVisp(endpoint);
//                if(!this.getCachedTopologyString().equals(topology)) {
//                    this.setCachedTopologyString(topology);
//                    updateStoredTopology(topology);
//                }
            }
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

    /**
     * Sends a REST request to a specific VISP runtime instance and asks it to return the current topology as a string
     * @param rt
     * @return
     */
    public String getTopologyFromVisp(VispRuntimeIdentifier rt) {
        RestTemplate restTemplate = new RestTemplate();
        URI targetUrl = UriComponentsBuilder.fromUriString("http://" + rt)
                .path("/getTopology")
                .build()
                .toUri();
        String topology = restTemplate.getForObject(targetUrl, String.class);
        return topology;
    }

    /**
     * Updates the internally stored topology from a topology file string
     * @param newTopology
     */
    public void updateStoredTopology(String newTopology) {
        LOG.debug("UPDATING stored VISP topology");
        Map<String, Operator> topology = topologyParser.parseTopologyFromString(newTopology).topology;
        vispTopology.setTopology(topology);
        processingOperatorHealth.topologyUpdate(topology);
        sds.updateSplitOperatorsFromTopology();
    }

    public void clearStoredTopology() {
        vispTopology.setTopology(null);
        processingOperatorHealth.topologyUpdate(null);
    }

    /**
     * Queries a VISP runtime for the current statistics
     * @param rt the VISP runtime identifier that is queried
     * @return a LinkedHashMap from String to SingleOperatorStatistics where each operator has its own statistics object
     */
    public OperatorStatisticsResponse getStatisticsFromVisp(VispRuntimeIdentifier rt) {
        RestTemplate restTemplate = new RestTemplate();
        URI targetUrl = UriComponentsBuilder.fromUriString("http://" + rt)
                .path("/getAllStatistics") // TODO: do not query the whole statistics but only the subset for the that runtime
                .build()
                .toUri();
        OperatorStatisticsResponse allStatistics;
        allStatistics = restTemplate.getForObject(targetUrl, OperatorStatisticsResponse.class);
        LOG.debug("For step1:");
        LOG.debug(allStatistics.get("step1").toString());

        return allStatistics;
    }

    /**
     * Stores the statistics entries in a local database
     * Method is called automatically in the Scheduler (getStatisticsFromAllRuntimes())
     * @param allStatistics statistics map returned by getStatisticsFromVisp()
     */
    public void persistStatisticEntries(Map<String, SingleOperatorStatistics> allStatistics) {
        for(Map.Entry<String, SingleOperatorStatistics> e : allStatistics.entrySet()) {
            SingleOperatorStatistics s = e.getValue();
            s.setOperatorName(e.getKey());
            singleOperatorStatisticsRepository.save(s);
        }
    }

    /**
     * instructs all VISP instances to change the active output path of split operator `split` to `path`
     * @param splitPathPair list of changes; each item consists of a `split` `path` pair
     */
    public void switchSplitToPath(List<Pair<String, String>> splitPathPair) {
        for(Pair p : splitPathPair) {
            LOG.info("IMPLEMENT ME: sending message to VISP to switch " + p.getFirst()
                    + " to path " + p.getSecond());
        }
    }

    /**
     * Determines all split operators that are affected if that operator fails. A split operator is affected if
     * the operator's failure would cause the split operator to switch to a different fallback path
     * @param operatorId The failing operator
     * @return returns a set of (operator, firstChild) that includes all affected split operators
     * @throws EmptyTopologyException
     */
    public Set<Pair<String, String>> getAffectedSplitOperators(String operatorId) throws EmptyTopologyException {
        // ASSUMPTION (for now): no nested split/join (this would get quite complicated)
        //TODO: generalize for nested split/join

        if(getVispTopology() == null || getVispTopology().getTopology() == null || getVispTopology().getTopology().isEmpty()) {
            throw new EmptyTopologyException("Could not return list of affected operators due to empty topology");
        }

        Set<Pair<String, String>> affectedSplitOperators = new HashSet<>();
        VispTopology topology = getVispTopology();

        Operator op = topology.getTopology().get(operatorId);

        Set<String> allSplitOperators = SplitDecisionService.getAlternativePaths(topology.getTopology()).keySet();

        LOG.info("All split operators: " + String.join(", ", allSplitOperators));

        for(String split : allSplitOperators) {
            String currentOp = split;
            String firstChild = null;
            List<String> possibleChilds = ((Split) topology.getTopology().get(split)).getPathOrder();
            Queue<String> q = new LinkedList<>();
            q.add(currentOp);
            while(!q.isEmpty()) {
                currentOp = q.remove();
                if(possibleChilds.contains(currentOp)) {
                    firstChild = currentOp;
                }
                LOG.info("Handling " + currentOp);
                if(currentOp.equals(operatorId)) {
                    boolean operatorIsAlreadyContainedInReturnSet = false;
                    for(Pair<String, String> entry : affectedSplitOperators) {
                        if(entry.getFirst().equals(split)) {
                            operatorIsAlreadyContainedInReturnSet = true;
                        }
                    }
                    if(!operatorIsAlreadyContainedInReturnSet) {
                        LOG.info("Adding operator to result list: " + split);
                        affectedSplitOperators.add(Pair.of(split, firstChild));
                    }
                }
                Set<String> downstreamOperators = SplitDecisionService.getDownstreamOperators(getVispTopology().getTopology(), currentOp);
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
