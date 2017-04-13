package net.knasmueller.pathfinder.controller;

import net.knasmueller.pathfinder.exceptions.InvalidCircuitBreakerTransition;
import net.knasmueller.pathfinder.service.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/circuitbreaker")
public class CircuitBreakerController {

    private static final Logger LOG = LoggerFactory.getLogger(CircuitBreakerController.class);

    @Autowired
    private CircuitBreaker circuitBreaker;

    @RequestMapping("/open")
    public String open() {
        LOG.debug("Opening circuit breaker");
        circuitBreaker.open();
        return "";
    }

    @RequestMapping("/close")
    public String close() {
        LOG.debug("Closing circuit breaker");
        circuitBreaker.close();
        return "";
    }

    @RequestMapping("/halfOpen")
    public String halfOpen() throws InvalidCircuitBreakerTransition {
        LOG.debug("Half-opening circuit breaker");
        circuitBreaker.halfOpen();
        return "";
    }
}