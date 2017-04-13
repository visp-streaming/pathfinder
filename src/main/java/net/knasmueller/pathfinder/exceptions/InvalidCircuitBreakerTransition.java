package net.knasmueller.pathfinder.exceptions;

public class InvalidCircuitBreakerTransition extends Exception
{
    public InvalidCircuitBreakerTransition() {}

    public InvalidCircuitBreakerTransition(String message)
    {
        super(message);
    }
}
