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
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.IAxisMember;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.ANZUtils;


/**
 * VaRConfidenceLevelPostProcessor
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName="com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key=VaRConfidenceLevelPostProcessor.PLUGIN_KEY)
public class VaRConfidenceLevelPostProcessor extends APostProcessor<Object> implements IPrefetcher {

	private static final long serialVersionUID = 6850960025651709535L;

	public final static String PLUGIN_KEY = "VAR_CONFIDENCE_LEVEL_PP";
	//used to store the dim and level indexes we focus on
	protected int[] firstDiscriminatorIndices;
	
	protected int[] otherDiscriminatorIndices;
	
	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();

	private String containerName;
	private IDimension dimContainerName;
	private int containerNameOrdinal;
	
	
	public VaRConfidenceLevelPostProcessor(String name, IActivePivot pivot) {
		super(name, pivot);
	}

	@Override
	public void init(Properties properties) throws QuartetException {
		super.init(properties);
		getPrefetchers().addAll( Collections.<IPrefetcher> singletonList(this) );
		analysisDimensionHelper.init(properties, pivot);
		
		firstDiscriminatorIndices=analysisDimensionHelper.getFirstDiscriminatorIndices().get(0);
		
		containerName=(String)properties.get(ANZConstants.LABEL_CONTAINER);
		
		
		
		dimContainerName = ANZUtils.findDimensionByName(pivot, (String) properties.get( "containerDim"  ));
		containerNameOrdinal     = dimContainerName.getOrdinal() -1;
	}

	@Override
	public void evaluate(ILocation locationReceived, final IAggregatesRetriever retriever) throws QuartetException {
		//1- 
		//if we have DEFAULT_DISCRIMINATOR we skip as no detailed value at the default member
		final Object discriminator = locationReceived.getCoordinate(firstDiscriminatorIndices[0], firstDiscriminatorIndices[1]);
		if(ANZConstants.DEFAULT_DISCRIMINATOR.equals(discriminator)) { 
			return; 
		}

		//2- 
		//populate the confidence levels  from the available members
		final Set<Double> confidenceLevels = new HashSet<Double>();

		@SuppressWarnings("unchecked")
		final List<IAxisMember> members = (List<IAxisMember>) pivot.getDimensions().get(firstDiscriminatorIndices[0] + 1).retrieveMembers(LocationUtil.copyPath(locationReceived, firstDiscriminatorIndices[0]));
		for(IAxisMember m : members) {
			if(m.getDiscriminator() instanceof Double)
				confidenceLevels.add((Double) m.getDiscriminator());
		}
				
		//3- 
		//use the location to prefetch
		final ILocation locationToQuery = overrideLocation(locationReceived);

		final ILocationPattern pattern = locationReceived.createWildCardPattern();
		final int index = pattern.getPatternIndex(firstDiscriminatorIndices[0], firstDiscriminatorIndices[1]);
		final double uniqueConfidenceLevel;
		if(index < 0) {//we don't have wildcard on the confidence level, we received here a confidence level value as a member
			if(confidenceLevels.size() != 1) throw new IllegalStateException();//should be one confidence level
			uniqueConfidenceLevel = confidenceLevels.iterator().next();//get the confidence level we're looking at
		} else
			uniqueConfidenceLevel = -1;


		//4- 
		//loop over the cellSet, notice that we use the locationToQuery with the DEFAULT_DISCRIMINATOR as we need to retrieve the vector
		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singleton(locationToQuery), Arrays.asList(underlyingMeasures));
		cellSet.forEachLocation(new ILocationProcedure() {
			@Override
			public boolean execute(final ILocation location, int rowId) {
				final Object measure = cellSet.getCellValue(rowId, underlyingMeasures[0]);
				if(measure instanceof double[]) {
					final double[] vector = (double[]) measure;//get the vector
					if(index >= 0) {//we received wildcard location here
						final Object[] confidenceLevelTuple = pattern.extractValues(location);
						for(Double confidenceLevel : confidenceLevels) {//loop over the confidenceLevels and generate the sublocation with the correct value taken from the vector
							final Object[] confidenceLevelTupleCloned = confidenceLevelTuple.clone();
							confidenceLevelTupleCloned[index] = confidenceLevel;
							retriever.write(pattern.generate(confidenceLevelTupleCloned), getObjectToWrite(vector,confidenceLevel.doubleValue(),cellSet,rowId));
						}
					} else {//we received point location here
						final Object[][] locationArray = location.arrayCopy();
						locationArray[firstDiscriminatorIndices[0]][firstDiscriminatorIndices[1]] = uniqueConfidenceLevel;//set the confidence level instead of the DEFAULT_DISCRIMINATOR
						retriever.write(new Location(locationArray), getObjectToWrite(vector,uniqueConfidenceLevel,cellSet,rowId)); //put the correct value taken from the vector that matches the confidence level
					}
				} 
				return true;
			}
		});
	}
	
	
	protected Object getObjectToWrite(double vector[], double confidenceLevel, ICellSet cellSet, int rowId)
	{
			return vector[VaRHelper.getIndexFromVectorLength(vector.length, confidenceLevel)];
	}
	
	

	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

	/* (non-Javadoc)
	 * @see com.quartetfs.biz.pivot.postprocessing.IPrefetcher#computeLocations(java.util.Collection)
	 */
	@Override
	public Collection<ILocation> computeLocations(Collection<ILocation> locations) {
		Set<ILocation> locs = new HashSet<ILocation>();
		for(ILocation l : locations) {
				locs.add(overrideLocation(l));
		}
		return locs;
	}

	/* (non-Javadoc)
	 * @see com.quartetfs.biz.pivot.postprocessing.IPrefetcher#computeMeasures(java.util.Collection)
	 */
	@Override
	public Collection<String> computeMeasures(Collection<ILocation> locations) {
		return Arrays.asList(underlyingMeasures[0]);
	}

	protected ILocation overrideLocation(ILocation location) {
		return analysisDimensionHelper.overrideLocation(location);
	}

	private boolean hasContainerValue(ILocation location) 
	{
		return location.getLevelDepth(containerNameOrdinal) >=2;
	}
	
	private String getContainerName( ILocation location ){
		String container  = null;
		if(hasContainerValue(location)){
			container = (String)location.getCoordinate(containerNameOrdinal,1);
		}	
		return container;
	}
}
