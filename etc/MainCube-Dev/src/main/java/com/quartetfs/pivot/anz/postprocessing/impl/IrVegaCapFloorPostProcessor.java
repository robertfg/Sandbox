package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.ILocationPattern;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationSet;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.anz.utils.ANZUtils;

@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = IrVegaCapFloorPostProcessor.PLUGIN_KEY)
public class IrVegaCapFloorPostProcessor extends APostProcessor<Object> implements IPrefetcher {

	private static final long serialVersionUID = 8221205760542025333L;
	public final static String PLUGIN_KEY = "IR_VEGA_CAP_FLOOR";

	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();
	
	private IDimension dimension;
	private int termBucketOrdinal;
	private Collection<String> measures; 
			
	public IrVegaCapFloorPostProcessor(final String name, final IActivePivot pivot) {
		super(name, pivot);
	}

	@Override
	public void init(final Properties properties) throws QuartetException {
		super.init(properties);
		analysisDimensionHelper.init(properties, pivot);
		
		final String dimensionName = properties.getProperty("dimensionName");
		this.dimension = ANZUtils.findDimensionByName(pivot, dimensionName) ;
		termBucketOrdinal = dimension.getOrdinal() - 1;
		
		getPrefetchers().addAll(Collections.<IPrefetcher> singletonList(this));
		measures = Collections.singletonList(underlyingMeasures[0]);
	}

	@Override
	public void evaluate(final ILocation locationReceived, final IAggregatesRetriever retriever) throws QuartetException {
	   
		final ILocationPattern locPattern = locationReceived.createWildCardPattern();		
		final List<String> terms = ANZUtils.getSortedTermBucket(dimension);
		ILocation newLocation=analysisDimensionHelper.overrideOtherDiscriminatorLocation(locationReceived,null);
				
		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singletonList(newLocation), measures);		
		ILocation bucketLoc = createWildCardBucketLocation(newLocation);
		final ICellSet bucketCellSet = bucketLoc.equals(newLocation) ? 
											cellSet :
											retriever.retrieveAggregates(Collections.singletonList(bucketLoc), measures);		
		
		cellSet.forEachLocation(new ILocationProcedure() {
			@Override
			public boolean execute(final ILocation location, final int rowId) 
			{				
				Object[] values = locPattern.extractValues(location);
				ILocation writeLocation = locPattern.generate(values);
				
				double currentTermValue = (Double) cellSet.getCellValue(rowId, underlyingMeasures[0]);		
				if(hasTermValue(location))
				{
					String currentTerm = (String)location.getCoordinate(termBucketOrdinal, 1);
					retriever.write(writeLocation, compute(bucketCellSet,currentTerm, location, currentTermValue, terms));		
				}
				else
				{
					retriever.write(writeLocation, currentTermValue);
				}				
				return true;
			}
		});

	}
	
	private double compute(ICellSet bucketCellSet,String currTerm, ILocation location, double currentTermValue, List<String> termBuckets)
	{		
		int termIndex = termBuckets.indexOf(currTerm);
		//If this is last element then return same value;
		if(termIndex==termBuckets.size()-1 || termIndex==-1)
		{
			return currentTermValue;
		}
		
		Iterator<String> itr  = termBuckets.listIterator(termIndex+1);
		while(itr.hasNext())
		{
			String nextTermBucket = itr.next();
			Object[][] nextTermBucketLoc = location.arrayCopy().clone();
			nextTermBucketLoc[termBucketOrdinal][1] = nextTermBucket;			
			Double value = (Double)bucketCellSet.getCellValue( new Location(nextTermBucketLoc), underlyingMeasures[0]);
			if(value!=null)
			{
				return currentTermValue - value;
			}
		}				
		return currentTermValue;			
	}

	private ILocation createWildCardBucketLocation(ILocation location) 
	{
		if(hasTermValue(location))
		{
			Object[][] bucketDimLoc = location.arrayCopy();
			bucketDimLoc[termBucketOrdinal][1]=null;
			return new Location(bucketDimLoc);
		}		
		return location;
	}

	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

	@Override
	public Collection<ILocation> computeLocations(final Collection<ILocation> locations)
	{
		Set<ILocation> locationSet = new LocationSet();
		for(ILocation loc : locations)
		{
			locationSet.add(loc);	
			locationSet.add(analysisDimensionHelper.overrideOtherDiscriminatorLocation(createWildCardBucketLocation(loc),null));				
		}
		return locationSet;
	}

	@Override
	public Collection<String> computeMeasures(Collection<ILocation> locations) {
		return measures;
	}

	private boolean hasTermValue(ILocation location) 
	{
		return location.getLevelDepth(termBucketOrdinal) >=2;
	}

}