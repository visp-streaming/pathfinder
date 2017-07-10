package net.knasmueller.pathfinder.exceptions;

/**
 * Thrown when a search method does not find a suitable operator for a specified query
 */
public class OperatorNotFoundException extends Exception
{
    public OperatorNotFoundException() {}

    public OperatorNotFoundException(String message)
    {
        super(message);
    }
}
