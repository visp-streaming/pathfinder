package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.common.operators.Split;
import net.knasmueller.pathfinder.entities.PathfinderOperator;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.exceptions.NoAlternativePathAvailableException;
import net.knasmueller.pathfinder.exceptions.OperatorNotFoundException;
import net.knasmueller.pathfinder.service.nexus.INexus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.expression.spel.ast.OperatorNot;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProcessingOperatorManagement {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingOperatorManagement.class);

    @Autowired
    private VispCommunicator vispCommunicator;

    HashMap<String, PathfinderOperator> processingOperatorMap = new HashMap<>(); // stores each processing operator's status

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

    public PathfinderOperator.Status getOperatorStatus(String operatorId) {
        return processingOperatorMap.get(operatorId).getStatus();

    }

    public void setOperatorStatus(String operatorId, INexus.OperatorClassification status) {
        if (status.equals(INexus.OperatorClassification.FAILED)) {
            setOperatorStatus(operatorId, "failed");
        } else if (status.equals(INexus.OperatorClassification.WORKING)) {
            setOperatorStatus(operatorId, "working");
        } else {
            throw new RuntimeException("invalid status: " + status.toString());
        }
    }

    public void updateOperatorAvailabilities(Map<String, INexus.OperatorClassification> newAvailabilities) {
        for (String operatorName : newAvailabilities.keySet()) {
            setOperatorStatus(operatorName, newAvailabilities.get(operatorName));
        }

        contactVispWithNewRecommendations(newAvailabilities);
    }

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
            vispCommunicator.switchSplitToPath(pairsToSwitch);
        } catch (EmptyTopologyException e) {
            LOG.error("contactVispWithNewRecommendations() call without active topology", e);
        }
        // add recommendation for ALL split operators - VISP will be wise enough to ignore those that don't matter

    }

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

    private String getBestAvailablePath(String splitId) throws NoAlternativePathAvailableException {
        Split splitOperator;
        try {
            splitOperator = (Split) vispCommunicator.getVispTopology().getOperator(splitId);
            List<String> pathAlternatives = splitOperator.getPathOrder();

            for (String currentAlternative : pathAlternatives) {
                if (getOperatorStatus(currentAlternative).equals(PathfinderOperator.Status.WORKING)) {
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

    public void topologyUpdate(Map<String, Operator> newTopology) {
        /** this function is called when the VISP instance has a new topology.
         * It removes and recreates the local operator topology
         * **/

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
                return false;
            }
            return processingOperatorMap.get(operatorId).getStatus().equals(PathfinderOperator.Status.WORKING);
        } catch (Exception e) {
            LOG.error("Could not retrieve operator status for operator " + operatorId, e);
            return false;
        }
    }

    public static Map<String, List<String>> getAlternativePaths(Map<String, Operator> topology) {
        /** this function extracts each split operator with the list of children in the correct order **/
        Map<String, List<String>> result = new HashMap<>();
        for (String operatorId : topology.keySet()) {
            if (topology.get(operatorId) instanceof Split) {
                List<String> splitChildren = new ArrayList<>();
                splitChildren.addAll(((Split) topology.get(operatorId)).getPathOrder());
                result.put(operatorId, splitChildren);
            }
        }

        return result;
    }

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
}
