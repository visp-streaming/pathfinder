package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.common.operators.Split;
import net.knasmueller.pathfinder.entities.PathfinderOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OperatorManagement {
    /* TODO: maybe think again if this is the best way to map this relation */
    private static final Logger LOG = LoggerFactory.getLogger(OperatorManagement.class);

    ConcurrentHashMap<String, PathfinderOperator> operatorMap = new ConcurrentHashMap<>();

    public void setOperatorStatus(String operatorId, String status) {
        try {
            if(!operatorMap.contains(operatorId)) {
                operatorMap.put(operatorId, new PathfinderOperator(operatorId));
            }
            operatorMap.get(operatorId).setStatus( "working".equals(status.toLowerCase()) ?
                    PathfinderOperator.Status.WORKING : PathfinderOperator.Status.FAILED );
        } catch(Exception e) {
            LOG.error("Could not set operator status for operatorId = " + operatorId, e);
        }
    }

    public ConcurrentHashMap<String, PathfinderOperator> getOperators() {
        return operatorMap;
    }

    public Map<String,List<String>> getAlternativePaths(Map<String, Operator> topology) {
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
}
