package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.common.operators.Split;
import net.knasmueller.pathfinder.entities.PathfinderOperator;
import net.knasmueller.pathfinder.entities.TopologyStability;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.exceptions.NoAlternativePathAvailableException;
import net.knasmueller.pathfinder.exceptions.OperatorNotFoundException;
import net.knasmueller.pathfinder.repository.TopologyStabilityRepository;
import net.knasmueller.pathfinder.service.nexus.INexus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A service that keeps track of each processing node's health
 */
@Service
public class ProcessingOperatorHealth {
    // TODO: refactor; some methods belong to SplitManagement
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingOperatorHealth.class);

    @Autowired
    private VispCommunicator vispCommunicator;

    @Autowired
    private TopologyStabilityRepository tsr;

    /**
     * Similar to a topology, this map stores a PathFinderOperator object for each operator id
     */
    HashMap<String, PathfinderOperator> processingOperatorMap = new HashMap<>(); // stores each processing operator's status

    /**
     * Updates the health status of a specific operator by a string status (working or failed)
     *
     * @param operatorId string identifier for the operator as used in the topology
     * @param status     the new health status for that operator
     */
    public void setOperatorStatus(String operatorId, String status) {
        try {
            if (!processingOperatorMap.containsKey(operatorId)) {
                processingOperatorMap.put(operatorId, new PathfinderOperator(operatorId));
            }
            processingOperatorMap.get(operatorId).setStatus("working".equals(status.toLowerCase()) ?
                    PathfinderOperator.Status.WORKING : PathfinderOperator.Status.FAILED);
        } catch (Exception e) {
            LOG.error("Could not set operator status for operatorId = " + operatorId, e);
        }
    }

    /**
     * Updates the health status of a specific operator by a classification object
     *
     * @param operatorId string identifier for the operator as used in the topology
     * @param status     the new health status for that operator
     */
    public void setOperatorStatus(String operatorId, INexus.OperatorClassification status) {
        if (status.equals(INexus.OperatorClassification.FAILED)) {
            setOperatorStatus(operatorId, "failed");
        } else if (status.equals(INexus.OperatorClassification.WORKING)) {
            setOperatorStatus(operatorId, "working");
        } else {
            throw new RuntimeException("invalid status: " + status.toString());
        }
    }

    public PathfinderOperator.Status getOperatorStatus(String operatorId) {
        return processingOperatorMap.get(operatorId).getStatus();

    }

    /**
     * Updates health status of multiple operators at once
     *
     * @param newAvailabilities map of (String, Classification) (coming from a classifier)
     */
    public void updateOperatorAvailabilities(Map<String, INexus.OperatorClassification> newAvailabilities) {
        for (String operatorName : newAvailabilities.keySet()) {
            setOperatorStatus(operatorName, newAvailabilities.get(operatorName));
        }

        contactVispWithNewRecommendations(newAvailabilities);
    }

    /**
     * Iterates over all split operators and determines the recommended output path for each of them
     * Then contacts VISP with a list of switch recommendations
     *
     * @param newAvailabilities
     */
    public void contactVispWithNewRecommendations(Map<String, INexus.OperatorClassification> newAvailabilities) {
        for (String s : newAvailabilities.keySet()) {
            LOG.info("new availability: " + s + " / " + newAvailabilities.get(s));
        }
        // newAvailabilities probably not necessary since the availabilities have already been updated
        List<Pair<String, String>> pairsToSwitch = new ArrayList<>();
        try {
            for (String splitId : vispCommunicator.getVispTopology().getSplitOperatorIds()) {
                String pathId;
                try {
                    pathId = getBestAvailablePath(splitId);
                } catch (NoAlternativePathAvailableException e) {
                    LOG.error("Could not find an available path; all down. Returning main path", e);
                    pathId = getMainPath(splitId);
                }
                pairsToSwitch.add(Pair.of(splitId, pathId));
            }
            vispCommunicator.switchSplitToPath(pairsToSwitch); // TODO: doing this once for each operator and not in a batch is highly inefficient
        } catch (EmptyTopologyException e) {
            LOG.error("contactVispWithNewRecommendations() call without active topology", e);
        }
        // add recommendation for ALL split operators - VISP will be wise enough to ignore those that don't matter

    }

    /**
     * Returns the main path of a specific split operator
     *
     * @param splitId the identifier of the split operator
     * @return the main path of that split operator (as defined by the user's pathOrder)
     */
    private String getMainPath(String splitId) {
        Split splitOperator;
        try {
            splitOperator = (Split) vispCommunicator.getVispTopology().getOperator(splitId);
            return splitOperator.getPathOrder().get(0);
        } catch (OperatorNotFoundException e) {
            throw new RuntimeException("Could not get main path for operator " + splitId + " - operator not found");
        } catch (EmptyTopologyException e) {
            throw new RuntimeException("Could not get main path for operator " + splitId + " - topology is empty");
        }
    }

    /**
     * Returns the split operator's path with the lowest path ranking that is still available
     *
     * @param splitId
     * @return first operator's id of the best path
     * @throws NoAlternativePathAvailableException
     */
    private String getBestAvailablePath(String splitId) throws NoAlternativePathAvailableException {
        Split splitOperator;
        try {
            splitOperator = (Split) vispCommunicator.getVispTopology().getOperator(splitId);
            List<String> pathAlternatives = splitOperator.getPathOrder();

            for (String currentAlternative : pathAlternatives) {
                if (getOperatorStatus(currentAlternative).equals(PathfinderOperator.Status.WORKING)) {
                    //TODO: this is insufficient; the error does not need to be in the first operator, it could also be
                    // that a downstream operator is failing
                    return currentAlternative;
                }
            }
            throw new NoAlternativePathAvailableException("All paths failed for split operator " + splitId);

        } catch (OperatorNotFoundException e) {
            throw new RuntimeException("Could not get main path for operator " + splitId + " - operator not found");
        } catch (EmptyTopologyException e) {
            throw new RuntimeException("Could not get main path for operator " + splitId + " - topology is empty");
        }
    }

    public HashMap<String, PathfinderOperator> getOperators() {
        return processingOperatorMap;
    }

    /**
     * this function is called when the VISP instance has a new topology. It removes and recreates the local operator topology
     *
     * @param newTopology
     */
    public void topologyUpdate(Map<String, Operator> newTopology) {
        processingOperatorMap.clear();
        if (newTopology == null || newTopology.keySet().isEmpty()) {
            return;
        }
        for (String operatorId : newTopology.keySet()) {
            PathfinderOperator operator = new PathfinderOperator(newTopology.get(operatorId));
            // assume each operator is working in the beginning
            operator.setStatus(PathfinderOperator.Status.WORKING);
            processingOperatorMap.put(operatorId, operator);
        }

        return;

    }


    public boolean isOperatorAvailable(String operatorId) {
        try {
            if (!processingOperatorMap.containsKey(operatorId)) {
                LOG.debug("Could not find operator " + operatorId + "; returning health = failed");
                return false;
            }
            boolean operatorWorking = processingOperatorMap.get(operatorId).getStatus().equals(PathfinderOperator.Status.WORKING);
            LOG.debug("returning health = " + (operatorWorking ? "working" : "failed") + " for operator " + operatorId);
            return operatorWorking;
        } catch (Exception e) {
            LOG.error("Could not retrieve operator status for operator " + operatorId, e);
            return false;
        }
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

    public void updateTopologyStability() {
        // called by Scheduler

        String topologyHash = null;
        try {
            topologyHash = vispCommunicator.getVispTopology().getHash();
        } catch (EmptyTopologyException e) {
            return;
        }

        TopologyStability ts = new TopologyStability(topologyHash, ThreadLocalRandom.current().nextDouble(0, 1.0));
        tsr.save(ts);
    }

    public List<TopologyStability> getStabilityTop10(String topologyHash) {
        return tsr.findAllTop20ByTopologyHashOrderByTimestamp(topologyHash, new PageRequest(0, 10));
    }
}
