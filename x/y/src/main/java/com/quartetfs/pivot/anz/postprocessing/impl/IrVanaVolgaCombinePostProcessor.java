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
import com.quartetfs.biz.pivot.query.aggregates.RetrievalException;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.pivot.anz.utils.ANZUtils;
import com.quartetfs.pivot.anz.utils.ServiceUtil;

@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = IrVanaVolgaCombinePostProcessor.PLUGIN_KEY)
public class IrVanaVolgaCombinePostProcessor extends APostProcessor<Object> implements IPrefetcher {

	private static final long serialVersionUID = 822120576054455333L;
	public final static String PLUGIN_KEY = "IR_VANNA_VOLGA_COMBINE";
	
	private IDimension dimTermBucket;
	private IDimension dimScenTermUnderlying;
	private IDimension dimSpreadType;
	private IDimension dimContainerName;
	 
	private int termBucketOrdinal;
	private int scnTermUnderLyingOrdinal;
	private int spreadTypeOrdinal;
	private int containerNameOrdinal;

	
	
	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();
	
	public IrVanaVolgaCombinePostProcessor(final String name, final IActivePivot pivot) {
		super(name, pivot);
	}

	public IrVanaVolgaCombinePostProcessor(final String name, final IActivePivot pivot,ServiceUtil serviceUtil) {
		super(name, pivot);
		
		
	}


	@Override
	public void init(final Properties properties) throws QuartetException {
		super.init(properties);
		analysisDimensionHelper.init(properties, pivot);

		String termBucket = properties.getProperty("dimTermBucket"); 
		String scenTermUnderlying = properties.getProperty("dimScenTermUnderlying"); 
		String spreadType = properties.getProperty("dimSpreadType");
		String containerName = properties.getProperty("dimContainerName");
		
		dimTermBucket = ANZUtils.findDimensionByName(pivot, termBucket);
		dimScenTermUnderlying = ANZUtils.findDimensionByName(pivot, scenTermUnderlying);
		dimSpreadType= ANZUtils.findDimensionByName(pivot, spreadType);
		dimContainerName = ANZUtils.findDimensionByName(pivot, containerName);
		
		termBucketOrdinal        = dimTermBucket.getOrdinal() - 1;
		scnTermUnderLyingOrdinal = dimScenTermUnderlying.getOrdinal() - 1;
		spreadTypeOrdinal        = dimSpreadType.getOrdinal() - 1;
		containerNameOrdinal     = dimContainerName.getOrdinal() -1;
		
		
		getPrefetchers().addAll( Collections.<IPrefetcher> singletonList(this) );
		
		
		
		
	}

	@Override
	public void evaluate(final ILocation locationReceived, final IAggregatesRetriever retriever) throws QuartetException {
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
					
					final double currentTermValue = (Double) cellSet.getCellValue(rowId, underlyingMeasures[0]);
				
					final String termBucket = hasTermValue(location) ? (String)location.getCoordinate(termBucketOrdinal, 1):null;
					final String underlyingTerm = hasTermUnderlyingValue(location) ? (String)location.getCoordinate(scnTermUnderLyingOrdinal, 1) : null;
					
					if(termBucket!=null && underlyingTerm!=null)//add condition if spreadtype == cpafloor
					{	
					
						double vegaValue = 0;
						
							if(hasLocationValue(location)) {
							  final String container = (String)location.getCoordinate(containerNameOrdinal , 1);
						
							  
							  if(!container.equals("IR_VEGA")) {
								  String vega = "IR_VEGA";
								  	 try {
										vegaValue = getVegaValue( createOtherContainerLocation( location, vega ), retriever ,cellSet );
									} catch (QueryException e) {
										e.printStackTrace();
									}
								
							  } else {
								
							  }
							 
							} else {
							
							}
						
						retriever.write(writeLocation, compute(underlyingTerm,termBucket,location ,currentTermValue,vegaValue)); 	
					}
					else
					{
						retriever.write(writeLocation,  currentTermValue);
					}					
				return true; 
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
			
			private double getVegaValue(ILocation vegaLocation, IAggregatesRetriever retriever,ICellSet cellSetIrVega ) throws QueryException{
				
			
				ILocation termBucketUnderlyerLoc = createWildCardLocation(vegaLocation);
				
				Set<ILocation> sLocs = new HashSet<ILocation>();
				sLocs.add(termBucketUnderlyerLoc);
				
				try {
					cellSetIrVega = retriever.retrieveAggregates( sLocs, Collections.singletonList(underlyingMeasures[0]));
				} catch (RetrievalException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				double   vegaValue = 0.0;
				try{
					vegaValue =(Double) cellSetIrVega.getCellValue( vegaLocation, underlyingMeasures[0]);//error here
				}catch(Exception e){
					e.printStackTrace();
				}
				return vegaValue;

			}
			private double compute(String underlyingTerm, String termBucket, ILocation location,double currentTermValue, double currentVegaValue) 
			{			
			
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
						Double nextVegaValue = 0.0;
						
						if(nextValue==null) {
							continue;// try for next termbucket
						} else {
							String[] containerDimension = new String[2];
							termsLocations[containerNameOrdinal] = containerDimension;
							termsLocations[containerNameOrdinal][0] = ILevel.ALLMEMBER;
							termsLocations[containerNameOrdinal][1] = "IR_VEGA";
							
							nextVegaValue=(Double)termBucketUnderlyerCellSet.getCellValue( new Location(termsLocations) , underlyingMeasures[0]);
							if(nextVegaValue==null){
								nextVegaValue = 0.0;
							}
						}
						
						return (currentTermValue-nextValue) - (currentVegaValue - nextVegaValue);
					}					
					
					 return implementCombinedNextUnderlyingTermBucket(currentTermValue,termsLocations,underlyingTermIdx,currentVegaValue);
					 
				}
				//If term bucket is longest & underlyingterm bucket is not longest 
				else if((termBucketIdx+1 == termBucketSize) && (underlyingTermIdx+1 < underlyingTermSize))
				{  
					//If calc by termbucket does't work then calc by shifting underlyingTermBucket 
						return implementCombinedNextUnderlyingTermBucket( currentTermValue,termsLocations,underlyingTermIdx,currentVegaValue);
					
				}				
				return currentTermValue;// - currentVegaValue;
			}
            
			private double implementCombinedNextUnderlyingTermBucket(double currentTermValue,final Object[][] termsLocations, int underlyingTermSize, double currentVegaValue){
				if( termsLocations[spreadTypeOrdinal].length > 1 ){
					if( !((String)termsLocations[spreadTypeOrdinal][1]).toUpperCase().equals("SWAPTION") ){
						return currentTermValue - currentVegaValue;
					} 
				}
				return calcUsingNextUnderlyingTermBucket( currentTermValue,  termsLocations,underlyingTermSize, currentVegaValue);
				
			}
			
			private double calcUsingNextUnderlyingTermBucket(double currentTermValue,final Object[][] termsLocations, int underlyingTermSize,double currentVegaValue) 
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
						Double nextVegaValue = 0.0;
						Double nextValue=(Double)termBucketUnderlyerCellSet.getCellValue( new Location(termsLocations) , underlyingMeasures[0]);
						
//				
						
						if(nextValue==null) {
							continue;// try for next termbucket						
						} else {
							String[] containerDimension = new String[2];
							termsLocations[containerNameOrdinal] = containerDimension;
							termsLocations[containerNameOrdinal][0] = ILevel.ALLMEMBER;
							termsLocations[containerNameOrdinal][1] = "IR_VEGA";
							
							nextVegaValue=(Double)termBucketUnderlyerCellSet.getCellValue( new Location(termsLocations) , underlyingMeasures[0]);
								if(nextVegaValue==null){
								nextVegaValue=0.0;
							}
							
							return (currentTermValue-nextValue); //- (currentVegaValue - nextVegaValue);
							
						}
					}
				}
				return currentTermValue;// - currentVegaValue;
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
		if(hasLocationValue(location)){
			locationCopy[containerNameOrdinal][1]=null;
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
	
	private boolean hasLocationValue(ILocation location) 
	{
		return location.getLevelDepth(containerNameOrdinal) >=2;
	}


}