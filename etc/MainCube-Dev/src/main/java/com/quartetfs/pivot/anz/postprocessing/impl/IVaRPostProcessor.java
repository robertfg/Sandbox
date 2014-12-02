/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.Arrays;
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
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.pivot.anz.utils.ANZConstants;

/**
 * Incremental VaR PostProcessor
 * get the value from the location vector that is in the same index as the parent VaR index
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName="com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key=IVaRPostProcessor.PLUGIN_KEY)
public class IVaRPostProcessor extends AParentAndSelfPostProcessor<Double> {
	private static final long serialVersionUID = 6071939889209042326L;
	public final static String PLUGIN_KEY = "IVAR";
	
	public IVaRPostProcessor(String name, IActivePivot pivot) {
		super(name, pivot);
	}

	@Override
	public void init(Properties properties) throws QuartetException {
		super.init(properties);
		
	}

	@Override
	protected Collection<String> getParentMeasures() {
		return Arrays.asList(underlyingMeasures[0]);
	}

	@Override
	protected Collection<String> getSelfMeasures() {
		return Collections.singleton(underlyingMeasures[0]);
	}

	@Override
	public void evaluate(ILocation location, final IAggregatesRetriever retriever) throws QuartetException {
		//1- 
		//if we have DEFAULT_DISCRIMINATOR we skip as no detailed value at the default member
		final Object discriminator = location.getCoordinate(firstDiscriminatorIndices[0], firstDiscriminatorIndices[1]);
		if(ANZConstants.DEFAULT_DISCRIMINATOR.equals(discriminator)) { 
			return; 
		}

		//2- 
		//use the location to prefetch, the overridden location focus on DEFAULT member for available analysis dimension
		final ILocation locationToQuery = overrideLocation(location);

		//3-
		//get the parent from the overridden location
		final ILocation parent = parentLocation(locationToQuery);
		if(parent == null)
			return;

		//4- 
		//populate the confidence levels  from the available members
		final Set<Double> confidenceLevels = new HashSet<Double>();

		@SuppressWarnings("unchecked")
		final List<IAxisMember> members = (List<IAxisMember>) pivot.getDimensions().get(firstDiscriminatorIndices[0] + 1).retrieveMembers(LocationUtil.copyPath(location, firstDiscriminatorIndices[0]));
		for(IAxisMember m : members) {
			if(m.getDiscriminator() instanceof Double)
				confidenceLevels.add((Double) m.getDiscriminator());
		}

		//5-
		//check if the dimension is a wildcard dimension on our analysis dimension
		//that means if we're watching one confidence levels or all
		final ILocationPattern pattern = location.createWildCardPattern();
		final int index = pattern.getPatternIndex(firstDiscriminatorIndices[0], firstDiscriminatorIndices[1]);
		final double uniqueConfidenceLevel;
		if(index < 0) {//we don't have wildcard on the confidence level, we received here a confidence level value as a member
			if(confidenceLevels.size() != 1) throw new IllegalStateException();//should be one confidence level
			uniqueConfidenceLevel = confidenceLevels.iterator().next();//get the confidence level we're looking at
		} else
			uniqueConfidenceLevel = -1;


		//6- 
		//loop over the cellSet, notice that we use the locationToQuery with the DEFAULT_DISCRIMINATOR as we need to retrieve the current vector and the parent vector
		final ICellSet parentCellSet = retriever.retrieveAggregates(Collections.singleton(parent), Arrays.asList(underlyingMeasures[0]));

		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singleton(locationToQuery), Collections.singleton(underlyingMeasures[0]));
		cellSet.forEachCell(new ICellProcedure() {
			@Override
			public boolean execute(final ILocation location,String measure, Object value) {
				//get the parent location and its associated vectors based on prefetched measures
				final ILocation lparent = parentLocation(location);
				if(lparent == null)
					return true;
				final Object parentVectorValue = parentCellSet.getCellValue(lparent, underlyingMeasures[0]);

				if(null == parentVectorValue){
					throw new QuartetRuntimeException("Unexpected parent measure value: " + parentVectorValue);
				}

				//get the location vector
				if(index >= 0) {//we received confidence level wildcard location here
					final Object[] confidenceLevelTuple = pattern.extractValues(location);//extract the variable tuple that will allow us to generate the location to write
					//loop over the confidenceLevels and generate the sublocation with the correct value taken from the vector
					for(Double confidenceLevel : confidenceLevels) {
						final Object[] confidenceLevelTupleCloned = confidenceLevelTuple.clone();//clone
						confidenceLevelTupleCloned[index] = confidenceLevel.doubleValue();//set the confidence level value
						ILocation locationToWrite = pattern.generate(confidenceLevelTupleCloned);//generate the associated location
						//compute the incremental VaR for that generated location
						incrementalComputation(value,parentVectorValue, confidenceLevel.doubleValue(), retriever, locationToWrite);
					}
				} else {//we received one confidence level here, no wildcard
					final Object[][] locationArray = location.arrayCopy();
					locationArray[firstDiscriminatorIndices[0]][firstDiscriminatorIndices[1]] = uniqueConfidenceLevel;//set the confidence level instead of the DEFAULT_DISCRIMINATOR
					ILocation locationToWrite = new Location(locationArray); //create the location we'll write in the retriever
					//compute the incremental VaR for that generated location
					incrementalComputation(value, parentVectorValue, uniqueConfidenceLevel, retriever, locationToWrite);
				}
				return true;
			}
		});
	}

	private void incrementalComputation(final Object locationVectorValue, 
			final Object parentVectorValue, final double confidenceLevel, final IAggregatesRetriever retriever, final ILocation locationToWrite){
		if(!(parentVectorValue instanceof double[]))
			throw new QuartetRuntimeException("Unexpected parent measure value: " + parentVectorValue);

		if(!(locationVectorValue instanceof double[]))
			throw new QuartetRuntimeException("Unexpected parent measure value: " + locationVectorValue);

		final double[] locationVector = (double[]) locationVectorValue;//the location vector
		final double[] parentVector = (double[])parentVectorValue;//the parent vector
		
		//populate the Pair[] array with parentVector, we preserve the orginal index after sorting Pair[] array
		Pair[] parentVectorPair = new Pair[parentVector.length];
		for (int i=0;i<parentVector.length;i++){
			parentVectorPair[i]=new Pair(i,parentVector[i]);
		}
		Arrays.sort(parentVectorPair);
		
		//get the index for the current confidence level
		final Pair parentVaRPair = parentVectorPair[VaRHelper.getIndexFromVectorLength(parentVector.length, confidenceLevel)];
		
		//get the value from the location vector that is in the same index as the parent VaR index
		retriever.write(locationToWrite, locationVector[parentVaRPair.getIndex()]); 

	}
	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

	/**
	 * 
	 * Pair class used to preserve the original index after sorting
	 *
	 */
	private class Pair implements Comparable<Pair> {
	    protected final double pnl;
	    protected final int index;
	    protected Pair(int index, double pnl) {
	        this.index = index;
	        this.pnl = pnl;
	    }
	    public int compareTo(Pair other) {
	        return Double.compare(pnl, other.pnl);
	    }

		public int getIndex() {
			return index;
		}
	}
}
