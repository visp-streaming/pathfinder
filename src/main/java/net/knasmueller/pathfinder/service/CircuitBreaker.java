package net.knasmueller.pathfinder.service;

import net.knasmueller.pathfinder.exceptions.InvalidCircuitBreakerTransition;
import org.springframework.stereotype.Service;

/**
 * A circuit breaker maintains the state of a particular operator and can transition into other states
 */
public class CircuitBreaker {
    public State getState() {
        return this.state;
    }

    enum State {OPEN, HALF_OPEN, CLOSED}

    private State state;

    public CircuitBreaker() {
        this.state = State.CLOSED;
    }

    /**
     * sets the state to OPEN which will cause all outgoing communication to stop
     */
    public void open() {
        this.state = State.OPEN;
    }

    /**
     * sets the state to HALF_OPEN (only legal when previous state is OPEN). In this state, only a small
     * fraction of the outgoing communication is allowed to pass in order to test whether communication is possible
     * @throws InvalidCircuitBreakerTransition
     */
    public void halfOpen() throws InvalidCircuitBreakerTransition {
        if(this.state.equals(State.OPEN)) {
            this.state = State.HALF_OPEN;
        }
        if(this.state.equals(State.CLOSED)) {
            throw new InvalidCircuitBreakerTransition("Transition to state HALF_OPEN only valid from state OPEN");
        }
    }

    /**
     * sets the state to CLOSED (all outgoing communication is allowed again).
     */
    public void close() {
        this.state = State.CLOSED;
    }

    public boolean isOpen() {
        return this.state.equals(State.OPEN);
    }

    public boolean isHalfOpen() {
        return this.state.equals(State.HALF_OPEN);
    }

    public boolean isClosed() {
        return this.state.equals(State.CLOSED);
    }

    @Override
    public String toString() {
        return state.toString().toUpperCase();
    }
}
