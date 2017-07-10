package net.knasmueller.pathfinder.exceptions;

/**
 * Thrown when an action is applied to an operator id that is unknown to the system
 */
public class UnknownOperatorException extends RuntimeException
{
    public UnknownOperatorException() {}

    public UnknownOperatorException(String message)
    {
        super(message);
    }
}
