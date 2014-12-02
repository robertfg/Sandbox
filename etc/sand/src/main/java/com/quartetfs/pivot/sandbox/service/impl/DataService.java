/*
 * (C) Quartet FS 2013
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.service.impl;

import java.util.Properties;

import com.quartetfs.pivot.sandbox.service.IDataService;
import com.quartetfs.pivot.sandbox.source.impl.TradeSource;

/**
 * 
 * Data service implementation.
 * 
 * @author Quartet FS
 *
 */
public class DataService implements IDataService {

	/** Data source */
	protected final TradeSource source;

	/** Properties of the data messages */
	protected final Properties properties;

	public DataService(TradeSource source) {
		this.source = source;
		this.properties = new Properties();
		this.properties.setProperty(TradeSource.SYNCHRONOUS_UPDATE_PROPERTY, "true");
	}

	@Override
	public void sendTradeUpdate(int nbNew, int nbUpdates) {
		this.source.sendTradeUpdate(nbNew, nbUpdates, properties);
	}

}