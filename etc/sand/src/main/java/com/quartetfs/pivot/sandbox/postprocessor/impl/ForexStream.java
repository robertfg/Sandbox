/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.postprocessor.impl;

import java.util.Collection;

import com.quartetfs.biz.pivot.IActivePivotSession;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesContinuousQueryEngine;
import com.quartetfs.biz.pivot.query.aggregates.impl.AStream;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.sandbox.service.IForexServiceListener;
import com.quartetfs.pivot.sandbox.service.impl.ForexService;

/**
 * <b>ForexStream</b> 
 * 
 * extends for the abstract class AStream.
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.query.aggregates.IStream", key = ForexStream.PLUGIN_KEY)
public class ForexStream extends AStream<Object> implements IForexServiceListener {

	/** serialVersionUID */
	private static final long serialVersionUID = -8004742130808337138L;

	/** plugin key */
	public static final String PLUGIN_KEY = "CONTINUOUS_FOREX_STREAM";

	/** forex service */
	private ForexService forexService;

	/** constructor */
	public ForexStream(IAggregatesContinuousQueryEngine engine,IActivePivotSession session) {
		super(engine, session);
	}
	
	/** ForexService setter to allow injection in Spring XML file*/
	public void setForexService(ForexService forexService) {
		this.forexService = forexService;
	}
	
	/** Enable/activate the stream.
	 *  Method called by the AggregateContinuousQueryEngine when a continuous query is registered
	 */
	@Override
	public void enable() {
		super.enable();
		//add Forex Service to the listeners 
		forexService.addListener(this);
	}
	
	/** Disable/deactivate the stream.  
	 *  Method called by the AggregateContinuousQueryEngine when a continuous query is unregistered
	 */
	@Override
	public void disable() {
		super.disable();
		//remove Forex Service from the listeners 
		forexService.removeListener(this);
	}
	
    /**
     * Upon arrival of a message, the source listener is being called with this method.
     * @param currencies
     */
	@Override
	public void onQuotationUpdate(Collection<String> currencies) {
		//if stream is enabled, send the set of updated currencies to the Forex handler
		if(getIsEnabled())
			sendEvent(currencies);
	}

	/** 
	 * class of the event sent by ForexStream
	 */
	@Override
	public Class<Object> getEventType() {
		return Object.class;
	}

	/**
	 * Being a Plugin, it returns the Type it is attached to.
	 */
	@Override
	public String getType() {
		return PLUGIN_KEY;
	}
	
}
