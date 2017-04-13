package net.knasmueller.pathfinder.unit_tests;

import net.knasmueller.pathfinder.exceptions.InvalidCircuitBreakerTransition;
import net.knasmueller.pathfinder.service.CircuitBreaker;
import net.knasmueller.pathfinder.service.Communicator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class CircuitBreakerTests {
    CircuitBreaker circuitBreaker;
    @Before
    public void init() {
        circuitBreaker = new CircuitBreaker();
    }

    @Test
    public void test_initialStateIsClosed() {
        assert(circuitBreaker.isClosed());
    }

    @Test
    public void test_openProducesOpenState() {
        assert(circuitBreaker.isClosed());
        circuitBreaker.open();
        assert(circuitBreaker.isOpen());
    }

    @Test(expected=InvalidCircuitBreakerTransition.class)
    public void test_transitionFromClosedToHalfOpen_notAllowed() throws InvalidCircuitBreakerTransition {
        assert(circuitBreaker.isClosed());
        circuitBreaker.halfOpen();
    }

    @Test(expected=InvalidCircuitBreakerTransition.class)
    public void test_transitionFromClosedToHalfOpenAfterOtherTransitions_notAllowed() throws InvalidCircuitBreakerTransition {
        assert(circuitBreaker.isClosed());
        circuitBreaker.open();
        circuitBreaker.close();
        circuitBreaker.close();
        circuitBreaker.open();
        circuitBreaker.close();
        circuitBreaker.halfOpen();
    }

    @Test
    public void test_transitionFromOpenToHalfOpen_isAllowed() throws InvalidCircuitBreakerTransition {
        assert(circuitBreaker.isClosed());
        circuitBreaker.open();
        circuitBreaker.halfOpen();
        assert(circuitBreaker.isHalfOpen());
    }


}
