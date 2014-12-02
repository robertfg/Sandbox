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
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.ANZUtils;

@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = IrVolgaETOPostProcessor.PLUGIN_KEY)
public class IrVolgaETOPostProcessor extends APostProcessor<Object> implements IPrefetcher {

	private static final long serialVersionUID = 8221205760542025333L;
	public final static String PLUGIN_KEY = "IR_VOLGA_ETO";

	
	private IDimension dimTermBucket;
	private int termBucketOrdinal;
	private IDimension dimScenTermUnderlying;

	
	private int scnTermUnderLyingOrdinal;
	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();
	
	private int containerNameOrdinal;
	private IDimension dimContainerName;
	private IDimension dimSpreadType;
	private int spreadTypeOrdinal;
	
	private String otherContainer;
	private List<String> validContainers;
	
	public IrVolgaETOPostProcessor(final String name, final IActivePivot pivot) {
		super(name, pivot);
	}

	@Override
	public void init(final Properties properties) throws QuartetException {
		super.init(properties);
		analysisDimensionHelper.init(properties, pivot);
		
		final String termBucket = properties.getProperty("dimTermBucket"); 
		dimTermBucket = ANZUtils.findDimensionByName(pivot, termBucket);
		termBucketOrdinal        = dimTermBucket.getOrdinal() - 1;
		final String scenTermUnderlying = properties.getProperty("dimScenTermUnderlying"); 
		dimScenTermUnderlying = ANZUtils.findDimensionByName(pivot, scenTermUnderlying);
		scnTermUnderLyingOrdinal = dimScenTermUnderlying.getOrdinal() - 1;
		String spreadType = properties.getProperty("dimSpreadType");
		dimSpreadType = ANZUtils.findDimensionByName(pivot, spreadType);
		spreadTypeOrdinal    = dimSpreadType.getOrdinal() -1;
		
		
		String containerName = properties.getProperty("dimContainerName");
		dimContainerName = ANZUtils.findDimensionByName(pivot, containerName);
		containerNameOrdinal     = dimContainerName.getOrdinal() -1;
		
		final String validContainer = properties.getProperty("validContainer");
		            validContainers = Arrays.asList(validContainer.split("/,"));  
		  
		
		getPrefetchers().addAll(Collections.<IPrefetcher> singletonList(this));

	}

	@Override
	public void evaluate(ILocation locationReceived,final IAggregatesRetriever retriever) throws QuartetException {
		
	//	if(!doMandatoryDimensionCheck(locationReceived)){return;}
		final List<String> validContainers = this.validContainers;
		
		final ILocationPattern locPattern = locationReceived.createWildCardPattern();
		ILocation newLocation=analysisDimensionHelper.overrideOtherDiscriminatorLocation(locationReceived,null);
		
		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singletonList(newLocation), Collections.singletonList(underlyingMeasures[0]));		
		ILocation wildCardLocation = createWildCardLocation(newLocation);	
		
		final ICellSet bucAndUnderlyerCellSet = retriever.retrieveAggregates(Collections.singletonList(wildCardLocation), Collections.singletonList(underlyingMeasures[0]));
		final List<String> termBuckets = ANZUtils.getSortedTermBucket(dimTermBucket);
		final List<String> underlyingTermBuckets =  ANZUtils.getSortedTermBucket(dimScenTermUnderlying);
		
		
		cellSet.forEachLocation(new ILocationProcedure() {
			@Override
			public boolean execute(final ILocation location, final int rowId) {
					
					Object[] values = locPattern.extractValues(location);;
					ILocation writeLocation = locPattern.generate(values);
					final double currentTermValue = (Double) cellSet.getCellValue(rowId, underlyingMeasures[0]);
					final String spreadType = hasSpreadType(location)? (String)location.getCoordinate(spreadTypeOrdinal, 1): "SLICER";
					
					
					/*final String container = (String)location.getCoordinate(containerNameOrdinal , 1);
					
					if(container.equals(otherContainer)){
						retriever.write(writeLocation, currentTermValue);
						return true;
					} else if(validContainers.toString().indexOf(container)<=-1) { 
						retriever.write(writeLocation, currentTermValue);
						return true;
					}*/
					 
					if (hasTermValue(location) && hasTermUnderlyingValue(location) && spreadType.equalsIgnoreCase("ETO")) 
					{
							String termBucket =(String)location.getCoordinate(termBucketOrdinal,1);												
							String underlyingTerm = (String)location.getCoordinate(scnTermUnderLyingOrdinal,1);
							retriever.write(writeLocation, compute(underlyingTerm, termBucket, location, currentTermValue));	
					} else if (hasTermValue(location) && hasTermUnderlyingValue(location) && spreadType.equalsIgnoreCase("SLICER") /* && !spreadType.equalsIgnoreCase("CapFloor") && !spreadType.equalsIgnoreCase("Swaption")*/ ) {
						String termBucket =(String)location.getCoordinate(termBucketOrdinal,1);												
						String underlyingTerm = (String)location.getCoordinate(scnTermUnderLyingOrdinal,1);
						retriever.write(writeLocation, compute(underlyingTerm, termBucket, location, currentTermValue));	
				    }
					else 
					{				
						retriever.write(writeLocation, 0.0); //currentTermValue
					}
			
				return true;
			}

			private double compute(String underlyingTerm,String termBucket, ILocation location, double currentTermValue)  
			{
				Double  currentVegaValue  = getVegaValue(location,bucAndUnderlyerCellSet);
				
				int bucketIndex=termBuckets.indexOf(termBucket);
				int underlyingTermIdx = underlyingTermBuckets.indexOf(underlyingTerm);
				double baseValue = 0;
				
				
				
				String container  = null;
				if(hasContainerValue(location)){
					container = (String)location.getCoordinate(containerNameOrdinal,1);
					if(container!=null && container.equalsIgnoreCase("IR_VANNA")){
						baseValue = getBaseValue(location,bucAndUnderlyerCellSet,termBucket); 
					}
				}
				
				if(bucketIndex+1==termBuckets.size() && underlyingTermIdx+1== underlyingTermBuckets.size())
				{
					return currentTermValue - baseValue;
				}
				
				final Object[][] nextTermBucketLoc = location.arrayCopy().clone();
				String[] termBucketDimension = new String[2];
				nextTermBucketLoc[termBucketOrdinal] = termBucketDimension;
				nextTermBucketLoc[termBucketOrdinal][0] = ILevel.ALLMEMBER;
				
				Iterator<String> itr = termBuckets.listIterator(bucketIndex+1);
				while(itr.hasNext())
				{
					String term=itr.next();
					nextTermBucketLoc[termBucketOrdinal][1] = term;
					
					ILocation nextLocation = new Location(nextTermBucketLoc);
					Double nextValue = (Double)bucAndUnderlyerCellSet.getCellValue(nextLocation, underlyingMeasures[0]);
					if(nextValue!=null)
					{	
						 Double nextVegaValue = getVegaValue(nextLocation,bucAndUnderlyerCellSet);
						 
						 Double nextValueBase = 0.0;
						 if(container!=null && container.equalsIgnoreCase("IR_VANNA")){
							 nextValueBase = getBaseValue(location,bucAndUnderlyerCellSet,term); 
						 }		 
						 
						 return ( (currentTermValue -baseValue) - (nextValue-nextValueBase));
					}
				}	
				
					if(!termBucket.equalsIgnoreCase( ANZConstants.NA ) && !underlyingTerm.equalsIgnoreCase(ANZConstants.NA)){
						boolean isLongestTerm = isLongestTermUnderlying(location,bucAndUnderlyerCellSet,underlyingTermIdx);
					 
						if(isLongestTerm){
					    	return currentTermValue - getBaseValue(location,bucAndUnderlyerCellSet,termBucket);
					    }
						 
					}
				  
				return currentTermValue;
				
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
				
				ILocation currentVegaLocation = this.createOtherContainerLocation(location, otherContainer); //to put in config
				Double    currentVegaValue    = (Double)cellSet.getCellValue( new Location(currentVegaLocation), underlyingMeasures[0]);
				if(currentVegaValue==null){
					currentVegaValue = 0.0;
				}
				return currentVegaValue;

			}
			private double getBaseValue(ILocation location, ICellSet cellSet, String termBucket ) {
				
				final Object[][] currentLocations = location.arrayCopy();
				String[] scenarioTerm = new String[2];
				currentLocations[termBucketOrdinal] = scenarioTerm;
				currentLocations[termBucketOrdinal][0] = ILevel.ALLMEMBER;
				currentLocations[termBucketOrdinal][1] =  "n/a";
				
				String[] scenarioTermUnderlying = new String[2];
				currentLocations[scnTermUnderLyingOrdinal] = scenarioTermUnderlying; 
				currentLocations[scnTermUnderLyingOrdinal][0] = ILevel.ALLMEMBER;
				currentLocations[scnTermUnderLyingOrdinal][1] = "n/a";
				
				String[] spreadType = new String[2];
				currentLocations[spreadTypeOrdinal ] = spreadType; 
				currentLocations[spreadTypeOrdinal][0] = ILevel.ALLMEMBER;
				currentLocations[spreadTypeOrdinal][1] = "Vanna";
				
				
				Double  baseValue    = (Double)cellSet.getCellValue( new Location(currentLocations), underlyingMeasures[0]);
				if(baseValue==null){
					baseValue = 0.0;
				}
				return baseValue;

			}
			
			private boolean isLongestTermUnderlying(ILocation location, ICellSet cellSet, int underlyingTermIdx){
				
				final Object[][] nextTermUnderlyingLoc = location.arrayCopy().clone();
				String[] termBucketDimension = new String[2];
				nextTermUnderlyingLoc[scnTermUnderLyingOrdinal] = termBucketDimension;
				nextTermUnderlyingLoc[scnTermUnderLyingOrdinal][0] = ILevel.ALLMEMBER;
				
				
				Iterator<String> itr = underlyingTermBuckets.listIterator(underlyingTermIdx+1);
				
				
				while(itr.hasNext())
				{
					String underTerm=itr.next();
					nextTermUnderlyingLoc[scnTermUnderLyingOrdinal][1] = underTerm;
					
					ILocation nextLocation = new Location(nextTermUnderlyingLoc);
					Double value = (Double)bucAndUnderlyerCellSet.getCellValue(nextLocation, underlyingMeasures[0]);
					if(value!=null)
					{	
						 return false;
					}
					
				}
				
				return true;
				
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
			locs.add(wildCardLoc);
			
			if(!wildCardLoc.equals(location))
			{
				locs.add(analysisDimensionHelper.overrideOtherDiscriminatorLocation(wildCardLoc,null));
				locs.add(analysisDimensionHelper.overrideOtherDiscriminatorLocation(location,null));
				//System.out.println( "2:" + analysisDimensionHelper.overrideOtherDiscriminatorLocation(wildCardLoc,null) );
				//System.out.println( "2.1:" + analysisDimensionHelper.overrideOtherDiscriminatorLocation(location,null) );
				
			}
			
			//System.out.println( "3:" + location.toString());
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
		
	/*	if(hasContainerValue(location))
		{
			locationCopy[containerNameOrdinal][1]=null;
		}	
		
		if(hasSpreadType(location))
		{
			locationCopy[spreadTypeOrdinal][1]=null;
		}	
		*/
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

	private boolean hasContainerValue(ILocation location) 
	{
		return location.getLevelDepth(containerNameOrdinal) >=2;
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
	
	private boolean hasSpreadType(ILocation location) 
	{
		return location.getLevelDepth(spreadTypeOrdinal) >=2;
	}
	
	
	
}