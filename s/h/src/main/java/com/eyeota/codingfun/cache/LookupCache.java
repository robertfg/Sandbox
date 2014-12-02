package com.eyeota.codingfun.cache;

/**
 * Interface to be implemented by your solution
 */
public interface LookupCache {
    public SegmentConfig[] getSegmentFor(final String orgKey, final String paramKey);
    public SegmentConfig[] getSegmentFor(final String orgKey, final String paramKey, final String paramValKey);
}
