package eye;

/**
 * Interface to be implemented by your solution
 */
public interface LookupCache {
    public SegmentConfig[] getSegmentFor(final String orgKey, final String paramKey);
    public SegmentConfig[] getSegmentFor(final String orgKey, final String paramKey, final String paramValKey);
}
