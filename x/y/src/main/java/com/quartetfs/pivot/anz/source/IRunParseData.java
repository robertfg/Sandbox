/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.source;


import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.quartetfs.pivot.anz.model.IVRParsingEntry;
import com.quartetfs.pivot.anz.model.impl.Deal;

/**
 * Repository for parse results of the current PSR parsing
 * 
 * @author Quartet Financial Systems
 */
public interface IRunParseData{

	/**
	 * Name of the currently parsed PSR
	 */
	String getPSRName();

	/**
	 * Container name of the currently parsed PSR
	 */
	String getContainerName();

	/**
	 * Contribute a sub-list of vr1 entries - Thread safe
	 * 
	 * @param list
	 *            The list to read from
	 * @param from
	 *            Index of the first object to read in the list (included)
	 * @param toExcluded
	 *            Index of the last object to read in the list (excluded)
	 * @throws Exception 
	 */
	void contribute(List<IVRParsingEntry> list, int from, int toExcluded) throws Exception;

	//void complete();
	/**
	 * Get the number of parsed entries (valid after the parsing is done the
	 * keys & values completed)
	 * 
	 * @return the number of parsed values
	 */
	int getValueCount();
	
	/**
	 * retrieve the deals
	 * @return
	 */
	Collection<Deal> deals();
	
	public Set<String> mergedPSR();
}
