package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.ILocationPattern;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.IAxisMember;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
/**
 * This Class represent "Calculated Gamma" refer requirement doc for details.
 * 
 * This value needs to be calculated for Two container
 * IR_GAMMA= PAR_DV01(Term N/A) MRESULT- (IR_GAMMA)(Term %n)MRESULT
 * GAMMA_BASIS= DELTA_BASIS(Term N/A) MRESULT- (GAMMA_BASIS)(Term %n)MRESULT
 * 
 * where n can be any term value example 1y, 2y etc.
 *
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = CalculatedDV01PostProcessor.PLUGIN_KEY)
public class CalculatedDV01PostProcessor  extends APostProcessor<Object> implements IPrefetcher{

	public static final Map<String,Integer> SCENARIO_IR_INDEX_MAPPING=new ConcurrentHashMap<String, Integer>(); 
	static{
		SCENARIO_IR_INDEX_MAPPING.put("-25 bp", 0);
		SCENARIO_IR_INDEX_MAPPING.put("-10 bp", 1);
		SCENARIO_IR_INDEX_MAPPING.put("-5 bp",  2);
		SCENARIO_IR_INDEX_MAPPING.put("Base",   3);
		SCENARIO_IR_INDEX_MAPPING.put("+5 bp",  4);
		SCENARIO_IR_INDEX_MAPPING.put("+10 bp", 5);
		SCENARIO_IR_INDEX_MAPPING.put("+25 bp", 6);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 290636684331639072L;
	public static final String PLUGIN_KEY="CALC_DV01";
	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();
	private int scenarioIrGammaOrdinal;
	protected int[] firstDiscriminatorIndices;
	
	
	   public CalculatedDV01PostProcessor(final String name, final IActivePivot pivot) {
			super(name, pivot);
		}
	
	   
	  @Override
	public void init(Properties properties1) throws QuartetException {
		super.init(properties1);
		analysisDimensionHelper.init(properties, pivot);
		
		scenarioIrGammaOrdinal=analysisDimensionHelper.getFirstDiscriminatorIndices().get(0)[0];
		
		getPrefetchers().addAll( Collections.<IPrefetcher> singletonList(this) );
		
		firstDiscriminatorIndices=analysisDimensionHelper.getFirstDiscriminatorIndices().get(0);
		
	} 
	  
	  
	@Override
	public void evaluate(final ILocation locationReceived,final IAggregatesRetriever retriever) throws QuartetException {
		// do not process locations which have only AllMember for Container or Term level
		/*Object[] termArrayx=locationReceived.arrayCopy()[scenarioIrGammaOrdinal];
		if (termArrayx.length!=2){
		//	return;
		}*/

		final ILocationPattern pattern = locationReceived.createWildCardPattern();
		final int index = pattern.getPatternIndex( firstDiscriminatorIndices[0], firstDiscriminatorIndices[1]);
		//final ILocation locationToQuery=analysisDimensionHelper.overrideOtherDiscriminatorLocation(locationReceived, null);
		final ILocation locationToQuery=analysisDimensionHelper.overrideLocation(locationReceived);
		
		
		//2- 
		final Set<String> scenarioIrGamma = new HashSet<String>();
		@SuppressWarnings("unchecked")
		final List<IAxisMember> members = (List<IAxisMember>) pivot.getDimensions().get(scenarioIrGammaOrdinal + 1).retrieveMembers(LocationUtil.copyPath(locationReceived, scenarioIrGammaOrdinal));
		for(IAxisMember m : members) {
			if(m.getDiscriminator() instanceof String)
				scenarioIrGamma.add((String) m.getDiscriminator());
		}
		
		
		
		final String sceneIrGamma;
		if(index < 0) {//we don't have wildcard on the confidence level, we received here a confidence level value as a member
			if(scenarioIrGamma.size() != 1) throw new IllegalStateException();//should be one confidence level
			sceneIrGamma = scenarioIrGamma.iterator().next();//get the confidence level we're looking at
		} else
			sceneIrGamma = null;
			
		
		List<String> measures = new ArrayList<String>();
		measures.add(underlyingMeasures[0]);
		measures.add(underlyingMeasures[1]);
		
		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singletonList(locationToQuery), measures);
		

		cellSet.forEachLocation(new ILocationProcedure() {
			
			@Override
			public boolean execute(ILocation location, int rowId) {
					/**
					 * Calculation Formula 
						base  =	mresultv - mresult			   
						shock =	mresultv*2 - mresult			 
					 */
					
					Object mResultVector = cellSet.getCellValue(rowId, underlyingMeasures[0]);       //MRESULT 
					Object mResultVVector    = cellSet.getCellValue(rowId, underlyingMeasures[1]); //MRESULTV
					
					double[] mResultVVectorValue = null;
					double[] mResultVectorValue    = null;
					
					if(mResultVVector!=null){
						if(mResultVVector instanceof double[]){
							mResultVVectorValue =	((double[])mResultVVector);	
						}
					}
					if(mResultVector!=null){
						if(mResultVector instanceof double[]){
							mResultVectorValue =	((double[])mResultVector);	
						}
					}
					if(index >= 0) {//we received wildcard location here
						final Object[] scnIrGammaTuple = pattern.extractValues(location);
						for(String sIrGamma : scenarioIrGamma) {
							final Object[] sIrGammaTupleCloned = scnIrGammaTuple.clone();
							sIrGammaTupleCloned[index] = sIrGamma;
							ILocation calcLocation = pattern.generate(sIrGammaTupleCloned);
							
							if(!sIrGamma.equals("N/A")){
								if( sIrGamma.equals("Base") ){ 
									retriever.write(calcLocation, mResultVectorValue[SCENARIO_IR_INDEX_MAPPING.get("-5 bp")] - mResultVVectorValue[SCENARIO_IR_INDEX_MAPPING.get("-5 bp")]  ) ;
									//retriever.write(calcLocation,  mResultVectorValue[SCENARIO_IR_INDEX_MAPPING.get("-1 bp")]  ) ;
								} else {
									//retriever.write(calcLocation, (mResultVVectorValue[SCENARIO_IR_INDEX_MAPPING.get(sIrGamma)] * 2) - mResultVectorValue[SCENARIO_IR_INDEX_MAPPING.get(sIrGamma)] ) ;
									retriever.write(calcLocation,  mResultVectorValue[SCENARIO_IR_INDEX_MAPPING.get(sIrGamma)] ) ;
								}	
							}
							
						}
					} else {//we received point location here
						final Object[][] locationArray = location.arrayCopy();
						String sIrGamma = (String)locationArray[firstDiscriminatorIndices[0]][firstDiscriminatorIndices[1]];
						locationArray[firstDiscriminatorIndices[0]][firstDiscriminatorIndices[1]] = sceneIrGamma;//set the confidence level instead of the DEFAULT_DISCRIMINATOR
					//	 
						if(!sceneIrGamma.equals("N/A")){
							if( sceneIrGamma.equals("Base") ){ 
								retriever.write(new Location(locationArray), mResultVectorValue[SCENARIO_IR_INDEX_MAPPING.get("-5 bp")] - mResultVVectorValue[SCENARIO_IR_INDEX_MAPPING.get("-5 bp")]  ) ;
								
								//retriever.write(new Location(locationArray), mResultVectorValue[SCENARIO_IR_INDEX_MAPPING.get("-5 bp")]  ) ;
								
							} else {
								//retriever.write(new Location(locationArray), (mResultVVectorValue[SCENARIO_IR_INDEX_MAPPING.get(sceneIrGamma)] * 2) - mResultVectorValue[SCENARIO_IR_INDEX_MAPPING.get(sceneIrGamma)] ) ;
								retriever.write(new Location(locationArray), mResultVectorValue[SCENARIO_IR_INDEX_MAPPING.get(sceneIrGamma)] ) ;
								
							}	
						} else{
							retriever.write(new Location(locationArray), "");	
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
	public Collection<ILocation> computeLocations(Collection<ILocation> locations) {
		final Set<ILocation> locs = new HashSet<ILocation>();
		for (final ILocation location : locations) {
			locs.add(location);
			
			ILocation otherLocation=generateOtherLocation(location);
			
			if (otherLocation!=null){
				locs.add(otherLocation);
			}
		}
		return locs;
	}
	
	private ILocation generateOtherLocation(ILocation location){
 		Object locationArray[][]=location.arrayCopy();
		return createAnotherLocationForGamma(locationArray);
	}
	
	private ILocation createAnotherLocationForGamma(Object[][] locationArray){
		locationArray[scenarioIrGammaOrdinal]=new Object[]{ILevel.ALLMEMBER,null};
		return new Location(locationArray);
	}
	

	@Override
	public Collection<String> computeMeasures(Collection<ILocation> arg0) {
		return Arrays.asList(underlyingMeasures);
	}

}
