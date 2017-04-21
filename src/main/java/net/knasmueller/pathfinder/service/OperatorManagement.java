package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.common.operators.Split;
import net.knasmueller.pathfinder.entities.PathfinderOperator;
import net.knasmueller.pathfinder.service.nexus.INexus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperatorManagement {
    /* TODO: maybe think again if this is the best way to map this relation */
    private static final Logger LOG = LoggerFactory.getLogger(OperatorManagement.class);

    HashMap<String, PathfinderOperator> operatorMap = new HashMap<>();

    public void setOperatorStatus(String operatorId, String status) {
        try {
            if(!operatorMap.containsKey(operatorId)) {
                operatorMap.put(operatorId, new PathfinderOperator(operatorId));
            }
            operatorMap.get(operatorId).setStatus( "working".equals(status.toLowerCase()) ?
                    PathfinderOperator.Status.WORKING : PathfinderOperator.Status.FAILED );
        } catch(Exception e) {
            LOG.error("Could not set operator status for operatorId = " + operatorId, e);
        }
    }

    public void setOperatorStatus(String operatorId, INexus.OperatorClassification status) {
        if(status.equals(INexus.OperatorClassification.FAILED)) {
            setOperatorStatus(operatorId, "failed");
        } else if(status.equals(INexus.OperatorClassification.WORKING)) {
            setOperatorStatus(operatorId, "working");
        } else {
            throw new RuntimeException("invalid status: " + status.toString());
        }
    }

    public void updateOperatorAvailabilities(Map<String, INexus.OperatorClassification> newAvailabilities) {
        for(String operatorName : newAvailabilities.keySet()) {
            setOperatorStatus(operatorName, newAvailabilities.get(operatorName));
        }
    }

    public HashMap<String, PathfinderOperator> getOperators() {
        return operatorMap;
    }

    public void topologyUpdate(Map<String, Operator> newTopology) {
        /** this function is called when the VISP instance has a new topology.
         * It removes and recreates the local operator topology
         * **/

        operatorMap.clear();
        for(String operatorId : newTopology.keySet()) {
            PathfinderOperator operator = new PathfinderOperator(newTopology.get(operatorId));
            // assume each operator is working in the beginning
            operator.setStatus(PathfinderOperator.Status.WORKING);
            operatorMap.put(operatorId, operator);
        }

        return;

    }

    public static Map<String,List<String>> getAlternativePaths(Map<String, Operator> topology) {
        /** this function extracts each split operator with the list of children in the correct order **/
        Map<String,List<String>> result = new HashMap<>();
        for(String operatorId : topology.keySet()) {
            if(topology.get(operatorId) instanceof Split) {
                List<String> splitChildren = new ArrayList<>();
                splitChildren.addAll(((Split) topology.get(operatorId)).getPathOrder());
                result.put(operatorId, splitChildren);
            }
        }

        return result;
    }

    public boolean isOperatorAvailable(String operatorId) {
        try {
            if(!operatorMap.containsKey(operatorId)) {
                return false;
            }
            return operatorMap.get(operatorId).getStatus().equals(PathfinderOperator.Status.WORKING);
        } catch(Exception e) {
            LOG.error("Could not retrieve operator status for operator " + operatorId, e);
            return false;
        }
    }
}
