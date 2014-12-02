/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.model.impl;

/**
 * 
 * Pair class used to preserve the original index after sorting
 *
 */
public class Pair implements Comparable<Pair> {
    protected final double pnl;
    protected final int index;
    public Pair(int index, double pnl) {
        this.index = index;
        this.pnl = pnl;
    }
    public int compareTo(Pair other) {
        return Double.compare(pnl, other.pnl);
    }

	public int getOriginalIndex() {
		return index;
	}
}
