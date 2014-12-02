package sorting.polyphase.kway;

/**
 * Wrapper for any exceptions thrown
 * 
 * @author Jordan Zimmerman (jordan@jordanzimmerman.com)
 * @version 1.1 Dec. 1, 2007 made a top level class
 */
public class KWayMergeError extends Exception
{
    public KWayMergeError()
    {
        fCause = null;
    }

    public KWayMergeError(String message)
    {
        super(message);
        fCause = null;
    }

    public KWayMergeError(String message, Throwable cause)
    {
        super(message);
        fCause = cause;
    }

    public KWayMergeError(Throwable cause)
    {
        fCause = cause;
    }

    public Throwable getCause()
    {
        return fCause;
    }

    private Throwable fCause;
}
