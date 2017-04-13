package net.knasmueller.pathfinder.service;

import net.knasmueller.pathfinder.entities.PathfinderOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class OperatorManagement {
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
}
