/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.postprocessor.impl;

import static com.quartetfs.fwk.util.MessageUtil.logRecord;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.context.IContext;
import com.quartetfs.biz.pivot.impl.LocationDiscriminator;
import com.quartetfs.biz.pivot.query.aggregates.IImpact;
import com.quartetfs.biz.pivot.query.aggregates.impl.AAggregatesContinuousHandler;
import com.quartetfs.biz.pivot.query.aggregates.impl.Impact;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.sandbox.context.IReferenceCurrency;
import com.quartetfs.pivot.sandbox.impl.MessagesSandbox;

/**
 * <b>ForexHandler</b> 
 * 
 * extends for the abstract class AAggregatesContinuousHandler.
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.query.aggregates.IAggregatesContinuousHandler", key = ForexHandler.PLUGIN_KEY)
public class ForexHandler extends AAggregatesContinuousHandler<Object> {

	/** serialVersionUID */
	private static final long serialVersionUID = -5399980219497341596L;

	/** the logger */
	private static Logger logger = Logger.getLogger(MessagesSandbox.LOGGER_NAME, MessagesSandbox.BUNDLE);

	/** plugin key */
	public static final String PLUGIN_KEY = "CONTINUOUS_FOREX_HANDLER";

	/** field for the location discriminator */
	private String currencyLevel;

	/** constructor */
	public ForexHandler(IActivePivot pivot) {
		super(pivot);
	}

	/** setter to allow injection of currency level in Spring XML file*/
	public void setCurrencyLevel(String currencyLevel) {
		this.currencyLevel = currencyLevel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IImpact computeImpact(ILocation location, Object event) {		
		//get the ReferenceCurrency context
		IContext currentContext = pivot.getContext();
		IReferenceCurrency referenceCurrencyCtx = currentContext.get(IReferenceCurrency.class);
		if(referenceCurrencyCtx == null) {
			logger.log(Level.SEVERE, MessagesSandbox.FOREX_HANDLER_CONTEXT_NOT_SET);
		}

		// get the reference currency
		String referenceCurrency = referenceCurrencyCtx.getCurrency();
		
		// Debug logging.
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Handler reference currency: " + referenceCurrency);
		}

		//get the updated currencies from the Forex Stream
		if (!(event instanceof Set))
			return new Impact(location, null, null);
		Set<String> updatedCurrencies = (Set<String>) event; // != null

		// determine the impacted currencies in the location
		if(!updatedCurrencies.contains(referenceCurrency)) {
			// if the reference currency is not one of the updated currencies,
			// there is no impact on the leaf level aggregation
			return new Impact(location, null, null);
		}

		// instantiate location discriminator with the current location and the UnderlierCurrency field
		LocationDiscriminator locationDiscriminator = new LocationDiscriminator(Collections.singleton(location), Collections.singleton(currencyLevel), pivot); 

		// set the currency values of the impacts to the impacted currencies values  
		Collection<Map<String, Object>> discriminators = new HashSet<Map<String, Object>>();
		for(String currency : updatedCurrencies) {
			if (currency.equals(referenceCurrency)) {
				// skip the reference currency since the aggregates expressed in the
				// reference currency are not impacted
				continue;
			}
			Map<String,Object> discriminator = new HashMap<String, Object>(1);
			discriminator.put(currencyLevel, currency);
			discriminators.add(discriminator);
		}	

		//compute the impacted locations
		Collection<ILocation> impactedLocs = null;
		try {
			impactedLocs = locationDiscriminator.discriminate(discriminators);
		} catch (QuartetException e) {
			logger.log(logRecord(Level.WARNING, e, MessagesSandbox.BUNDLE, MessagesSandbox.EXC_COMPUTING_IMPACTED_LOCATION));
		}

		//return the impacted location
		return new Impact(location, (Set<ILocation>) impactedLocs, null);
	}

	/**
	 * @return the identifier of the stream associated with this handler.
	 */
	@Override
	public String getStreamKey() { return ForexStream.PLUGIN_KEY; }

	/**
	 * @return the type identifying this plugin value.
	 */
	@Override
	public String getType() { return  PLUGIN_KEY; }

}
