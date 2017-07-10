package net.knasmueller.pathfinder.exceptions;

/**
 * Thrown when there is currently no topology (that should have been fetched from VISP)
 */
public class EmptyTopologyException extends Exception
{
    public EmptyTopologyException() {}

    public EmptyTopologyException(String message)
    {
        super(message);
    }
}
