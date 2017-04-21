package net.knasmueller.pathfinder.service;

import net.knasmueller.pathfinder.exceptions.InvalidCircuitBreakerTransition;
import net.knasmueller.pathfinder.exceptions.UnknownOperatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SplitManagement {
    /**
     * used by the nexus to update circuit breaker status for all known split operators
     */
    Map<String, CircuitBreaker> circuitBreakerMap = new HashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(SplitManagement.class);

    public SplitManagement() {

    }

    public void clear() {
        circuitBreakerMap.clear();
    }

    public void addSplitOperators(List<String> splitOperators) {
        for(String s : splitOperators) {
            circuitBreakerMap.put(s, new CircuitBreaker());
        }
    }

    public boolean isOpen(String operatorId) {
        if(!circuitBreakerMap.containsKey(operatorId)) {
            throw new UnknownOperatorException("Operator with id " + operatorId + " is unknown");
        }
        return circuitBreakerMap.get(operatorId).isOpen();
    }

    public boolean isHalfOpen(String operatorId) {
        if(!circuitBreakerMap.containsKey(operatorId)) {
            throw new UnknownOperatorException("Operator with id " + operatorId + " is unknown");
        }
        return circuitBreakerMap.get(operatorId).isHalfOpen();
    }

    public boolean isClosed(String operatorId) {
        if(!circuitBreakerMap.containsKey(operatorId)) {
            throw new UnknownOperatorException("Operator with id " + operatorId + " is unknown");
        }
        return circuitBreakerMap.get(operatorId).isClosed();
    }

    public void open(String operatorId) {
        if(!circuitBreakerMap.containsKey(operatorId)) {
            throw new UnknownOperatorException("Operator with id " + operatorId + " is unknown");
        }
        this.circuitBreakerMap.get(operatorId).open();
    }

    public void halfOpen(String operatorId) throws InvalidCircuitBreakerTransition {
        if(!circuitBreakerMap.containsKey(operatorId)) {
            throw new UnknownOperatorException("Operator with id " + operatorId + " is unknown");
        }
        this.circuitBreakerMap.get(operatorId).halfOpen();
    }

    public void close(String operatorId) {
        if(!circuitBreakerMap.containsKey(operatorId)) {
            throw new UnknownOperatorException("Operator with id " + operatorId + " is unknown");
        }
        this.circuitBreakerMap.get(operatorId).close();
    }


}
