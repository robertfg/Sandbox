package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.ILocationPattern;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.anz.utils.ANZUtils;

@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = IrVegaETOPostProcessor.PLUGIN_KEY)
public class IrVegaETOPostProcessor extends APostProcessor<Object> implements IPrefetcher {

	private static final long serialVersionUID = 8221205760542025333L;
	public final static String PLUGIN_KEY = "IR_VEGA_ETO";

	private IDimension dimTermBucket;
	private IDimension dimScenTermUnderlying;

	private int termBucketOrdinal;
	private int scnTermUnderLyingOrdinal;
	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();
	
	public IrVegaETOPostProcessor(final String name, final IActivePivot pivot) {
		super(name, pivot);
	}

	@Override
	public void init(final Properties properties) throws QuartetException {
		super.init(properties);
		analysisDimensionHelper.init(properties, pivot);
		
		final String termBucket = properties.getProperty("dimTermBucket"); 
		final String scenTermUnderlying = properties.getProperty("dimScenTermUnderlying"); 
	
		dimTermBucket = ANZUtils.findDimensionByName(pivot, termBucket);
		dimScenTermUnderlying = ANZUtils.findDimensionByName(pivot, scenTermUnderlying);
		
		termBucketOrdinal        = dimTermBucket.getOrdinal() - 1;
		scnTermUnderLyingOrdinal = dimScenTermUnderlying.getOrdinal() - 1;
		
		getPrefetchers().addAll(Collections.<IPrefetcher> singletonList(this));

	}

	@Override
	public void evaluate(ILocation locationReceived,final IAggregatesRetriever retriever) throws QuartetException {
		
		final ILocationPattern locPattern = locationReceived.createWildCardPattern();
		ILocation newLocation=analysisDimensionHelper.overrideOtherDiscriminatorLocation(locationReceived,null);
		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singletonList(newLocation), Collections.singletonList(underlyingMeasures[0]));		
		ILocation wildCardLocation = createWildCardLocation(newLocation);	
		final ICellSet bucAndUnderlyerCellSet = retriever.retrieveAggregates(Collections.singletonList(wildCardLocation), Collections.singletonList(underlyingMeasures[0]));
		final List<String> termBuckets = ANZUtils.getSortedTermBucket(dimTermBucket);
		
		
		cellSet.forEachLocation(new ILocationProcedure() {
			@Override
			public boolean execute(final ILocation location, final int rowId) {
					
					Object[] values = locPattern.extractValues(location);;
					ILocation writeLocation = locPattern.generate(values);
					final double currentTermValue = (Double) cellSet.getCellValue(rowId, underlyingMeasures[0]);

					if (hasTermValue(location) && hasTermUnderlyingValue(location)) 
					{
						String termBucket =(String)location.getCoordinate(termBucketOrdinal,1);												
						retriever.write(writeLocation, compute(termBucket, location, currentTermValue));				
					}
					else 
					{				
						retriever.write(writeLocation, currentTermValue);
					}
			
				return true;
			}

			private double compute(String termBucket, ILocation location, double currentTermValue)  
			{
				int bucketIndex=termBuckets.indexOf(termBucket);
				if(bucketIndex+1==termBuckets.size())
				{
					return currentTermValue;
				}
				
				final Object[][] termsLocations = location.arrayCopy().clone();
				String[] termBucketDimension = new String[2];
				termsLocations[termBucketOrdinal] = termBucketDimension;
				termsLocations[termBucketOrdinal][0] = ILevel.ALLMEMBER;
				
				Iterator<String> itr = termBuckets.listIterator(bucketIndex+1);
				while(itr.hasNext())
				{
					String term=itr.next();
					termsLocations[termBucketOrdinal][1] = term;					
					Double value = (Double)bucAndUnderlyerCellSet.getCellValue( new Location(termsLocations), underlyingMeasures[0]);
					if(value!=null)
					{
						 return currentTermValue - value;
					}
				}				
				return currentTermValue;
				
				}

		});

	}

	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

	@Override
	public Collection<ILocation> computeLocations(final Collection<ILocation> locations) {
		final Set<ILocation> locs = new HashSet<ILocation>();
		for (final ILocation location : locations) {
			
			ILocation wildCardLoc = createWildCardLocation(location);
			if(!wildCardLoc.equals(location))
			{
				locs.add(analysisDimensionHelper.overrideOtherDiscriminatorLocation(wildCardLoc,null));
			}
			locs.add(location);
		}
		return locs;
	}

	private ILocation createWildCardLocation(ILocation location) 
	{
		Object[][] locationCopy = location.arrayCopy();
		if(hasTermValue(location))
		{
			locationCopy[termBucketOrdinal][1]=null;
		}
		if(hasTermUnderlyingValue(location))
		{
			locationCopy[scnTermUnderLyingOrdinal][1]=null;
		}	
		return new Location(locationCopy);	
	}

	@Override
	public Collection<String> computeMeasures(final Collection<ILocation> locations) {
		return Arrays.asList(underlyingMeasures[0]);
	}
	
	private boolean hasTermValue(ILocation location) 
	{
		return location.getLevelDepth(termBucketOrdinal) >=2;
	}
	
	private boolean hasTermUnderlyingValue(ILocation location) 
	{
		return location.getLevelDepth(scnTermUnderLyingOrdinal) >=2;
	}

}