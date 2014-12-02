/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.postprocessor.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.context.subcube.ISubCubeProperties;
import com.quartetfs.biz.pivot.postprocessing.impl.ADynamicAggregationPostProcessor;
import com.quartetfs.biz.pivot.query.IQueryCache;
import com.quartetfs.biz.pivot.query.aggregates.impl.StoredMeasureHandler;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.sandbox.context.IReferenceCurrency;
import com.quartetfs.pivot.sandbox.impl.MessagesSandbox;
import com.quartetfs.pivot.sandbox.service.impl.ForexService;
import com.quartetfs.tech.type.IDataType;
import com.quartetfs.tech.type.impl.DoubleDataType;

/**
 * <b>ContinuousForexPostProcessor</b> 
 * 
 * extends for the abstract class ADynamicAggregationPostProcessor.
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = ContinuousForexPostProcessor.PLUGIN_KEY)
public class ContinuousForexPostProcessor extends ADynamicAggregationPostProcessor<Double> {

	/** serialVersionUID */
	private static final long serialVersionUID = 15874126988574L;
	
	/** the logger */
	private static Logger logger = Logger.getLogger(MessagesSandbox.LOGGER_NAME, MessagesSandbox.BUNDLE);

	/** post processor plugin key */
	public final static String PLUGIN_KEY = "CONTINUOUS_FOREX";

	/** post processor return type */
	private static final IDataType<Double> DATA_TYPE = new DoubleDataType();

	/** currency level ordinals */
	private int[] currencyOrdinals = null;

	/** forex service*/
	private ForexService forexService = null;

	/** constructor */
	public ContinuousForexPostProcessor(String name, IActivePivot pivot) {
		super(name, pivot);
	}

	/** ForexService setter to allow injection in Spring XML file*/
	public void setForexService(ForexService forexService) {
		this.forexService = forexService;
	}

	/** post processor initialisation */
	@Override
	public  void init(Properties properties) throws QuartetException {
		super.init(properties);

		// init required level values
		// first element of currencyOrdinals array is the dimension ordinal
		// second element of currencyOrdinals array is the level ordinal
		currencyOrdinals = this.leafLevelsOrdinals.get(0);


		// Here, we define any extra context dependencies. When a post processor depends on
		// a context value, its evaluation may differ from one query to the next, depending on
		// that value in the scope of the query.
		//
		// Only post processors that depend on the same value of a context value
		// can share some of their processing during query evaluation.

		// add SubCubeProperties to the context dependencies
		addContextDependency(ISubCubeProperties.class);
		// add ReferenceCurrency ContextValue to the context dependencies.
		// mandatory to access the ReferenceCurrency context
		addContextDependency(IReferenceCurrency.class);
	}

	static final AtomicBoolean once = new AtomicBoolean(true);
	
	/**
	 * Perform the evaluation of the post processor on a leaf (as defined in the properties).
	 * Here the leaf level is the UnderlierCurrency level in the Underlyings dimension .
	 */
	@Override
	protected Double doLeafEvaluation(ILocation leafLocation, Object[] underlyingMeasures) throws QuartetException {

		//retrieve the currency
		String currency = (String)leafLocation.getCoordinate(currencyOrdinals[0]-1, currencyOrdinals[1]);

		//retrieve the measure in the native currency
		double measureNative = (Double)underlyingMeasures[0];

		// retrieve the contextual reference currency
		// the class implementing IReferenceCurrency must be added in the context dependencies
		// (see getContextDependencies() method)
		IReferenceCurrency referenceCurrency = pivot.getContext().get(IReferenceCurrency.class);
		if(referenceCurrency == null) {
			logger.log(Level.SEVERE, MessagesSandbox.FOREX_PP_CONTEXT_NOT_SET);
			return Double.NaN;
		}

		String refCurrency = referenceCurrency.getCurrency(); 

		// System.out.println("Post processor reference currency: "+refCurrency);

		// if currency is reference currency or measureNative is equal to 0.0 no need to convert
		if ((currency.equals(refCurrency)) || (measureNative == .0) )
			return measureNative;

		// retrieve the rate and rely on the IQueryCache
		// in order to retrieve the same rate for the same currency for our query
		IQueryCache queryCache = pivot.getContext().get(IQueryCache.class);
        Double rate = (Double) queryCache.get(currency);
        if(rate == null) {
        	Double rateRetrieved = forexService.retrieveQuotation(currency, refCurrency);
        	Double rateCached = (Double) queryCache.putIfAbsent(currency, rateRetrieved);
            rate = rateCached == null ? rateRetrieved : rateCached;
        }
		
		//compute equivalent in reference currency
		return  rate == null ? measureNative :  measureNative * rate;
	}

	/**
	 * Returns handlers' plugin keys to use for processing a "GET_AGGREGATES" continuous query.
	 * 
	 * @return handlers' plugin keys to use for processing a "GET_AGGREGATES" continuous query.
	 * @see IContinuousQueryHandler
	 */
	@Override
	public List<String> getContinuousQueryHandlerKeys() {
		//set here the handlers to which the post processor is sensitive:
		// - StoredMeasureHandler.PLUGIN_TYPE ("STORED"):
		//   all ActivePivot's transactions (added /removed objects), this is a native handler
		// - ForexHandler.PLUGIN_KEY ("CONTINUOUS_FOREX_HANDLER"):
		//   Forex quotations changes, this is the bespoke handler implemented in the sandbox
		//
		//Those handlers can also be set in the cube definition XML file by adding the 
		//following entry in the post processor properties:
		//<entry key="continuousQueryHandlerKeys" value="STORED, CONTINUOUS_FOREX_HANDLER" />
		return Arrays.asList(StoredMeasureHandler.PLUGIN_TYPE, ForexHandler.PLUGIN_KEY);
	}
	
	/**
	 * @return the data type of the post processed values
	 * that must be dynamically aggregated.
	 */
	@Override
	protected IDataType<Double> getDataType() { return DATA_TYPE; }

	/**
	 * @return the type of this post processor, within the post processor extended plugin.
	 */
	@Override
	public String getType() { return PLUGIN_KEY; }
}
