/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.impl.Util;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.pivot.anz.utils.ANZConstants;

/**
 * Base class for post-processors depending on measures both from the current location and the parent location along some dimension
 * @author Quartet Financial Systems
 * @param <T> The type
 */
public abstract class AParentAndSelfPostProcessor<T> extends APostProcessor<T> {
	private static final long serialVersionUID = 8137492300298882468L;
	
	protected int parentDimIndex;
	//used to store the dim and level indexes we focus on
	protected int[] firstDiscriminatorIndices;
	//other dimension that have an impact (Scenario dim in our case)
	
	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();
	
	public AParentAndSelfPostProcessor(String name, IActivePivot pivot) {
		super(name, pivot);
	}
	
	@Override
	public void init(Properties properties) throws QuartetException {
		super.init(properties);
		parentDimIndex = Util.findDimension(pivot.getDimensions(), properties.getProperty(ANZConstants.PARENT_DIM_PROP)) - 1;
		this.prefetchers.add(new SelfPrefetcher());//Add a prefetcher for measures on the evaluation locations
		this.prefetchers.add(new ParentPrefetcher());//Add a prefetcher for measures on the parent locations
		
		analysisDimensionHelper.init(properties, pivot);
		firstDiscriminatorIndices=analysisDimensionHelper.getFirstDiscriminatorIndices().get(0);
	}

	/**
	 * Build the parent location
	 * @param location The location to find the parent for
	 * @return the parent location
	 */
	protected ILocation parentLocation(ILocation location) {
		return LocationUtil.parentLocation(location, parentDimIndex);
	}
	
	/**
	 * @return the measures to prefetch on the evaluation locations
	 */
	protected abstract Collection<String> getSelfMeasures();

	/**
	 * @return the measures to prefetch on the parent locations
	 */
	protected abstract Collection<String> getParentMeasures();
	
		/**
	 * @return the measures to prefetch on the parent locations
	 */
	protected ILocation overrideLocation(ILocation location){
		return analysisDimensionHelper.overrideLocation(location);
	}
	
	protected class ParentPrefetcher implements IPrefetcher {
		@Override
		public Collection<ILocation> computeLocations(Collection<ILocation> locations) {
			Set<ILocation> res = new HashSet<ILocation>();
			for(ILocation location : locations) {
				final ILocation p = parentLocation(overrideLocation(location));//add parent location of the overridden location here
				if(p != null)
					res.add(p);
			}
			return res;
		}
		
		@Override
		public Collection<String> computeMeasures(Collection<ILocation> locations) {
			return getParentMeasures();
		}
	}
	
	protected class SelfPrefetcher implements IPrefetcher {
		@Override
		public Collection<ILocation> computeLocations(Collection<ILocation> locations) {
			Set<ILocation> res = new HashSet<ILocation>();
			for(ILocation location : locations) {
				//We compute the parent location because if it's null we won't need to compute the PP
				final ILocation p = parentLocation(overrideLocation(location));
				if(p != null)
					res.add(overrideLocation(location));//Careful: here we add the evaluation location not the parent location!
			}
			return res;
		}
		@Override
		public Collection<String> computeMeasures(Collection<ILocation> locations) {
			return getSelfMeasures();
		}
	}
	
	
	
}
