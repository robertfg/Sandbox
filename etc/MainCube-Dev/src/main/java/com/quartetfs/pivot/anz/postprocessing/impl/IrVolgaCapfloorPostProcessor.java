package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.Arrays;
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
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationSet;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.anz.utils.ANZUtils;

@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = IrVolgaCapfloorPostProcessor.PLUGIN_KEY)
public class IrVolgaCapfloorPostProcessor extends APostProcessor<Object> implements IPrefetcher {

	private static final long serialVersionUID = 822120576054455333L;
	public final static String PLUGIN_KEY = "IR_VOLGA_CAPFLOOR"; 
	

	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();
	
	private IDimension dimension;
	private int termBucketOrdinal;
	private Collection<String> measures; 
	private int containerNameOrdinal;
	private IDimension dimContainerName;
	private String otherContainer;
	private List<String> validContainers;
	
	private IDimension dimSpreadType;
	private int spreadTypeOrdinal;
	
	private IDimension dimScenTermUnderlying;
	private int scnTermUnderLyingOrdinal;
	//private int spreadTypeOrdinal;
	//private IDimension dimSpreadType;
	
	
	public IrVolgaCapfloorPostProcessor(final String name, final IActivePivot pivot) {
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
		
		String containerName = properties.getProperty("dimContainerName");
		dimContainerName = ANZUtils.findDimensionByName(pivot, containerName);
		containerNameOrdinal     = dimContainerName.getOrdinal() -1;
		otherContainer = properties.getProperty("otherContainer");
		final String validContainer = properties.getProperty("validContainer");
		  validContainers = Arrays.asList(validContainer.split("/,"));  
		String spreadType = properties.getProperty("dimSpreadType");
		dimSpreadType = ANZUtils.findDimensionByName(pivot, spreadType);
		spreadTypeOrdinal    = dimSpreadType.getOrdinal() -1;
		
		
		final String scenTermUnderlying = properties.getProperty("dimScenTermUnderlying"); 
		dimScenTermUnderlying = ANZUtils.findDimensionByName(pivot, scenTermUnderlying);
		scnTermUnderLyingOrdinal = dimScenTermUnderlying.getOrdinal() - 1;
		
	}

	@Override
	public void evaluate(final ILocation locationReceived, final IAggregatesRetriever retriever) throws QuartetException {
//	 	if(!doMandatoryDimensionCheck(locationReceived)){
//		   return;
//	    }  
		final List<String> validContainers = this.validContainers;
		
		final ILocationPattern locPattern = locationReceived.createWildCardPattern();		
		final List<String> terms = ANZUtils.getSortedTermBucket(dimension);
		ILocation newLocation=analysisDimensionHelper.overrideOtherDiscriminatorLocation(locationReceived,null);
				
		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singletonList(newLocation), measures);		
		
		ILocation bucketLoc = createWildCardBucketLocation(newLocation);
		
		final ICellSet bucketCellSet = bucketLoc.equals(newLocation) ? 
											cellSet :retriever.retrieveAggregates(Collections.singletonList(bucketLoc), measures);		
		
		
		
		cellSet.forEachLocation(new ILocationProcedure() {
			@Override
			public boolean execute(final ILocation location, final int rowId) 
			{				
				Object[] values = locPattern.extractValues(location);
				ILocation writeLocation = locPattern.generate(values);
				
				double currentTermValue = (Double) cellSet.getCellValue(rowId, underlyingMeasures[0]);
				final String spreadType = hasSpreadType(location)? (String)location.getCoordinate(spreadTypeOrdinal, 1): "SLICER";
				
				
			/*	final String container = (String)location.getCoordinate(containerNameOrdinal , 1);
				
				if(container.equals(otherContainer)){ // in this case IR_VEGA
					retriever.write(writeLocation, currentTermValue);
					return true;
				} 
				 // apply the computation only on valid containers defined in the config
				else if(validContainers.toString().indexOf(container)<=-1){
					retriever.write(writeLocation, currentTermValue);
					return true;
				}
				*/
				if(hasTermValue(location) && spreadType.equalsIgnoreCase("CapFloor"))
				{ 
						String currentTerm = (String)location.getCoordinate(termBucketOrdinal, 1);
						retriever.write(writeLocation, compute(bucketCellSet,currentTerm, location, currentTermValue, terms));		
				
				} else if(hasTermValue(location) && spreadType.equalsIgnoreCase("SLICER") /*&& !spreadType.equalsIgnoreCase("ETO") && !spreadType.equalsIgnoreCase("Swaption")*/ )
				{ 
					String currentTerm = (String)location.getCoordinate(termBucketOrdinal, 1);
					retriever.write(writeLocation, compute(bucketCellSet,currentTerm, location, currentTermValue, terms));		
			
				}	  
				else
				{
					retriever.write(writeLocation, 0.0); //
				}				
				return true;
			}
		});

	}
	
	private double compute(ICellSet bucketCellSet,String currTerm, ILocation location, double currentTermValue, List<String> termBuckets)
	{	
		double baseValue = 0;
		
		
		String container  = null;
		if(hasContainerValue(location)){
			container = (String)location.getCoordinate(containerNameOrdinal,1);
			if(container!=null && container.equalsIgnoreCase("IR_VANNA")){
				baseValue = getBaseValue(location,bucketCellSet,currTerm); 
			}
		}
		Double  currentVegaValue  = getVegaValue(location,bucketCellSet);
		 
		
		int termIndex = termBuckets.indexOf(currTerm);
		//If this is last element then return same value;
		if(termIndex==termBuckets.size()-1 || termIndex==-1)
		{
			return currentTermValue-baseValue;// - currentVegaValue; commented according to BA no need for IR_VEGA VALUE
		}
		
		Iterator<String> itr  = termBuckets.listIterator(termIndex+1);
		while(itr.hasNext())
		{
			String nextTermBucket = itr.next();
			Object[][] nextTermBucketLoc = location.arrayCopy().clone();
			nextTermBucketLoc[termBucketOrdinal][1] = nextTermBucket;	
			ILocation nextLocation = new Location(nextTermBucketLoc);
			Double value = (Double)bucketCellSet.getCellValue( new Location(nextLocation), underlyingMeasures[0]);
			if(value!=null)
			{
				Double nextVegaValue = getVegaValue(nextLocation,bucketCellSet); 
				return ((currentTermValue - baseValue) - (value - baseValue)) ;// - (currentVegaValue - nextVegaValue);  commented according to BA no need for IR_VEGA VALUE
			}
		}				
		return currentTermValue- baseValue;// - currentVegaValue;		 commented according to BA no need for IR_VEGA VALUE	
	}

	private ILocation createWildCardBucketLocation(ILocation location) 
	{	
		if(hasTermValue(location))
		{
			Object[][] bucketDimLoc = location.arrayCopy();
			bucketDimLoc[termBucketOrdinal][1]=null;
			if(hasContainerValue(location) ){
				bucketDimLoc[containerNameOrdinal][1]=null;
			}
			return new Location(bucketDimLoc);

		}	
		return location;
	}
	private double getBaseValue(ILocation location, ICellSet cellSet, String termBucket ) {
		
		final Object[][] currentLocations = location.arrayCopy();
		String[] scenarioTerm = new String[2];
		currentLocations[termBucketOrdinal] = scenarioTerm;
		currentLocations[termBucketOrdinal][0] = ILevel.ALLMEMBER;
		currentLocations[termBucketOrdinal][1] = termBucket ;
		
	    /*String[] spreadType = new String[2];
		currentLocations[spreadTypeOrdinal ] = spreadType; 
		currentLocations[spreadTypeOrdinal][0] = ILevel.ALLMEMBER;
		currentLocations[spreadTypeOrdinal][1] = "3m";
		*/
		
		String[] scenarioTermUnderlying = new String[2];
		currentLocations[scnTermUnderLyingOrdinal] = scenarioTermUnderlying; 
		currentLocations[scnTermUnderLyingOrdinal][0] = ILevel.ALLMEMBER;
		currentLocations[scnTermUnderLyingOrdinal][1] = "3m";
		
		
		
		Double  baseValue    = (Double)cellSet.getCellValue( new Location(currentLocations), underlyingMeasures[0]);
		if(baseValue==null){
			baseValue = 0.0;
		}
		return baseValue;

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
	
	private boolean hasContainerValue(ILocation location) 
	{
		return location.getLevelDepth(containerNameOrdinal) >=2;
	}
	
	
	private ILocation  createOtherContainerLocation(ILocation locationReceived,String container){
		final Object[][] containerLocations = locationReceived.arrayCopy();
		String[] containerDimension = new String[2];
		containerLocations[containerNameOrdinal] = containerDimension;
		containerLocations[containerNameOrdinal][0] = ILevel.ALLMEMBER;
		containerLocations[containerNameOrdinal][1] = container;
		ILocation containerLocation = new Location(containerLocations);
		
		return containerLocation;
		
	}
	
	private double getVegaValue(ILocation location, ICellSet cellSet ) {
		ILocation currentVegaLocation = this.createOtherContainerLocation(location, "IR_VEGA"); //to put in config
		Double    currentVegaValue    = (Double)cellSet.getCellValue( new Location(currentVegaLocation), underlyingMeasures[0]);
		if(currentVegaValue==null){
			currentVegaValue = 0.0;
		}
		return currentVegaValue;
	}
	
	private boolean doMandatoryDimensionCheck(ILocation locationReceived){
		Object[][] locationCopy = locationReceived.arrayCopy();
		
	/*	if (locationCopy[spreadTypeOrdinal].length < 2 ) { 
		    return false;		
		}*/
		
		if (locationCopy[containerNameOrdinal].length < 2 ) { 
		    return false;		
		}
		
		
		return true;
	}

	public void setValidContainers(List<String> validContainers) {
		this.validContainers = validContainers;
	}

	public List<String> getValidContainers() {
		return validContainers;
	}
	
	private boolean hasSpreadType(ILocation location) 
	{
		return location.getLevelDepth(spreadTypeOrdinal) >=2;
	}
}