package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.common.operators.Join;
import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.common.operators.Split;
import net.knasmueller.pathfinder.entities.TopologyStability;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.exceptions.InvalidCircuitBreakerTransition;
import net.knasmueller.pathfinder.exceptions.UnknownOperatorException;
import net.knasmueller.pathfinder.repository.TopologyStabilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * used by the nexus to update circuit breaker status for all known split operators
 * Each split fallback path has its own circuit breaker
 * Can also be used to query the status of a specific split operator's circuit breaker
 */
@Service
public class SplitDecisionService {

    Map<String, CircuitBreaker> circuitBreakerMap = new HashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(SplitDecisionService.class);

    @Autowired
    private TopologyStabilityRepository tsr;

    @Autowired
    private VispCommunicator vispCommunicator;

    @Autowired
    private ProcessingOperatorHealth poh;

    public SplitDecisionService() {

    }

    /**
     * Reset all circuit breakers
     */
    public void clear() {
        circuitBreakerMap.clear();
    }

    /**
     * Initialize circuit breakers for specific list of split operators
     * @param splitOperators set of split operators where a circuit breaker should be created
     */
    @Deprecated
    public void addSplitOperators(List<String> splitOperators) {
        for(String s : splitOperators) {
            circuitBreakerMap.put(s, new CircuitBreaker());
        }
    }

    public void updateSplitOperatorsFromTopology() {
        for(String split : getAlternativePaths().keySet()) {
            for(String path : getAlternativePaths().get(split)) {
                circuitBreakerMap.put(path, new CircuitBreaker());
            }
        }
    }

    public CircuitBreaker getCircuitBreakerOfPath(String pathId) {
        return circuitBreakerMap.get(pathId);
    }

    public boolean isOpen(String pathId) {
        if(!circuitBreakerMap.containsKey(pathId)) {
            throw new UnknownOperatorException("Path with id " + pathId + " is unknown");
        }
        return circuitBreakerMap.get(pathId).isOpen();
    }

    public boolean isHalfOpen(String pathId) {
        if(!circuitBreakerMap.containsKey(pathId)) {
            throw new UnknownOperatorException("Path with id " + pathId + " is unknown");
        }
        return circuitBreakerMap.get(pathId).isHalfOpen();
    }

    public boolean isClosed(String pathId) {
        if(!circuitBreakerMap.containsKey(pathId)) {
            throw new UnknownOperatorException("Path with id " + pathId + " is unknown");
        }
        return circuitBreakerMap.get(pathId).isClosed();
    }

    public void open(String pathId) {
        if(!circuitBreakerMap.containsKey(pathId)) {
            throw new UnknownOperatorException("Path with id " + pathId + " is unknown");
        }
        this.circuitBreakerMap.get(pathId).open();
    }

    public void halfOpen(String pathId) throws InvalidCircuitBreakerTransition {
        if(!circuitBreakerMap.containsKey(pathId)) {
            throw new UnknownOperatorException("Path with id " + pathId + " is unknown");
        }
        this.circuitBreakerMap.get(pathId).halfOpen();
    }

    public void close(String pathId) {
        if(!circuitBreakerMap.containsKey(pathId)) {
            throw new UnknownOperatorException("Path with id " + pathId + " is unknown");
        }
        this.circuitBreakerMap.get(pathId).close();
    }

    public Map<String, List<String>> getAlternativePaths() {
        try {
            return getAlternativePaths(vispCommunicator.getVispTopology().getTopology());
        } catch (EmptyTopologyException e) {
            return new HashMap<>();
        }
    }

    /**
     * this function extracts each split operator with the list of children in the correct order
     *
     * @param topology
     * @return
     */
    public static Map<String, List<String>> getAlternativePaths(Map<String, Operator> topology) throws EmptyTopologyException {
        Map<String, List<String>> result = new HashMap<>();
        if (topology == null) {
            throw new EmptyTopologyException();
        }
        for (String operatorId : topology.keySet()) {
            if (topology.get(operatorId) instanceof Split) {
                List<String> splitChildren = new ArrayList<>();
                splitChildren.addAll(((Split) topology.get(operatorId)).getPathOrder());
                result.put(operatorId, splitChildren);
            }
        }

        return result;
    }

    /**
     * Gets all operators that are directly consuming messages of the specified operator
     *
     * @param topology
     * @param operatorId
     * @return set of downstream operator ids
     */
    public static Set<String> getDownstreamOperators(Map<String, Operator> topology, String operatorId) {
        Set<String> result = new HashSet<>();

        if (topology == null || topology.isEmpty() || operatorId == null || operatorId.equals("")) {
            return result;
        }

        for (String o : topology.keySet()) {
            // check if current operator is child of operatorId
            List<Operator> sources = topology.get(o).getSources();
            if (sources == null || sources.size() == 0) {
                continue;
            }
            for (Operator source : sources) {
                if (source.getName().equals(operatorId)) {
                    result.add(o);
                }
            }
        }
        return result;
    }

    /**
     * adds a row to the database describing the current topology's stability
     */
    public void updateTopologyStability() {
        // called by Scheduler

        String topologyHash = null;
        try {
            topologyHash = vispCommunicator.getVispTopology().getHash();
        } catch (EmptyTopologyException e) {
            return;
        }

        TopologyStability ts = new TopologyStability(topologyHash, getCurrentTopologyStability());
        tsr.save(ts);
    }

    /**
     * Computes the current topology's stability by evaluating the number of failed paths
     * @return number between 0 and 1; 1 means high stability, 0 means low stability
     */
    private double getCurrentTopologyStability() {
        if (this.getAlternativePaths().size() == 0) {
            return 1.0; // no fallback paths at all
        }
        double stability = 0.0;
        int counter = 0;
        for (String splitId : this.getAlternativePaths().keySet()) {
            double currentStability = 0.0;
            counter++;

            List<String> outgoingPaths = this.getAlternativePaths().get(splitId);
            int availablePaths = 0;
            for (String path : outgoingPaths) {
                if (this.isPathAvailable(path)) {
                    availablePaths++;
                }
            }
            currentStability = ((double) availablePaths) / outgoingPaths.size();
            stability += currentStability;
        }
        return stability / counter;
    }

    /**
     * Checks whether a path is available in the current topology
     *
     * @param path operator ID of the first operator in the path; last operator is the one before the join node
     * @return True if all operators along that path are working, false otherwise
     */
    public boolean isPathAvailable(String path) {
        Set<String> idsToCheck = new HashSet<>();
        Queue<String> idsToVisit = new LinkedList<>();
        idsToVisit.add(path);
        Operator currentOperator;

        while (true) {
            if (idsToVisit.isEmpty()) {
                break;
            }
            currentOperator = this.vispCommunicator.getVispTopology().getTopology().get(idsToVisit.poll());
            if (!idsToCheck.contains(currentOperator.getName())) {
                idsToCheck.add(currentOperator.getName());
            }
            Set<String> nextOperators = getDownstreamOperators(this.vispCommunicator.getVispTopology().getTopology(), currentOperator.getName());
            if (nextOperators.isEmpty()) {
                continue;
            }
            for (String op : nextOperators) {
                if (this.vispCommunicator.getVispTopology().getTopology().get(op) instanceof Join) {
                    continue;
                } else {
                    idsToVisit.add(op);
                }

            }

        }

        // now check if each operator is available
        boolean pathIsAvailable = true;
        for (String operatorToCheck : idsToCheck) {
            pathIsAvailable &= poh.isOperatorAvailable(operatorToCheck);
        }

        return pathIsAvailable;
    }

    /**
     * Queries the database for the 10 last stability measurements for the current topology
     * @param topologyHash hash of the current topology (changes when new operators are added)
     * @return the 10 youngest stability measurements in the database
     */
    public List<TopologyStability> getStabilityTop10(String topologyHash) {
        return tsr.findAllTop20ByTopologyHashOrderByTimestamp(topologyHash, new PageRequest(0, 10));
    }


    /**
     * Called each second by the scheduler; checks each circuit and changes its state if necessary
     */
    public void updateCircuits() {
        // open all paths' circuit breakers where their path has failed operators
        for(String s : circuitBreakerMap.keySet()) {
            if(circuitBreakerMap.get(s).isClosed() && !isPathAvailable(s)) {
                circuitBreakerMap.get(s).open();
                LOG.debug("Changed path " + s + "'s circuit breaker to OPEN");
            } else {
                if(circuitBreakerMap.get(s).isOpen() && isPathAvailable(s)) {
                    try {
                        circuitBreakerMap.get(s).halfOpen();
                        LOG.debug("Changed path " + s + "'s circuit breaker to HALF_OPEN");
                    } catch (InvalidCircuitBreakerTransition invalidCircuitBreakerTransition) {
                        LOG.warn("Could not change circuit breaker state", invalidCircuitBreakerTransition);
                    }
                }
            }
        }
    }
}
