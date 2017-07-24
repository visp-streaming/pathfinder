package net.knasmueller.pathfinder.domain;

import net.knasmueller.pathfinder.service.CircuitBreaker;

import java.util.Map;

public interface ICircuitBreakerStatusProvider {
    Map<String,CircuitBreaker> getCircuitBreakerMap();
}
