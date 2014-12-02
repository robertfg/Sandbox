/*
 * (C) Quartet FS 2007-2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.postprocessor.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.IMember;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.IAxisMember;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.Util;
import com.quartetfs.biz.pivot.query.aggregates.IImpact;
import com.quartetfs.biz.pivot.query.aggregates.impl.AAggregatesContinuousHandler;
import com.quartetfs.biz.pivot.query.aggregates.impl.Impact;
import com.quartetfs.biz.pivot.query.aggregates.impl.TransactionStream;
import com.quartetfs.biz.pivot.transaction.IActivePivotTransactionInfo;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.transaction.TransactionException;

/**
 *
 * Continuous handler associated with one measure that needs to be updated
 * on a certain analysis dimension.
 *
 * @author Quartet FS
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.query.aggregates.IAggregatesContinuousHandler", key = AnalysisDimensionMeasureHandler.PLUGIN_TYPE)
public class AnalysisDimensionMeasureHandler extends AAggregatesContinuousHandler<IActivePivotTransactionInfo>  {

	/** serialVersionUID */
	private static final long serialVersionUID = 8324390380985999825L;

	/** Plugin value type */
	public final static String PLUGIN_TYPE = "UPDATE_ANALYSIS_DIMENSION";

	/** The name of the analysis dimension to find*/
	protected String analysisDimensionName ;

	/** The local dimensions */
	protected final List<IDimension> dimensions ;

	/** The analysis dimension where we handle the update */
	protected IDimension analysisDimension;

	/** The dimension ordinal of the analysis dimension */
	protected int analysisDimensionOrdinal; 

	/** Constructor */
	public AnalysisDimensionMeasureHandler(IActivePivot pivot) {
		super(pivot);
		this.dimensions = pivot.getDimensions();
	}

	public void setAnalysisDimensionName(String analysisDimensionName) {
		this.analysisDimensionName = analysisDimensionName;
		
		// Find the ordinal of the dimension by name
		int ordinal = Util.findDimension(dimensions, analysisDimensionName);
		
		// Save the analysis dimension ordinal
		this.analysisDimensionOrdinal = ordinal - 1;

		// Save analysis dimension
		this.analysisDimension = this.dimensions.get(ordinal);
	}

	@Override
	public IImpact computeImpact(ILocation location, IActivePivotTransactionInfo event) {
		// The results will be stored in these variables
		Set<ILocation> impact = new HashSet<ILocation>();
		Set<ILocation> removedPointCandidates = null;

		// The original location
		Object[][] original = location.arrayCopy();

		// Find the discriminators at the analysis dimension ordinal
		Object[] discriminators = original[this.analysisDimensionOrdinal];

		// Substitute the current discriminator with the null one
		original[this.analysisDimensionOrdinal] = new Object[discriminators.length];

		ILocation rangeLocation = new Location(original);
		try {
			// Compute the added locations 
			Set<ILocation> toAdd = event.computeAddedObjectsImpact(Collections.singleton(rangeLocation));

			// Substitute the default discriminator with the original discriminator
			toAdd = substitute(toAdd, discriminators);

			// Add it to the impact
			impact.addAll(toAdd);

			// Compute the locations to remove
			Set<ILocation> toRemove = event.computeRemovedObjectsImpact(Collections.singleton(rangeLocation));

			toRemove = substituteAsPointLocations(toRemove, discriminators);

			// add it to the impact
			impact.addAll(toRemove);

			// Update the removed impacts
			removedPointCandidates = toRemove;
		} catch (TransactionException e) {
			throw new QuartetRuntimeException(e);
		}
		return new Impact(location, impact, removedPointCandidates);
	}

	/**
	 * Puts back at the analysis dimension ordinal the initial discriminators that 
	 * were found
	 * 
	 * @param locations the locations to substitute
	 * @param discriminator the initial discriminators that were found
	 * @return the changed locations
	 */
	protected Set<ILocation> substitute(Set<ILocation> locations, Object[] discriminators) {
		Set<ILocation> result = new HashSet<ILocation>();
		for (ILocation loc : locations) {
			Object[][] matrix = loc.arrayCopy();
			matrix[this.analysisDimensionOrdinal] = discriminators;
			result.add(new Location(matrix));
		}

		// Return the substituted result
		return result;
	}

	/**
	 * Puts back at the analysis dimension ordinal the initial discriminators that 
	 * were found. The returned locations are point locations. If the discriminator was range
	 * we expand it.
	 * 
	 * @param locations the locations to substitute
	 * @param discriminator the initial discriminators that were found
	 * @return the changed locations
	 */
	protected Set<ILocation> substituteAsPointLocations(Set<ILocation> locations, Object[] discriminators) {
		Set<ILocation> result = new HashSet<ILocation>();
		for (ILocation loc : locations) {
			Set<ILocation> locs = substituteAsPointLocations(loc, discriminators);
			result.addAll(locs);
		}

		// Return the subsituted result
		return result;
	}

	@SuppressWarnings("rawtypes")
	protected Set<ILocation> substituteAsPointLocations(ILocation location, Object[] discriminators) {
		Set<ILocation> result = new HashSet<ILocation>(Collections.singleton(location));

		Object[][] matrix ;
		for (int i = 0; i < discriminators.length; i++) {
			
			// Temporary variable to stock the results
			Set<ILocation> tmpResult = new HashSet<ILocation>();
			for (ILocation loc : result) {
				final Object discriminator = discriminators[i];
				if (discriminator == null) {
					
					// Expand the null location with all the possible members of the
					// analysis dimension
					for (IMember allDiscriminators : this.analysisDimension.retrieveMembers(i)) {
						matrix = loc.arrayCopy();
						matrix[this.analysisDimensionOrdinal][i] = ((IAxisMember)allDiscriminators).getDiscriminator();
						tmpResult.add(new Location(matrix));
					}

				} else if (discriminator instanceof	Collection) {

					// Expand the collection location with all the members of the location
					for (Object allDiscriminators : ((Collection)discriminator)) {
						matrix = loc.arrayCopy();
						matrix[this.analysisDimensionOrdinal][i] = allDiscriminators;
						tmpResult.add(new Location(matrix));
					}
					
				} else {
					
					// Put the the concrete discriminator as it is
					matrix = loc.arrayCopy();
					matrix[this.analysisDimensionOrdinal][i] = discriminator;
					tmpResult.add(new Location(matrix));
				}
			}
			
			// Export the results to the final variable
			// and clean the temporary variable
			result.clear();
			result.addAll(tmpResult);
			
			tmpResult.clear();
		}

		// Return the substituted result
		return result;
	}

	@Override
	public String getStreamKey() { return TransactionStream.PLUGIN_TYPE; }

	@Override
	public String getType() { return PLUGIN_TYPE; }

}
