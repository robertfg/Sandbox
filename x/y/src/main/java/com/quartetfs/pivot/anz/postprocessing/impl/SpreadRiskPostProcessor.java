package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.ArrayList;
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
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.IAxisMember;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.biz.pivot.query.aggregates.RetrievalException;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.query.QueryException;
/**
 * 
 * Calculates Spread based on Spread Type Dimension
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = SpreadRiskPostProcessor.PLUGIN_KEY)
public class SpreadRiskPostProcessor extends APostProcessor<Object> implements IPrefetcher {

	private static final long serialVersionUID = 8229509990542025333L;
	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();
	public final static String PLUGIN_KEY = "SPREAD_RISK_PP";
	private IDimension dimension = null;
	private int dimensionOrdinal;
	private final int SPREADLEVEL1OFFSET=1;
	private final int SPREADLEVEL2OFFSET=2;
	private Collection<String> negativeSpreads=null;
	private final String NEGATIVESPREADS="NEGATIVESPREADS";
	private static double MINUSONE=-1.0d;
	    
    public SpreadRiskPostProcessor(final String name, final IActivePivot pivot) {
		super(name, pivot);
	}
	
	
	
	@Override
	public void init(final Properties properties) throws QuartetException {
		super.init(properties);
		analysisDimensionHelper.init(properties, pivot);
		final String dimensionName = properties.getProperty("dimensionName");
		final List<IDimension> dimensions = pivot.getDimensions();
		for (final IDimension iDimension : dimensions) {
			if (iDimension.getName().equals(dimensionName)) {
				dimension = iDimension;
				break;
			}
		}
		dimensionOrdinal = dimension.getOrdinal() - 1;
		getPrefetchers().addAll( Collections.<IPrefetcher> singletonList(this) );
		String csv=properties.getProperty(NEGATIVESPREADS);
		negativeSpreads=Arrays.asList(csv.split(","));
	}

	private ICellSet getPrefetchedCellSet(ICellSet current, ILocation location, IAggregatesRetriever retriever) throws QueryException, RetrievalException{
		ICellSet prefecthCellSet;
		Object[][]locationArray=location.arrayCopy();
		if (locationArray[dimensionOrdinal].length==1 ){
			List<ILocation> list=new ArrayList<ILocation>();
			list.add(location);
			ILocation otherLocation=getOtherLocationForSpread(location);
			otherLocation=analysisDimensionHelper.overrideOtherDiscriminatorLocation(otherLocation,null);
			list.add(otherLocation);
			prefecthCellSet = retriever.retrieveAggregates(list, Collections.singletonList(underlyingMeasures[0]));
			 return prefecthCellSet;
		}else if (locationArray[dimensionOrdinal].length==2 ){
			//e.g Allmember/null
			List<ILocation> list=new ArrayList<ILocation>();
			ILocation otherLocation=getOtherLocationForSpread(location);
			otherLocation=analysisDimensionHelper.overrideOtherDiscriminatorLocation(otherLocation,null);
			list.add(otherLocation);
			list.add(location);
			prefecthCellSet = retriever.retrieveAggregates(list, Collections.singletonList(underlyingMeasures[0]));
			return prefecthCellSet;
		}else if (locationArray[dimensionOrdinal].length==3){
			return current;
		}
		return null;
	}
	
	public Object getValueAtLevel1(Object[][] locationArray){
		Object[]content=locationArray[dimensionOrdinal];
		return content[0];
	}
	
	public Object getValueAtLevel2(Object[][] locationArray){
		Object[]content=locationArray[dimensionOrdinal];
		return content[1];
	}
	
		@Override
	public void evaluate(final ILocation locationReceived, final IAggregatesRetriever retriever) throws QuartetException {
		
		ILocation locationToQuery=analysisDimensionHelper.overrideOtherDiscriminatorLocation(locationReceived,null);
		final ILocationPattern pattern=locationReceived.createWildCardPattern();
		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singletonList(locationToQuery), Collections.singletonList(underlyingMeasures[0]));
		final List<IAxisMember> spreadsMembersAtLevel1 = getSelectedMembers(SPREADLEVEL1OFFSET);
		final List<IAxisMember> spreadsMembersAtLevel2 = getSelectedMembers(SPREADLEVEL2OFFSET);
			
		final ICellSet cellSetForPointLocations = getPrefetchedCellSet(cellSet,locationToQuery,retriever);
		if (cellSetForPointLocations==null){
			return;
		}
		
	 	cellSet.forEachLocation(new ILocationProcedure() {
			
			@Override
			public boolean execute(ILocation location, int i) {
				final Object[][] locArray = location.arrayCopy();
				Double value=null;
				if (locArray[dimensionOrdinal].length==1){
					//e.g AllMember, since we calculate Spread as value at AllMember Level for spreadType1 members
					value = getSpreadValue(cellSetForPointLocations, locArray,spreadsMembersAtLevel1);					
				}else if (locArray[dimensionOrdinal].length == 2) {
					//e.g AllMember/Bond calculate spread between spread level2 members
					value = getSpreadValue(cellSetForPointLocations, locArray,spreadsMembersAtLevel2);					
				} else if (locArray[dimensionOrdinal].length == 3){
					//e.g AllMember/Bond/Govt
					value = ((Double) cellSetForPointLocations.getCellValue(location,underlyingMeasures[0]) ); // * MINUSONE ); //
					
				}
				write(location, value,retriever,pattern);
				return true;
			}
			
			
			
			private Double getSpreadValue(final ICellSet cellSetForPointLocations,final Object[][] locArray, List<IAxisMember> spreads) {
				Double value=null;
				// Only if two members are selected then spread will be calculated at AllMember level else it will always be blank			
				if (spreads.size()==2) 
				{	
					String parent1 = ((IAxisMember)spreads.get(0)).getParent().getDiscriminator().toString();
			        String parent2 = ((IAxisMember)spreads.get(1)).getParent().getDiscriminator().toString();
			        if (!parent2.equals(parent1)) 
			        {
			            return getMeasureValue(cellSetForPointLocations, locArray);
			        }
			        
					Double spreadA =null;
					Double spreadB =null;
					if (negativeSpreads.contains(spreads.get(0).getDiscriminator().toString())){
						spreadA = getSpreadTypeValue(spreads.get(0), locArray,cellSetForPointLocations,underlyingMeasures[0]);
						spreadB = getSpreadTypeValue(spreads.get(1), locArray,cellSetForPointLocations,underlyingMeasures[0]);
					}else{
						spreadA = getSpreadTypeValue(spreads.get(1), locArray,cellSetForPointLocations,underlyingMeasures[0]);
						spreadB = getSpreadTypeValue(spreads.get(0), locArray,cellSetForPointLocations,underlyingMeasures[0]);
					}					
					// if any of the spread is null return null
					if (spreadA!=null && spreadB!=null){
						//value=calculateSpread(spreadA * MINUSONE, spreadB * MINUSONE);
						value=calculateSpread(spreadA,spreadB);
					}
				}
				else
				{
					//Invalid selection
					value = getMeasureValue(cellSetForPointLocations, locArray);
				/*
					String parent1 = ((IAxisMember)spreads.get(0)).getParent().getDiscriminator().toString();
			        String parent2 = ((IAxisMember)spreads.get(1)).getParent().getDiscriminator().toString();
			        if (!parent2.equals(parent1)) 
			        {
			            return getMeasureValue(cellSetForPointLocations, locArray);
			        }
			        
					Double spreadA =null;
					Double spreadB =null;
					if (negativeSpreads.contains(spreads.get(0).getDiscriminator().toString())){
						spreadA = getSpreadTypeValue(spreads.get(0), locArray,cellSetForPointLocations,underlyingMeasures[0]);
						spreadB = getSpreadTypeValue(spreads.get(1), locArray,cellSetForPointLocations,underlyingMeasures[0]);
					}else{
						spreadA = getSpreadTypeValue(spreads.get(1), locArray,cellSetForPointLocations,underlyingMeasures[0]);
						spreadB = getSpreadTypeValue(spreads.get(0), locArray,cellSetForPointLocations,underlyingMeasures[0]);
					}		*/			
					// if any of the spread is null return null
					/*if (spreadA!=null && spreadB!=null){
						//value=calculateSpread(spreadA * MINUSONE, spreadB * MINUSONE);
						value=calculateSpread(spreadA,spreadB);
					} else {
						if(spreadA==null){
							spreadA = 0.0;
						}
						if(spreadB==null){
							spreadB = 0.0;
						}
						
						
						value=calculateSpread(spreadA,spreadB);
						
					}*/
				}
				return value;
			}



			private Double getMeasureValue(final ICellSet cellSetForPointLocations,final Object[][] locArray) {
				Double value;
				Object otherValue =  cellSetForPointLocations.getCellValue(new Location(locArray),underlyingMeasures[0]);	
				value=otherValue==null?null:(Double)otherValue; // *MINUSONE; //
				return value;
			}
		});
	}
		
		
		@SuppressWarnings({ "unchecked" })
		private List<IAxisMember> getSelectedMembers(int depth) {
			IDimension dimension = pivot.getDimensions().get(dimensionOrdinal + 1);
			List<IAxisMember> spreads=(List<IAxisMember>) dimension.getLevels().get(depth).getMembers();
			return spreads;
		}	

		private void write(ILocation location, Double value, IAggregatesRetriever retriever, ILocationPattern pattern){
			if (value!=null){
				// here we are using pattern for the case to restore analysis dimension value (if nything other than N/A is selected then first we override with "N/A" 
				// but at the time of writing we need to rollback to original value"
				final Object[] tuple = pattern.extractValues(location);
				ILocation locationToWrite=pattern.generate(tuple);
				retriever.write(locationToWrite,value );
			}
		}	
		
		
	@Override
	public String getType() {
		return PLUGIN_KEY;
	}
	
	@Override
	public Collection<ILocation> computeLocations(final Collection<ILocation> locations) {
	Set<ILocation> results=new HashSet<ILocation>(locations);
		for (ILocation loc:locations){
			ILocation newLoc=getOtherLocationForSpread(loc);
			if (newLoc!=null){
				results.add(newLoc);
			}
		}
		return results;
	}
	
	
	
	private ILocation getOtherLocationForSpread(ILocation location){
		Object locationArray[][]=location.arrayCopy();
		// if there is AllMember then add AllMember/null as prefetch location
		if (locationArray[dimensionOrdinal].length==1){
			locationArray[dimensionOrdinal]= new Object[]{ILevel.ALLMEMBER,null};
			return new Location(locationArray);
		}else if (locationArray[dimensionOrdinal].length==2){
			locationArray[dimensionOrdinal]= new Object[]{ILevel.ALLMEMBER,null,null};
			return new Location(locationArray);
		}
		return null;
	}

	

	/**
	 * Actual formula to calculate spread, refer Requirement document for details.
	 * @param spreadA
	 * @param spreadB
	 * @return
	 */
	private Double calculateSpread(Double spreadA, Double spreadB) {
	double spread=0.0d;
		
		if ((spreadA * spreadB) < 0) {
			spread= Math.min(Math.abs(spreadA), Math.abs(spreadB));
		} 
		
		if (spreadA > 0) {
			return spread * MINUSONE;
		}		
		return spread;
		
	}
	
	/**
	 * Returns the value of a selected SpreadType, for example if spreadType="BOND" then it will construct location with AllMember/BOND and get the value of M_RESULT
	 * at That location.
	 * @param spreadType
	 * @param location
	 * @return
	 * @throws QueryException
	 */
	private Double getSpreadTypeValue(IAxisMember spreadType, Object[][] locationArray, ICellSet cellSet, String underlyingMeasure) {
		if (spreadType.getDepth()==1){
			String spreadTypeStr=spreadType.getDiscriminator().toString();
			locationArray[dimensionOrdinal]= new Object[]{ILevel.ALLMEMBER,String.valueOf(spreadTypeStr)};
		}else if (spreadType.getDepth()==2){
			String parentDesc=spreadType.getParent().getDiscriminator().toString();
			String child=spreadType.getDiscriminator().toString();
			locationArray[dimensionOrdinal]= new Object[]{ILevel.ALLMEMBER,String.valueOf(parentDesc),String.valueOf(child)};
		}
		
		Double retValue=null;
		ILocation sLoc = new Location(locationArray);
		if (cellSet.getCellValue(sLoc, underlyingMeasure) != null) {
			retValue = (Double) cellSet.getCellValue(sLoc,underlyingMeasure);
		}
		return retValue;
	}

	
	
	@Override
	public Collection<String> computeMeasures(final Collection<ILocation> locations) {
		return Arrays.asList(underlyingMeasures[0]);
	}
}

	