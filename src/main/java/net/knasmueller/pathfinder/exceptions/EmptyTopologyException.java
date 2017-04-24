package net.knasmueller.pathfinder.exceptions;

public class EmptyTopologyException extends Exception
{
    public EmptyTopologyException() {}

    public EmptyTopologyException(String message)
    {
        super(message);
    }
}
