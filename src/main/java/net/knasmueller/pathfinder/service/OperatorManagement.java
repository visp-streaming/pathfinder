package net.knasmueller.pathfinder.service;

import net.knasmueller.pathfinder.entities.Operator;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OperatorManagement {
    private static final Logger LOG = LoggerFactory.getLogger(OperatorManagement.class);

    ConcurrentHashMap<String, Operator> operatorMap = new ConcurrentHashMap<String, Operator>();

    public void setOperatorStatus(String operatorId, String status) {
        try {
            if(!operatorMap.contains(operatorId)) {
                operatorMap.put(operatorId, new Operator(operatorId));
            }
            operatorMap.get(operatorId).setStatus( "working".equals(status.toLowerCase()) ? Operator.Status.WORKING : Operator.Status.FAILED );
        } catch(Exception e) {
            LOG.error("Could not set operator status for operatorId = " + operatorId, e);
        }
    }

    public ConcurrentHashMap<String, Operator> getOperators() {
        return operatorMap;
    }
}
