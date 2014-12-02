/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.postprocessor.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellProcedure;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.context.subcube.ISubCubeProperties;
import com.quartetfs.biz.pivot.impl.Util;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.postprocessing.impl.UnderlyingMeasuresPrefetcher;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

/**
 * <b>AllMemberHidePostProcessor</b> 
 * 
 * extends for the abstract class APostProcessor.
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = AllMemberHidePostProcessor.PLUGIN_KEY)
public class AllMemberHidePostProcessor extends APostProcessor<Object> {

	/** serial version uid for the class. */
	private static final long serialVersionUID = 1L;

	/** AllMemberHidePostProcessor plugin key */
	public static final String PLUGIN_KEY = "ALLMEMBER_HIDE";

	/** dimension and level ordinals of UnderlierCurrency level */
	protected int[] currencyOrdinals = null;

	/** constructor */
	public AllMemberHidePostProcessor(String name, IActivePivot pivot) {
		super(name, pivot);
	}

	@Override
	public void init(Properties properties) throws QuartetException {
		super.init(properties);

		// init required dimension and level values
		String currencyDimensionName = properties.getProperty("dimension",
				"Underlyings");
		String currencyLevelName = properties.getProperty("level",
				"UnderlierCurrency");

		// retrieve dimension and level ordinals of UnderlierCurrency level
		currencyOrdinals = Util.findLevelOrdinals(pivot.getDimensions(),
				currencyDimensionName, currencyLevelName);
	}
	
	/**
	 * Get the prefetchers associated with that post-processor. By exposing its prefetchers
	 * the post processor allows the query engine to efficiently prefetch all aggregates
	 * for a post processor chain in a single pass.
	 */
	@Override
	public List<IPrefetcher> getPrefetchers() {
		//set the underlying measure of the post processor as prefetched measure
		//if you see a warning similar to the following:
		//
		//WARNING: The following aggregate retrieval chain was not prefetched, a post processor does not properly expose its prefetch directives:
		//--> pnl.ALLMEMBER_HIDE @ AllMember\[*]|AllMember|AllMember|AllMember|AllMember|13/08/10
		//--> [ pnl.SUM ] @ AllMember\[*]|AllMember|AllMember|AllMember|AllMember|13/08/10
		//
		//it means that the aggregates prefectching is not set properly
		return Arrays.asList(new IPrefetcher[]{new UnderlyingMeasuresPrefetcher(prefetchMeasures[0])});
	}
	
	@Override
	public void evaluate(ILocation location, final IAggregatesRetriever retriever) throws QuartetException {

		// retrieve UnderlierCurrency level depth in Underlyings dimension in
		// location
		int currencyDepth = location.getLevelDepth(currencyOrdinals[0] - 1);

		// if current location level depth in dimension Underlyings is lower
		// than UnderlierCurrency depth, do no aggregate measure
		if (currencyDepth <= currencyOrdinals[1])
			return;

		// if current location level depth in dimension Underlyings is higher
		// than UnderlierCurrency depth, retrieve the aggregates

		// the retriever handles retrieval of aggregates in the cube
		ICellSet cellSet = retriever.retrieveAggregates(Collections.singleton(location), Collections.singleton(prefetchMeasures[0]));
		
		// re-copy the values using a procedure
		cellSet.forEachCell(new ICellProcedure() {
			@Override
			public boolean execute(ILocation location, String measure, Object value) {
				retriever.write(location, value);
				return true;
			}
		});
	}

	/**
	 * A post processor exposes its context dependencies. When a post processor depends on
	 * a context value, its evaluation may differ from one query to the next, depending on
	 * that value in the scope of the query.
	 * <BR>
	 * Only post processors that depend on the same value of a context value
	 * can share some of their processing during query evaluation.
	 * 
	 * @return context dependencies
	 */
	@Override
	public Set<Class<? extends IContextValue>> getContextDependencies() {
		Set<Class<? extends IContextValue>> contextsSet = super.getContextDependencies();
		
		// Add SubCubeProperties context to the context dependencies
		contextsSet.add(ISubCubeProperties.class);
		
		// add ReferenceCurrency ContextValue to the context dependencies.
		// mandatory to access the ReferenceCurrency context
//		contextsSet.add(IReferenceCurrency.class);
		return contextsSet;
	}

	/**
	 * Being a Plugin, it returns the Type it is attached to.
	 */
	@Override
	public String getType() { return PLUGIN_KEY; }

}
