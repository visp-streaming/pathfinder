package net.knasmueller.pathfinder.exceptions;

/**
 * Thrown when PathFinder is not able to find a fallback path where no operator has failed
 */
public class NoAlternativePathAvailableException extends Exception
{
    public NoAlternativePathAvailableException() {}

    public NoAlternativePathAvailableException(String message)
    {
        super(message);
    }
}
