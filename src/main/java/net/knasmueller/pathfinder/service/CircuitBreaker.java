package net.knasmueller.pathfinder.service;

import org.springframework.stereotype.Service;

@Service
public class CircuitBreaker {
    enum State {OPEN, HALF_OPEN, CLOSED}

    private State state;

    public CircuitBreaker() {
        this.state = State.CLOSED;
    }

    public void open() {
        this.state = State.OPEN;
    }

    public void halfOpen() {
        this.state = State.HALF_OPEN;
    }

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
}
