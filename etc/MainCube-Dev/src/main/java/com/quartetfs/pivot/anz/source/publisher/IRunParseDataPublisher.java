/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.source.publisher;



import com.quartetfs.pivot.anz.service.impl.PSRDetail.PublisherType;
import com.quartetfs.pivot.anz.source.IRunParseData;

/**
 * Publisher for run parse data
 * 
 * @author Quartet Financial Systems
 */
public interface IRunParseDataPublisher {

	/**
	 * Publisher name
	 * 
	 * @return the name
	 */
	String getName();

	/**
	 * Returns true if handled
	 * 
	 * @param runParseData
	 *            data to publish
	 * @return true if the data is published
	 */
	boolean publish(IRunParseData runParseData) throws Throwable;
	
	public PublisherType getType();
	
}
