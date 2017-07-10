package net.knasmueller.pathfinder.exceptions;

/**
 * Thrown when an invalid transition is attempted on a circuit breaker
 */
public class InvalidCircuitBreakerTransition extends Exception
{
    public InvalidCircuitBreakerTransition() {}

    public InvalidCircuitBreakerTransition(String message)
    {
        super(message);
    }
}
