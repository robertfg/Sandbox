package sorting.polyphase.kway;

/**
 * This is the interface to each of the buckets being merged
 *
 * @author Jordan Zimmerman (jordan@jordanzimmerman.com)
 * @version 1.1 Dec. 1, 2007 made a top level class
 */
public interface KWayMergeIterator<T extends KWayMergeIterator<T>>
{
    /**
     * Return true when this bucket is empty (NOTE: KWayMerge will call this multiple times so it's
     * best to have some sort of "done" flag)
     *
     * @return true/false
     * @throws KWayMergeError any errors
     */
    public boolean              isDone() throws KWayMergeError;

    /**
     * Advance to the next item in the bucket. The iterator starts at one before the first item, so this will
     * get called to move to the first item, etc. This method must be able to handle being called after {@link #isDone} returns
     * true.
     *
     * @throws KWayMergeError any errors
     */
    public void 			    advance() throws KWayMergeError;
                                          
    /**
     * Compare the current top item in this bucket to the top item in another bucket. Return the iterator with the
     * winning comparison (i.e. the smallest or whatever comparison is needed).
     *
     * @param iterator bucket to compare to
     * @return the winning bucket
     * @throws KWayMergeError any errors
     */
    public T                    compare(T iterator) throws KWayMergeError;
}
