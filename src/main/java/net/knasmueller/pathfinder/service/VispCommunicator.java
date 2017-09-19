package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.common.operators.Join;
import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.common.operators.Split;
import ac.at.tuwien.infosys.visp.topologyParser.TopologyParser;
import net.knasmueller.pathfinder.entities.PathfinderOperator;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.entities.operator_statistics.OperatorStatisticsResponse;
import net.knasmueller.pathfinder.entities.operator_statistics.SingleOperatorStatistics;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.exceptions.VispRuntimeUnavailableException;
import net.knasmueller.pathfinder.repository.SingleOperatorStatisticsRepository;
import net.knasmueller.pathfinder.repository.VispRuntimeIdentifierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
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

    @Autowired
    VispTopology vispTopology;

    @Autowired
    VispRuntimeIdentifierRepository vriRepo;

    @Autowired
    SingleOperatorStatisticsRepository singleOperatorStatisticsRepository;

    /**
     * The current VISP topology file as a string
     */
    public String cachedTopologyString = "";
    private boolean initialized = false;

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

    public VispTopology getVispTopology() throws EmptyTopologyException {
        if (vispTopology == null || vispTopology.topology == null) {
            throw new EmptyTopologyException();
        }
        return vispTopology;
    }

    /**
     * Since there can be multiple VISP runtime instances in parallel, this method adds a new instance to the local
     * list of instances
     *
     * @param endpoint
     */
    public synchronized void addVispRuntime(VispRuntimeIdentifier endpoint) {
        waitForInit();

        if (endpoint == null || "".equals(endpoint)) {
            LOG.error("Invalid endpoint");
        } else {
            List<VispRuntimeIdentifier> knownIdentifiers = vriRepo.findAll();
            for (VispRuntimeIdentifier i : knownIdentifiers) {
                if (i.getIp().equals(endpoint.getIp()) && i.getPort() == endpoint.getPort()) {
                    LOG.warn("Did not add new VISP runtime; is already known to repository");
                    return;
                }
            }

            vriRepo.save(endpoint);
//            if(vispRuntimeIdentifiers.size() == 1) {
            // added the first runtime - fetch topology
//                String topology = getTopologyFromVisp(endpoint);
//                if(!this.getCachedTopologyString().equals(topology)) {
//                    this.setCachedTopologyString(topology);
//                    updateStoredTopology(topology);
//                }
//            }
//            LOG.debug("Added endpoint " + endpoint);
        }
    }

    private void waitForInit() {
        while (!this.isInitialized()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.error("Interrupted while waiting for VispCommunicato getting initialized");
                return;
            }
        }
    }

    public synchronized void removeVispRuntime(VispRuntimeIdentifier endpoint) {
        waitForInit();
        if (endpoint == null || "".equals(endpoint)) {
            LOG.error("Invalid endpoint");
        } else {
            //vispRuntimeIdentifiers.remove(endpoint);
            vriRepo.deleteByIpAndPort(endpoint.getIp(), endpoint.getPort());
        }
        LOG.debug("Removed endpoint " + endpoint);
    }


    public List<VispRuntimeIdentifier> getVispRuntimeIdentifiers() {
        return getVispRuntimeIdentifiers(false);
    }

    public List<VispRuntimeIdentifier> getVispRuntimeIdentifiers(boolean force) {
        if (!force) {
            waitForInit();
        }
        return vriRepo.findAll();
    }

    /**
     * Sends a REST request to a specific VISP runtime instance and asks it to return the current topology as a string
     *
     * @param rt
     * @return
     */
    public String getTopologyFromVisp(VispRuntimeIdentifier rt) throws VispRuntimeUnavailableException {
        RestTemplate restTemplate = new RestTemplate();
        URI targetUrl = UriComponentsBuilder.fromUriString("http://" + rt)
                .path("/getTopology")
                .build()
                .toUri();
        try {
            String topology = restTemplate.getForObject(targetUrl, String.class);
            return topology;

        } catch (ResourceAccessException e) {
            LOG.warn("Could not fetch topology");
            throw new VispRuntimeUnavailableException("Could not reach VISP runtime at " + rt);
        }
    }

    /**
     * Makes a test invocation and removes runtime if no answer in time arrives
     *
     * @param rt
     */
    public void pingAndMaybeDeleteRuntime(VispRuntimeIdentifier rt) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            URI targetUrl = UriComponentsBuilder.fromUriString("http://" + rt)
                    .path("/checkStatus")
                    .build()
                    .toUri();
            restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
            SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate
                    .getRequestFactory();
            rf.setReadTimeout(1000);
            rf.setConnectTimeout(1000);
            HashMap<String, String> result = restTemplate.getForObject(targetUrl, HashMap.class);
            if (!result.containsKey("onlineStatus")) {
                throw new Exception("Could not reach instance's checkStatus method");
            }
        } catch (Exception e) {
            LOG.info("Exception: ", e);
            vriRepo.deleteByIpAndPort(rt.getIp(), rt.getPort());
        }

    }

    /**
     * Updates the internally stored topology from a topology file string
     *
     * @param newTopology
     */
    public void updateStoredTopology(String newTopology) {
        LOG.debug("UPDATING stored VISP topology");
        Map<String, Operator> topology = topologyParser.parseTopologyFromString(newTopology).topology;
        vispTopology.setTopology(topology);
    }

    public void clearStoredTopology() {
        vispTopology.setTopology(null);
    }

    /**
     * Queries a VISP runtime for the current statistics
     *
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
     *
     * @param allStatistics statistics map returned by getStatisticsFromVisp()
     */
    public void persistStatisticEntries(Map<String, SingleOperatorStatistics> allStatistics) {
        for (Map.Entry<String, SingleOperatorStatistics> e : allStatistics.entrySet()) {
            SingleOperatorStatistics s = e.getValue();
            s.setOperatorName(e.getKey());
            singleOperatorStatisticsRepository.save(s);
        }
    }

    /**
     * instructs all VISP instances to change the active output path of split operator `split` to `path`
     *
     * @param splitPathPair list of changes; each item consists of a `split` `path` pair
     */
    public void switchSplitToPath(List<Pair<String, String>> splitPathPair) {
        for (Pair p : splitPathPair) {
            LOG.info("IMPLEMENT ME: sending message to VISP to switch " + p.getFirst()
                    + " to path " + p.getSecond());
        }
    }

    /**
     * Determines all split operators that are affected if that operator fails. A split operator is affected if
     * the operator's failure would cause the split operator to switch to a different fallback path
     *
     * @param operatorId The failing operator
     * @return returns a set of (operator, firstChild) that includes all affected split operators
     * @throws EmptyTopologyException
     */
    public Set<Pair<String, String>> getAffectedSplitOperators(String operatorId) throws EmptyTopologyException {
        // ASSUMPTION (for now): no nested split/join (this would get quite complicated)
        //TODO: generalize for nested split/join

        if (getVispTopology() == null || getVispTopology().getTopology() == null || getVispTopology().getTopology().isEmpty()) {
            throw new EmptyTopologyException("Could not return list of affected operators due to empty topology");
        }

        Set<Pair<String, String>> affectedSplitOperators = new HashSet<>();
        VispTopology topology = getVispTopology();

        Operator op = topology.getTopology().get(operatorId);

        Set<String> allSplitOperators = SplitDecisionService.getAlternativePaths(topology.getTopology()).keySet();

        LOG.info("All split operators: " + String.join(", ", allSplitOperators));

        for (String split : allSplitOperators) {
            String currentOp = split;
            String firstChild = null;
            List<String> possibleChilds = ((Split) topology.getTopology().get(split)).getPathOrder();
            Queue<String> q = new LinkedList<>();
            q.add(currentOp);
            while (!q.isEmpty()) {
                currentOp = q.remove();
                if (possibleChilds.contains(currentOp)) {
                    firstChild = currentOp;
                }
                LOG.info("Handling " + currentOp);
                if (currentOp.equals(operatorId)) {
                    boolean operatorIsAlreadyContainedInReturnSet = false;
                    for (Pair<String, String> entry : affectedSplitOperators) {
                        if (entry.getFirst().equals(split)) {
                            operatorIsAlreadyContainedInReturnSet = true;
                        }
                    }
                    if (!operatorIsAlreadyContainedInReturnSet) {
                        LOG.info("Adding operator to result list: " + split);
                        affectedSplitOperators.add(Pair.of(split, firstChild));
                    }
                }
                Set<String> downstreamOperators = SplitDecisionService.getDownstreamOperators(getVispTopology().getTopology(), currentOp);
                LOG.info("Downstream operators: " + String.join(", ", downstreamOperators));
                if (downstreamOperators.isEmpty()) {
                    continue;
                } else {
                    for (String downstreamOp : downstreamOperators) {
                        if (!(topology.getTopology().get(downstreamOp) instanceof Join)) {
                            LOG.info("Adding to queue: " + downstreamOp);
                            q.add(downstreamOp);
                        }
                    }
                }
            }
        }


        return affectedSplitOperators;
    }


    /**
     * Queries VISP for the current message flows; for each alternative path, VISP replies whether
     * a message flow is currently in place ("CLOSED"), it is being monitored ("HALF_OPEN") or whether it is blocked
     * ("OPEN")
     *
     * @return
     */
    public Map<String, String> getCurrentMessageFlows(List<String> paths) throws EmptyTopologyException {
        // TODO: actually implement getCurrentMessageFlows()

        Map<String, String> result = new HashMap<>();

        for (String path : paths) {
            result.put(path, "CLOSED");
        }

        return result;
    }

    /**
     * Instructs VISP to stop forwarding messages from parentSplitOperator to op
     *
     * @param parentSplitOperator
     * @param op
     */
    public void stopMessageFlow(String parentSplitOperator, String op) {
        LOG.warn("Not yet implemented: stopMessageFlow(" + parentSplitOperator + ", " + op + ")");
        // TODO: implement
    }

    /**
     * Instructs VISP to resume forwarding messages from parentSplitOperator to op
     *
     * @param parentSplitOperator
     * @param op
     */
    public void resumeMessageFlow(String parentSplitOperator, String op) {
        LOG.warn("Not yet implemented: resumeMessageFlow(" + parentSplitOperator + ", " + op + ")");
        // TODO: implement
    }

    /**
     * Instructs VISP to probe whether a normal message flow can be restored
     *
     * @param parentSplitOperator
     * @param op
     */
    public void probeMessageFlow(String parentSplitOperator, String op) {
        // TODO: implement
        LOG.warn("Not yet implemented: probeMessageFlow(" + parentSplitOperator + ", " + op + ")");
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
