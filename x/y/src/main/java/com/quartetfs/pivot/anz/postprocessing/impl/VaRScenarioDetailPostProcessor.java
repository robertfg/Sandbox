/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.postprocessing.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.ILocationPattern;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.IAxisMember;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.impl.Util;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.IDateService;
import com.quartetfs.pivot.anz.service.IPSRService;
import com.quartetfs.pivot.anz.service.impl.PSRDetail;
import com.quartetfs.pivot.anz.utils.ANZConstants;

/**
 * VaR Scenario Details PostProcessor
 * 
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = VaRScenarioDetailPostProcessor.PLUGIN_KEY)
public class VaRScenarioDetailPostProcessor extends APostProcessor<Object> implements
IPrefetcher {
	private static final long serialVersionUID = -6354043244465857209L;
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME,
			MessagesANZ.BUNDLE);
  
	public final static String PLUGIN_KEY = "VAR_SCENARIO_PP";

	// used to store the dim and level indexes we focus on
	protected int[] firstDiscriminatorIndices;
	protected int[] secondDiscriminatorIndices;
	// other analysis dim
	protected int[] otherDiscriminatorIndices;
	// psrName used to retrieve the scenario container
	protected List<String> psrName;
	// scenarioService and psrService used to retrieve the scenario container
	private IDateService dateService;
	protected IPSRService psrService;
	protected PSRDetail psrDetail;
	private int timeDimensionIdx;
	private int vectorLength;
	private String dateType;
	private String container;
	private int containerDimensionIdx;
	
	public VaRScenarioDetailPostProcessor(final String name, final IActivePivot pivot) {
		super(name, pivot);
	}

	
	public void setPsrService(final IPSRService psrService) {
		this.psrService = psrService;
	}



	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

	@Override
	public void init(final Properties properties) throws QuartetException {
		super.init(properties);
		getPrefetchers().addAll( Collections.<IPrefetcher> singletonList(this) );
		psrName = psrDetail.retrievePSR(aggregatedMeasureName);
		LOGGER.info("psrName:" + psrName);
		// populate firstDiscriminatorIndices : idx 0 for dimension / idx 1 for
		// the level

		final String[] firstDiscriminatorLevels = properties.getProperty("firstDiscriminatorLevel").split(":");
		final String[] firstDiscriminatorLevel=firstDiscriminatorLevels[0].split("@");
		final String[] secondDiscriminatorLevel=firstDiscriminatorLevels[1].split("@");

		firstDiscriminatorIndices = Util.findLevelOrdinals(pivot.getDimensions(), firstDiscriminatorLevel[1].trim(),firstDiscriminatorLevel[0].trim());
		firstDiscriminatorIndices[0]--;// we decrement it as measures are in the dimension 0 of the cube 

		secondDiscriminatorIndices = Util.findLevelOrdinals(pivot.getDimensions(), secondDiscriminatorLevel[1].trim(),secondDiscriminatorLevel[0].trim());
		secondDiscriminatorIndices[0]--;// we decrement it as measures are in the dimension 0 of the cube


		//this is needed to set the other member of the analysis dim to default
		final String[] otherDiscriminatorLevel = properties.getProperty("otherDiscriminatorLevel").split("@");
		otherDiscriminatorIndices = Util.findLevelOrdinals(	pivot.getDimensions(), otherDiscriminatorLevel[1].trim(),otherDiscriminatorLevel[0].trim());
		otherDiscriminatorIndices[0]--;// we decrement it as measures are in the
		// dimension 0 of the cube

		String timeDimensionName = properties.getProperty("timeDimension","COB Date");
		int timeDimensionOrdinal = Util.findDimension(pivot.getDimensions(), timeDimensionName);

		if (timeDimensionOrdinal < 1)
			throw new QuartetException("The time dimension '" + timeDimensionName + "' cannot be found by the post-processor.");

		IDimension timeDimension = pivot.getDimensions().get(timeDimensionOrdinal);
		this.timeDimensionIdx = timeDimension.getOrdinal() - 1;
		
		this.vectorLength     = Integer.valueOf(properties.getProperty("vectorLength"));
		this.dateType         = properties.getProperty("varDateType");
		this.container        = properties.getProperty("varContainer");
		
		int containerDimensionOrdinal = Util.findDimension(pivot.getDimensions(), "Container");
		
		IDimension containerDimension = pivot.getDimensions().get(containerDimensionOrdinal);
		
		this.containerDimensionIdx = containerDimension.getOrdinal() - 1;
		
	}


	private boolean isPsrLoaded(){
		for (String psr:psrName){
		 LOGGER.info("psrService.getContainerName(psr):" + psrService.getContainerName(psr) );
			if (psrService.getContainerName(psr)!=null){
				return true;
			}
		}	
		return false;
	}

	@Override
	public void evaluate(final ILocation locationReceived,final IAggregatesRetriever retriever) throws QuartetException {
		final Object firstdiscriminator      = locationReceived.getCoordinate(firstDiscriminatorIndices[0], firstDiscriminatorIndices[1]);
		final Object secondiscriminator      = locationReceived.getCoordinate(secondDiscriminatorIndices[0], secondDiscriminatorIndices[1]);
		final boolean scenarioNumberSelected = !ANZConstants.DEFAULT_DISCRIMINATOR.equals(firstdiscriminator);
		final boolean scenarioDateSelected   = !ANZConstants.DEFAULT_DISCRIMINATOR.equals(secondiscriminator);
		// 1-
		// if we have DEFAULT_DISCRIMINATOR for both scenario number and scenario date we skip as no detailed value at the default member
		if (!scenarioNumberSelected && !scenarioDateSelected ) {
			return;
		}

		// 2-
		// get the scenario container

		if (!isPsrLoaded()) {
			LOGGER.log(Level.WARNING,"[VaRScenarioDetailPostProcessor] The PSR ["+ psrName+"] are not available in the PSR Service, the report is probably not processed yet.");
			return;
		}



		//3-
		//flags to Check whether point or wild card location
		final ILocationPattern pattern = locationReceived.createWildCardPattern();
		final int firstIndex = pattern.getPatternIndex(firstDiscriminatorIndices[0],	firstDiscriminatorIndices[1]);
		final int secondIndex = pattern.getPatternIndex(secondDiscriminatorIndices[0],	secondDiscriminatorIndices[1]);


		// 4-
		// use the location to prefetch
		ILocation locationToQuery = prefetchLocation(locationReceived);

		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singleton(locationToQuery),Collections.singleton(underlyingMeasures[0]));
		// 5-
		// Create Helper to write to location.
		final ScenarioDetailHelper helper=new ScenarioDetailHelper(retriever, pattern, locationReceived, this.vectorLength, this.dateType);

		// 6-
		// loop over the cellSet, notice that we use the locationToQuery with
		// the VarScenarioDimension.DEFAULT_DISCRIMINATOR as we need to retrieve
		// the vector

		cellSet.forEachLocation(new ILocationProcedure() {
			@Override
			public boolean execute(final ILocation location, final int rowId) {
				final Object measure = cellSet.getCellValue(rowId,	underlyingMeasures[0]);
				if (measure instanceof double[]) {
					final double[] vector = (double[]) measure;// get the vector
					if (scenarioNumberSelected && scenarioDateSelected){
						helper.processScenarioDateNumber(firstIndex, secondIndex,location, vector);
					}else if(scenarioNumberSelected) {
						helper.processScenarioNumber(firstIndex,  location, vector);
					}else{
						Object[][] locationArray = location.arrayCopy();
						Object container[] = locationArray[containerDimensionIdx];
						if(container.length>=2){
							helper.processScenarioDate(secondIndex,  location, vector);
						} else{
							//helper.processScenarioDate(secondIndex,  location, vector);
						}
					}
				}
				return true;
			}
		});
	}





	/**
	 * This class contains all logic to generate and write data to locations.
	 * @author Quartet Financial System
	 *
	 */
	class ScenarioDetailHelper{
		private final IAggregatesRetriever retriever;
		private final ILocationPattern pattern; 
		private final ILocation locationReceived;
		private final int vectorLength;
		private final String dateType;

		public ScenarioDetailHelper(IAggregatesRetriever retriever,ILocationPattern pattern,ILocation locationReceived,int vectorLength, String dateType) {
			this.retriever=retriever;
			this.pattern=pattern;
			this.locationReceived=locationReceived;
			this.vectorLength = vectorLength;
			this.dateType = dateType;
		}


		/**
		 * Method to get Scenario Numbers for PL Vector
		 * @param locationReceived
		 * @return
		 */
		private Collection<Long> getVarScenarioNumbers() {
			Collection<Long> varScenarios=new TreeSet<Long>(  ); //Collections.reverseOrder()
			@SuppressWarnings("unchecked")
			final List<IAxisMember> members = (List<IAxisMember>) pivot.getDimensions().get(firstDiscriminatorIndices[0] + 1).
			retrieveMembers(LocationUtil.copyPath(locationReceived,	firstDiscriminatorIndices[0]));
			for (final IAxisMember m : members) {
				if (m.getDiscriminator() instanceof Long) {
			
					varScenarios.add((Long) m.getDiscriminator());
				}
			}
			
			return varScenarios;
		}

		/**
		 * Method to get Historical dates w.r.t a given COB date
		 * notice we are using location here instead of locationReceived as in locationReceived timeDimension can be Null as well. 
		 * @param location
		 * @return
		 */
		private Collection<IDate> getVarScenarioDates(ILocation location) {
			Collection<IDate> varScenariosDates=new ArrayList<IDate>();
			Object[][] locationArray = location.arrayCopy();
			Object date[] = locationArray[timeDimensionIdx];
			if (date[0]!=null){
				IDate dt = (IDate) date[0];
				varScenariosDates=dateService.getHistoryDates(dt, this.vectorLength,this.dateType); 
			}
			return varScenariosDates;
		}

		private Collection<IDate> getDailyDates(ILocation location) {
			Collection<IDate> varScenariosDates=new ArrayList<IDate>();
			Object[][] locationArray = location.arrayCopy();
			Object date[] = locationArray[timeDimensionIdx];
			if (date[0]!=null){
				IDate dt = (IDate) date[0];
				varScenariosDates=dateService.getHistoryDates(this.dateType); 
			}
			return varScenariosDates;
		}
		
		private Collection<IDate> getVarScenarioDatesByContainer(ILocation location ) {
			Collection<IDate> varScenariosDates=new ArrayList<IDate>();
			Object[][] locationArray = location.arrayCopy();
			Object date[] = locationArray[timeDimensionIdx];
			Object container[] = locationArray[containerDimensionIdx];
			
			
			String dateType = "VARDATES";
			
			if(container.length >1){
				if( ((String)container[1]).equals(ANZConstants.VAR_CONTAINER)){
					dateType = "VARDATES";
				}else if( ((String)container[1]).equals(ANZConstants.UAT_VAR_CONTAINER)){
					dateType = "VARDATESUAT";
				}else if(((String)container[1]).equals(ANZConstants.VAR_STRESS_CONTAINER)) {
					dateType = "VARSTRESSDATES";
				}else if(((String)container[1]).equals(ANZConstants.UAT_VAR_STRESS_CONTAINER)) {
					dateType = "VARSTRESSDATESUAT";
				}else if(((String)container[1]).equals(ANZConstants.SIX_YEAR_VAR_CONTAINER)) {
					dateType = "VARSIXYEARDATES";
				}
			}
			
			if (date[0]!=null){
				IDate dt = (IDate) date[0];
				varScenariosDates=dateService.getHistoryDates(dt, this.vectorLength,dateType); 
			}
			return varScenariosDates;
		}

		private int getUniqueScenrioId(Collection<Long> varScenarios){
			return varScenarios.iterator().next().intValue();
		}
		/**
		 * Unique scenario date needs to be taken from original location received. 
		 * @return
		 */
		private IDate getUniqueScenrioDate(){
			Object[][] locationArray = locationReceived.arrayCopy();
			Object[] object = locationArray[secondDiscriminatorIndices[0]];
			return (IDate) object[0];

		}

		/**
		 * This method handle the case when both Analysis dimension VaR-ScenarioDate and VaR-Scenario Nr is selected.
		 * @param scNrIndex
		 * @param scDateIndex
		 * @param location
		 * @param vector
		 */
		public void processScenarioDateNumber(final int scNrIndex,	final int scDateIndex,final ILocation location,final double[] vector) {

			Collection<IDate>varScenarioDates=getVarScenarioDates(location);
			Collection<Long>varScenarios=getVarScenarioNumbers();
			if (varScenarios.size() == 0) {
				LOGGER.log(Level.WARNING,"[VaRScenarioPostProcessor] not able to retrieve the scenarios");
				return;
			}
			//1-
			//Wild card location
			if (scNrIndex>=0 && scDateIndex>=0){
				final Object[] scenarioTuple = pattern.extractValues(location);
				Iterator<IDate> dates=varScenarioDates.iterator();

				for (final Long varScenario : varScenarios) {
					//wild card for both Scenario vector and scenario date 
					//loop over the scenarios and generate the
					// sub-location with the
					// correct value taken from the vector
					if(dates.hasNext()){ 
						final Object[] scenarioTupleCloned = scenarioTuple.clone();
						scenarioTupleCloned[scNrIndex] = varScenario;
						scenarioTupleCloned[scDateIndex]=dates.next();
						retriever.write(pattern.generate(scenarioTupleCloned),vector[varScenario.intValue()-1]);
					}				
				}

			}//2-
			// Point location for both.
			else if (scNrIndex==-1 && scDateIndex==-1){
				//point location for both scenario number and Scenario date
				IDate uniqueScenarioDate=getUniqueScenrioDate();
				int index=getDateIndex(varScenarioDates, uniqueScenarioDate);
				long uniqueScenarioId=getUniqueScenrioId(varScenarios);
				if (index+1==uniqueScenarioId){
					final Object[][] locationArray = location.arrayCopy();
					locationArray[firstDiscriminatorIndices[0]][firstDiscriminatorIndices[1]] = uniqueScenarioId;
					locationArray[secondDiscriminatorIndices[0]][secondDiscriminatorIndices[1]] = uniqueScenarioDate;
					ILocation newLocation=new Location(locationArray);
					retriever.write(newLocation,vector[index]);
				}

			}
		}


		/**
		 * This method handles the case when only Scenario Nr is selected
		 * @param scNrIndex
		 * @param mapping
		 * @param location
		 * @param vector
		 */
		public void processScenarioNumber(final int scNrIndex, final ILocation location, final double[] vector) {
			Collection<Long>varScenarios=getVarScenarioNumbers();
			
			
			
			if (scNrIndex >= 0) {// we received wildcard location here
				final Object[] scenarioTuple =  pattern.extractValues(location);

				for (final Long varScenario : varScenarios) {
					// loop over the scenarios and generate the
					// sublocation with the
					// correct value taken from the vector
					final Object[] scenarioTupleCloned = scenarioTuple.clone();
					scenarioTupleCloned[scNrIndex] = varScenario;
				
					retriever.write(pattern.generate(scenarioTupleCloned),vector[varScenario.intValue()-1]);
				}
			} else {// we received point location here
				final Object[][] locationArray = location.arrayCopy();
				long uniqueScenarioId=getUniqueScenrioId(varScenarios) - 1;
				// set the scenario id instead of the
				// DEFAULT_DISCRIMINATOR
				locationArray[firstDiscriminatorIndices[0]][firstDiscriminatorIndices[1]] = uniqueScenarioId;

				// put the correct value taken from the vector that
				// matches the scenario
				retriever.write(new Location(locationArray),vector[(int)uniqueScenarioId]);
			}
		}

		/**
		 * 
		 * @param scDateIndex
		 * @param mapping
		 * @param location
		 * @param vector
		 */
		public void processScenarioDate(final int scDateIndex, final ILocation location,final double[] vector) {
			Collection<IDate>varScenarioDates=getVarScenarioDates(location);//generic date holder
			Collection<IDate>varScenarioDatesByContainer=getVarScenarioDatesByContainer(location); //specific date holder based on container and date
			
			
			
			
			if (scDateIndex >= 0) {// we received wildcard location here
				
				final Object[] scenarioTuple =  pattern.extractValues(location);
				int scenarioCounter=0;
				
				for (final IDate varScenarioDate : varScenarioDates) {
					final Object[] scenarioTupleCloned = scenarioTuple.clone();
					scenarioTupleCloned[scDateIndex] = varScenarioDate;
					
		            
					if( varScenarioDatesByContainer.contains( varScenarioDate)){
						int idx=scenarioCounter++;
						if(dateType.equals( "VARSIXYEARDATES" )){
							retriever.write(pattern.generate(scenarioTupleCloned),vector[idx]);
							
						}else {
							if(idx<500) {
								retriever.write(pattern.generate(scenarioTupleCloned),vector[idx]);
							}
						}
					}else {
						retriever.write(pattern.generate(scenarioTupleCloned),"");	
					}
					
				}
				
				
			} else {// we received point location here
				final Object[][] locationArray = location.arrayCopy();
				IDate uniqueScenarioDate=getUniqueScenrioDate();
				// set the scenario id instead of the
				// DEFAULT_DISCRIMINATOR
				locationArray[secondDiscriminatorIndices[0]][secondDiscriminatorIndices[1]] = uniqueScenarioDate;

				// put the correct value taken from the vector that
				// matches the scenario
				int index=getDateIndex(varScenarioDatesByContainer, uniqueScenarioDate);
				if (index>-1){
					retriever.write(new Location(locationArray),vector[index]);	
				}
			}
		}


	}

	/**
	 * Helper method to retun the offset of target date from dates, where Dates is sorted collection of VaR historical Dates
	 * this index is used to find the offset in PL vector from which the value is shown against target date.
	 * @param dates
	 * @param target
	 * @return
	 */
	private int getDateIndex(Collection<IDate> dates, IDate target){
		IDate dateArray[]=new IDate[dates.size()];
		dates.toArray(dateArray);

		if (!dates.contains(target)){
			return -1;
		}
		for (int i=0;i<dateArray.length;i++){
			if (dateArray[i].equals(target)){
				//return i+1; //Nov 30 fixed
				return i;
				
			}
		}
		return -1;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.quartetfs.biz.pivot.postprocessing.IPrefetcher#computeLocations(java
	 * .util.Collection)
	 */
	@Override
	public Collection<ILocation> computeLocations(
			final Collection<ILocation> locations) {
		final Set<ILocation> locs = new HashSet<ILocation>();
		for (final ILocation l : locations) {
			locs.add(prefetchLocation(l));
		}
		return locs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.quartetfs.biz.pivot.postprocessing.IPrefetcher#computeMeasures(java
	 * .util.Collection)
	 */
	@Override
	public Collection<String> computeMeasures(
			final Collection<ILocation> locations) {
		return Arrays.asList(underlyingMeasures[0]);
	}

	private ILocation prefetchLocation(final ILocation location) {
		final Object[][] locA = location.arrayCopy();
		locA[firstDiscriminatorIndices[0]][firstDiscriminatorIndices[1]]   = ANZConstants.DEFAULT_DISCRIMINATOR;
		locA[secondDiscriminatorIndices[0]][secondDiscriminatorIndices[1]] = ANZConstants.DEFAULT_DISCRIMINATOR;
		
		if( hasValue(location, otherDiscriminatorIndices[0]) ) {
		  locA[otherDiscriminatorIndices[0]][otherDiscriminatorIndices[1]]   = this.container; // ANZConstants.DEFAULT_DISCRIMINATOR;
		}
		
		return new Location(locA);
	}

	public void setPsrDetail(PSRDetail psrDetail) {
		this.psrDetail = psrDetail;
	}

	public void setDateService(IDateService dateService) {
		this.dateService = dateService;
	}
	
	private boolean hasValue(ILocation location,int dimOrdinal) 
	{
		return location.getLevelDepth(dimOrdinal) >=2;
	}
}
