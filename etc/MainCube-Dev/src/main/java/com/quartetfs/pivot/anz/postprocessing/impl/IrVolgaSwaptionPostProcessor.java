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
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.pivot.anz.utils.ANZUtils;

@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = IrVolgaSwaptionPostProcessor.PLUGIN_KEY)
public class IrVolgaSwaptionPostProcessor extends APostProcessor<Object> implements IPrefetcher {

	private static final long serialVersionUID = 822120576054455333L;
	public final static String PLUGIN_KEY = "IR_VOLGA_SWAPTION"; 
	
	private IDimension dimTermBucket;
	private IDimension dimScenTermUnderlying;
	
	private IDimension dimSpreadType;
	private int spreadTypeOrdinal;
	private int termBucketOrdinal;
	private int scnTermUnderLyingOrdinal;
	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();
	private int containerNameOrdinal;
	private IDimension dimContainerName;
	
	private String otherContainer;
	private List<String> validContainers;
	
	
	public IrVolgaSwaptionPostProcessor(final String name, final IActivePivot pivot) {
		super(name, pivot);
	}

	@Override
	public void init(final Properties properties) throws QuartetException {
		super.init(properties);
		analysisDimensionHelper.init(properties, pivot);

		String termBucket = properties.getProperty("dimTermBucket"); 
		dimTermBucket = ANZUtils.findDimensionByName(pivot, termBucket);
		termBucketOrdinal        = dimTermBucket.getOrdinal() - 1;
		String scenTermUnderlying = properties.getProperty("dimScenTermUnderlying"); 
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

		getPrefetchers().addAll( Collections.<IPrefetcher> singletonList(this) );
	}

	@Override
	public void evaluate(final ILocation locationReceived, final IAggregatesRetriever retriever) throws QuartetException {
   //  if(!doMandatoryDimensionCheck(locationReceived)){ return;}
    	final List<String> validContainers = this.validContainers;
    	 
        final ILocationPattern locPattern = locationReceived.createWildCardPattern();	
		ILocation newLocation=analysisDimensionHelper.overrideOtherDiscriminatorLocation(locationReceived,null);
		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singletonList(newLocation), Collections.singletonList(underlyingMeasures[0]));
        
		final List<String> termBuckets =  ANZUtils.getSortedTermBucket(dimTermBucket);
		final List<String> underlyingTermBuckets =  ANZUtils.getSortedTermBucket(dimScenTermUnderlying);
		
		ILocation termBucketUnderlyerLoc = createWildCardLocation(newLocation);
		final ICellSet termBucketUnderlyerCellSet =termBucketUnderlyerLoc==null? cellSet: 
				retriever.retrieveAggregates(Collections.singletonList(termBucketUnderlyerLoc), Collections.singletonList(underlyingMeasures[0]));
		
			
		cellSet.forEachLocation( new ILocationProcedure() {
			@Override
			public boolean execute(final ILocation location, final int rowId) {
					
					Object[] values = locPattern.extractValues(location);
					ILocation writeLocation = locPattern.generate(values);
					
					 double currentTermValue = (Double) cellSet.getCellValue(rowId, underlyingMeasures[0]);
					
					final String termBucket = hasTermValue(location) ? (String)location.getCoordinate(termBucketOrdinal, 1):null;
					final String underlyingTerm = hasTermUnderlyingValue(location) ? (String)location.getCoordinate(scnTermUnderLyingOrdinal, 1) : null;
				
					final String spreadType = hasSpreadType(location)? (String)location.getCoordinate(spreadTypeOrdinal, 1): "SLICER";
					 
					
					
					if(termBucket!=null && underlyingTerm!=null && spreadType.equalsIgnoreCase("Swaption"))
					{	
					   retriever.write(writeLocation, compute(underlyingTerm,termBucket,location ,currentTermValue)); 	
						
					} else if(termBucket!=null && underlyingTerm!=null && spreadType.equalsIgnoreCase("SLICER") /* && !spreadType.equalsIgnoreCase("CapFloor") && !spreadType.equalsIgnoreCase("ETO")*/ )
					{	
						   retriever.write(writeLocation, compute(underlyingTerm,termBucket,location ,currentTermValue)); 	
							
					}
					else
					{
						//currentTermValue = 0d;
						retriever.write(writeLocation,  0.0);
					}					
				return true; 
			}
			
			private double compute(String underlyingTerm, String termBucket, ILocation location,double currentTermValue) 
			{			
				double baseValue = 0;
				
				String container  = null;
				if(hasContainerValue(location)){
					container = (String)location.getCoordinate(containerNameOrdinal,1);
					if(container!=null && container.equalsIgnoreCase("IR_VANNA")){
						baseValue = getBaseValue(location,termBucketUnderlyerCellSet,termBucket); 
					}
				}
				
				Double  currentVegaValue  = getVegaValue(location,termBucketUnderlyerCellSet);
				
				final Object[][] termsLocations = location.arrayCopy();
				
				int termBucketIdx = termBuckets.indexOf(termBucket);
				int underlyingTermIdx = underlyingTermBuckets.indexOf(underlyingTerm);
				
				int termBucketSize=termBuckets.size();
				int underlyingTermSize=underlyingTermBuckets.size();
				
				//if both member is not the longest term
				if( (termBucketIdx+1 < termBucketSize) && (underlyingTermIdx+1 < underlyingTermSize))
				{
					//Calc by shifting termBucket					
					Iterator<String> itr = termBuckets.listIterator(termBucketIdx+1);
					String nextTermBucket=null;
					while(itr.hasNext())
					{
						nextTermBucket =itr.next();
						String[] termBucketDimension = new String[2];
						termsLocations[termBucketOrdinal] = termBucketDimension;
						termsLocations[termBucketOrdinal][0] = ILevel.ALLMEMBER;
						termsLocations[termBucketOrdinal][1] = nextTermBucket;
						
						Double nextValue=(Double)termBucketUnderlyerCellSet.getCellValue( new Location(termsLocations) , underlyingMeasures[0]);
						
						if(nextValue==null){
							continue;// try for next termbucket
						} else {
							Double  nextVegaValue  = getVegaValue(location,termBucketUnderlyerCellSet);
							return ( (currentTermValue-baseValue) + nextVegaValue) - ( (nextValue-baseValue) + currentVegaValue);
						}
					}					
					
					return calcUsingNextUnderlyingTermBucket(currentTermValue,currentVegaValue,termsLocations,underlyingTermIdx,baseValue);					
				}
				//If term bucket is longest & underlyingterm bucket is not longest 
				else if((termBucketIdx+1 == termBucketSize) && (underlyingTermIdx+1 < underlyingTermSize))
				{
					//If calc by termbucket does't work then calc by shifting underlyingTermBucket
					return calcUsingNextUnderlyingTermBucket( currentTermValue,currentVegaValue,termsLocations,underlyingTermIdx,baseValue);		
				}				
				return currentTermValue;// - currentVegaValue;
			}

			private double calcUsingNextUnderlyingTermBucket(double currentTermValue,double currentVegaValue,final Object[][] termsLocations, int underlyingTermSize, double baseValue) 
			{
				
				Iterator<String> itr;
				//If calc by termbucket does't work then calc by shifting underlyingTermBucket
				itr = underlyingTermBuckets.listIterator(underlyingTermSize+1);
				while(itr.hasNext())
				{
					String nextUnderlyingBucket = itr.next();
					String[] underlyingDimension = new String[2];
					termsLocations[scnTermUnderLyingOrdinal] = underlyingDimension;
					termsLocations[scnTermUnderLyingOrdinal][0] = ILevel.ALLMEMBER;
					termsLocations[scnTermUnderLyingOrdinal][1] = nextUnderlyingBucket;	
					for(String currentTerm : termBuckets)
					{
						String[] termBucketDimension = new String[2];
						termsLocations[termBucketOrdinal] = termBucketDimension;
						termsLocations[termBucketOrdinal][0] = ILevel.ALLMEMBER;
						termsLocations[termBucketOrdinal][1] = currentTerm;	
						
						ILocation nextLocation = new Location(termsLocations);
						
						Double nextValue=(Double)termBucketUnderlyerCellSet.getCellValue( new Location(nextLocation) , underlyingMeasures[0]);
						if(nextValue==null){ 
							continue;
						} else {
						
							// try for next termbucket	
							Double nextVegaValue = getVegaValue(nextLocation,termBucketUnderlyerCellSet); 
							
							return ( (currentTermValue-baseValue) + nextVegaValue) - ( (nextValue-baseValue) + currentVegaValue);
							 
							// old imple return (currentTermValue-nextValue);// - ( currentVegaValue - nextVegaValue);
							
						}
						
					}
				}
				
				
				//return currentTermValue;// - currentVegaValue;
				
				
				Double shortestVegaValue = 0.0;
				try {
					shortestVegaValue = getShortestVegaValue( new Location(termsLocations), "IR_VEGA", termBucketUnderlyerCellSet, termBuckets, underlyingTermBuckets);
				   if(shortestVegaValue==null){
					   shortestVegaValue=0.0;
				   }
				} catch (QueryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return currentTermValue-baseValue; // - shortestVegaValue;// - currentVegaValue;
				
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
			 
			
			private double getShortestVegaValue(ILocation locRecieved, String container,
					ICellSet cellSet,List<String> sceneTermList,List<String> sceneTermUnderlyingList ) throws QueryException{
				
				Iterator<String> scenarioTermIterator = sceneTermList.iterator();
				Iterator<String> scenarioTermUnderlyingIterator = sceneTermUnderlyingList.iterator();
				boolean gotShortestVegaValue = false;
				Double	vegaValue = null;

				while(scenarioTermUnderlyingIterator.hasNext())
				{
					String shortestScenarioTermUnderlying=scenarioTermUnderlyingIterator.next();
						while(scenarioTermIterator.hasNext())
						{
							String shortestScenarioTerm =scenarioTermIterator.next();
							   ILocation vegaLocation	= createShortestVegaValueLocation(locRecieved, container, shortestScenarioTerm,shortestScenarioTermUnderlying  );
								vegaValue =(Double) cellSet.getCellValue( vegaLocation, underlyingMeasures[0]);//error here
								//System.out.println("shortest vega value:" + vegaValue);
								if(vegaValue!=null) {
									gotShortestVegaValue = true;
									break;
								} 	
						}
						if(gotShortestVegaValue){
							break;
						}
						
				}
			
				if(vegaValue==null){
					return 0;
				}
				
				
				return vegaValue;

			}
			private ILocation  createShortestVegaValueLocation(ILocation locationReceived,String container, String shortestScenarioTerm,
					String shortestScenarioTermUnderlying){
				final Object[][] containerLocations = locationReceived.arrayCopy();
				
				String[] containerDimension = new String[2];
						 containerLocations[containerNameOrdinal] = containerDimension;
						 containerLocations[containerNameOrdinal][0] = ILevel.ALLMEMBER;
						 containerLocations[containerNameOrdinal][1] = container;
						 
				String[] senarioTermDimension = new String[2];
						 containerLocations[termBucketOrdinal] = senarioTermDimension;
						 containerLocations[termBucketOrdinal][0] = ILevel.ALLMEMBER;
						 containerLocations[termBucketOrdinal][1] = shortestScenarioTerm;
				
	 		   String[] senarioTermUnderlyingDimension = new String[2];
						 containerLocations[scnTermUnderLyingOrdinal] = senarioTermUnderlyingDimension;
						 containerLocations[scnTermUnderLyingOrdinal][0] = ILevel.ALLMEMBER;
						 containerLocations[scnTermUnderLyingOrdinal][1] = shortestScenarioTermUnderlying;
				
						 		 
						 
				ILocation containerLocation = new Location(containerLocations);
				
				return containerLocation;
				
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
			//locs.add(wildCardLoc);
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
	
	private boolean hasSpreadType(ILocation location) 
	{
		return location.getLevelDepth(spreadTypeOrdinal) >=2;
	}
	

	private boolean hasContainerValue(ILocation location) 
	{
		return location.getLevelDepth(containerNameOrdinal) >=2;
	} 

	private boolean doMandatoryDimensionCheck(ILocation locationReceived){
		Object[][] locationCopy = locationReceived.arrayCopy();
		if (locationCopy[containerNameOrdinal].length < 2 ) { 
		    return false;		
		}
		return true;
	}
	
	private String getDimensionValue(ILocation locationReceived, int dimOrdinal){
		Object[][] locationCopy = locationReceived.arrayCopy();
		if (locationCopy[dimOrdinal].length < 2 ) { 
		    return null;		
		} else {
		  return (String) locationCopy[dimOrdinal][1];	
		}
		
	}
	
	
}