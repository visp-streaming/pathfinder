package net.knasmueller.pathfinder.domain.impl;

import net.knasmueller.pathfinder.domain.ICircuitBreakerStatusProvider;
import net.knasmueller.pathfinder.service.CircuitBreaker;
import net.knasmueller.pathfinder.service.SplitDecisionService;
import net.knasmueller.pathfinder.service.VispCommunicator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class CircuitBreakerStatusProvider implements ICircuitBreakerStatusProvider {

    SplitDecisionService splitDecisionService;

    public CircuitBreakerStatusProvider(SplitDecisionService splitDecisionService) {
        this.splitDecisionService = splitDecisionService;
    }

    @Override
    public Map<String, CircuitBreaker> getCircuitBreakerMap() {
        return splitDecisionService.getCircuitBreakerMap();
    }
}
