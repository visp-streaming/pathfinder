package net.knasmueller.pathfinder.exceptions;

public class UnknownOperatorException extends RuntimeException
{
    public UnknownOperatorException() {}

    public UnknownOperatorException(String message)
    {
        super(message);
    }
}
