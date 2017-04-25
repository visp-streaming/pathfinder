package net.knasmueller.pathfinder.exceptions;

public class NoAlternativePathAvailableException extends Exception
{
    public NoAlternativePathAvailableException() {}

    public NoAlternativePathAvailableException(String message)
    {
        super(message);
    }
}
