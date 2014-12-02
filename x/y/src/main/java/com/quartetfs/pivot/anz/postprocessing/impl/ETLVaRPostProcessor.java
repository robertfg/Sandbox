/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.ILocationPattern;
import com.quartetfs.biz.pivot.cellset.ICellProcedure;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.IAxisMember;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.pivot.anz.utils.ANZConstants;

/**
 * Estimated tail lost post-processor
 * 
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = ETLVaRPostProcessor.PLUGIN_KEY)
public class ETLVaRPostProcessor extends APostProcessor<Double> implements
IPrefetcher {

	private static final long serialVersionUID = 7352103338886022207L;
	public final static String PLUGIN_KEY = "ETL";

	// used to store the dim and level indexes we focus on
	protected int[] firstDiscriminatorIndices;
	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();
	

	public ETLVaRPostProcessor(final String name, final IActivePivot pivot) {
		super(name, pivot);
	}

	@Override
	public void init(final Properties properties) throws QuartetException {
		super.init(properties);

		getPrefetchers().addAll( Collections.<IPrefetcher> singletonList(this) );
		analysisDimensionHelper.init(properties, pivot);
		firstDiscriminatorIndices=analysisDimensionHelper.getFirstDiscriminatorIndices().get(0);
				
	}

	@Override
	public void evaluate(final ILocation location,
			final IAggregatesRetriever retriever) throws QuartetException {
		// 1-
		// if we have DEFAULT_DISCRIMINATOR we skip as no detailed value at the
		// default member
		final Object discriminator = location.getCoordinate(
				firstDiscriminatorIndices[0], firstDiscriminatorIndices[1]);
		if (ANZConstants.DEFAULT_DISCRIMINATOR.equals(discriminator)) {
			return;
		}

		// 2-
		// use the location to prefetch, the overridden location focus on
		// DEFAULT member for available analysis dimension
		final ILocation locationToQuery = overrideLocation(location);

		// 3-
		// populate the confidence levels from the available members
		final Set<Double> confidenceLevels = new HashSet<Double>();

		@SuppressWarnings("unchecked")
		final List<IAxisMember> members = (List<IAxisMember>) pivot
		.getDimensions()
		.get(firstDiscriminatorIndices[0] + 1)
		.retrieveMembers(
				LocationUtil.copyPath(location,
						firstDiscriminatorIndices[0]));
		for (final IAxisMember m : members) {
			if (m.getDiscriminator() instanceof Double) {
				confidenceLevels.add((Double) m.getDiscriminator());
			}
		}

		// 4-
		// check if the dimension is a wildcard dimension on our analysis
		// dimension
		// that means if we're watching one confidence levels or all
		final ILocationPattern pattern = location.createWildCardPattern();
		final int index = pattern.getPatternIndex(firstDiscriminatorIndices[0],
				firstDiscriminatorIndices[1]);
		final double uniqueConfidenceLevel;
		if (index < 0) {
			// we don't have wildcard on the confidence level, we received here
			// a confidence level value as a member
			if (confidenceLevels.size() != 1) {
				// should be one confidence level
				throw new IllegalStateException();
			}
			// get the confidence level we're looking at
			uniqueConfidenceLevel = confidenceLevels.iterator().next();
		} else {
			uniqueConfidenceLevel = -1;
		}

		// 5-
		// loop over the cellSet, notice that we use the locationToQuery with
		// the DEFAULT_DISCRIMINATOR as we need to retrieve the current sorted
		// vector for the overriden location
		final ICellSet cellSet = retriever.retrieveAggregates(
				Collections.singleton(locationToQuery),
				Collections.singleton(underlyingMeasures[0]));
		cellSet.forEachCell(new ICellProcedure() {
			@Override
			public boolean execute(final ILocation location,
					final String measure, final Object value) {
				// get the location vector
				if (index >= 0) {// we received confidence level wildcard
					// location here
					// extract the variable tuple that will allow us to generate
					// the location to write
					final Object[] confidenceLevelTuple = pattern.extractValues(location);
					// loop over the confidenceLevels and generate the
					// sublocation with the correct value taken from the vector
					for (final Double confidenceLevel : confidenceLevels) {
						final Object[] confidenceLevelTupleCloned = confidenceLevelTuple
						.clone();// clone
						confidenceLevelTupleCloned[index] = confidenceLevel
						.doubleValue();// set the confidence level value
						// generate the associated location
						final ILocation locationToWrite = pattern
						.generate(confidenceLevelTupleCloned);
						// compute the ETL VaR for that generated location
						etlComputation(value, confidenceLevel.doubleValue(),
								retriever, locationToWrite);
					}
				} else {// we received one confidence level here, no wildcard
					final Object[][] locationArray = location.arrayCopy();
					// set the confidence level instead of the
					// DEFAULT_DISCRIMINATOR
					locationArray[firstDiscriminatorIndices[0]][firstDiscriminatorIndices[1]] = uniqueConfidenceLevel;
					final ILocation locationToWrite = new Location(
							locationArray); // create the location we'll write
					// in the retriever
					// compute the ETL VaR for that generated location
					etlComputation(value, uniqueConfidenceLevel, retriever,
							locationToWrite);
				}
				return true;
			}
		});
	}

	protected ILocation overrideLocation(final ILocation location) {
		return analysisDimensionHelper.overrideLocation(location);
	}

	private void etlComputation(final Object locationSortedVectorValue,
			final double confidenceLevel, final IAggregatesRetriever retriever,
			final ILocation locationToWrite) {
		if (!(locationSortedVectorValue instanceof double[])) {
			throw new QuartetRuntimeException(
					"Unexpected parent measure value: "
					+ locationSortedVectorValue);
		}

		final double[] locationSortedVector = (double[]) locationSortedVectorValue;

		int count = 0;
		double sum = 0;
		for (int i = VaRHelper.getIndexFromVectorLength(locationSortedVector.length, confidenceLevel); i >= 0; i--) {
			count++;
			sum += locationSortedVector[i];
		}
		retriever.write(locationToWrite, sum / count);
	}

	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

	@Override
	public Collection<ILocation> computeLocations(
			final Collection<ILocation> locations) {
		final Set<ILocation> locs = new HashSet<ILocation>();
		for (final ILocation l : locations) {
			locs.add(overrideLocation(l));
		}
		return locs;
	}

	@Override
	public Collection<String> computeMeasures(
			final Collection<ILocation> locations) {
		return Collections.singleton(underlyingMeasures[0]);
	}

}
