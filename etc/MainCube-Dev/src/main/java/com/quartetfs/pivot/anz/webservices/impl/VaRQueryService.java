/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.webservices.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jws.WebService;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.IActivePivotSchema;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.IProjection;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.context.IContextSnapshot;
import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.context.subcube.ISubCubeProperties;
import com.quartetfs.biz.pivot.context.subcube.impl.SubCubeProperties;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.impl.LocationUtil.ConvertLocationsResult;
import com.quartetfs.biz.pivot.query.IDrillthroughQuery;
import com.quartetfs.biz.pivot.query.IGetAggregatesQuery;
import com.quartetfs.biz.pivot.query.impl.GetAggregatesQuery;
import com.quartetfs.biz.pivot.query.impl.LocationLookupQuery;
import com.quartetfs.biz.pivot.webservices.impl.AManagerService;
import com.quartetfs.biz.pivot.webservices.impl.ServicesUtil;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.fwk.query.UnsupportedQueryException;
import com.quartetfs.pivot.anz.drillthrough.DrillThroughUtil;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.model.impl.Pair;
import com.quartetfs.pivot.anz.postprocessing.impl.VaRHelper;
import com.quartetfs.pivot.anz.service.IDateService;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.ANZUtils;
import com.quartetfs.pivot.anz.utils.ListProperties;
import com.quartetfs.pivot.anz.webservices.IVaRQueryService;
import com.quartetfs.tech.indexer.IField;

@WebService(name="IVaRService", 
		targetNamespace="http://webservices.quartetfs.com/activepivot",
		endpointInterface = "com.quartetfs.pivot.anz.webservices.IVaRQueryService",
		serviceName = "VaRQueryService")
	public class VaRQueryService extends AManagerService implements IVaRQueryService {
 
	public static final Map<String,String> MEASURES_MAPPING=new ConcurrentHashMap<String, String>(); 
	static{
		MEASURES_MAPPING.put(ANZConstants.ONE_DAY,  ANZConstants.ONE_DAY_VECTOR_MEASURE);
		MEASURES_MAPPING.put(ANZConstants.TEN_DAYS, ANZConstants.TEN_DAYS_VECTOR_MEASURE);
		MEASURES_MAPPING.put(ANZConstants.STRESS,   ANZConstants.STRESS_VAR_VECTOR_MEASURE);
		
		MEASURES_MAPPING.put(ANZConstants.UAT_ONE_DAY,  ANZConstants.ONE_DAY_VECTOR_MEASURE);
		MEASURES_MAPPING.put(ANZConstants.UAT_TEN_DAYS, ANZConstants.TEN_DAYS_VECTOR_MEASURE);
		MEASURES_MAPPING.put(ANZConstants.UAT_STRESS,   ANZConstants.STRESS_VAR_VECTOR_MEASURE);
		
		MEASURES_MAPPING.put(ANZConstants.VAR_1540,   ANZConstants.VAR_1540_VECTOR_MEASURE);
		
	}
	
	
	
	public static final Map<String,String> CONTAINER_MAPPING=new ConcurrentHashMap<String, String>(); 
	static{
		CONTAINER_MAPPING.put(ANZConstants.ONE_DAY,  ANZConstants.VAR_CONTAINER);
		CONTAINER_MAPPING.put(ANZConstants.TEN_DAYS,  ANZConstants.VAR_CONTAINER);
		CONTAINER_MAPPING.put(ANZConstants.STRESS,    ANZConstants.VAR_STRESS_CONTAINER);
		
		CONTAINER_MAPPING.put(ANZConstants.UAT_ONE_DAY,   ANZConstants.UAT_VAR_CONTAINER);
		CONTAINER_MAPPING.put(ANZConstants.UAT_TEN_DAYS,   ANZConstants.UAT_VAR_CONTAINER);
		CONTAINER_MAPPING.put(ANZConstants.UAT_STRESS,  ANZConstants.UAT_VAR_STRESS_CONTAINER);
		CONTAINER_MAPPING.put(ANZConstants.VAR_1540,  ANZConstants.VAR_1540_CONTAINER);
			

	}
	
	public static final Map<String,String> VAR_DATE_TYPE_MAPPING=new ConcurrentHashMap<String, String>(); 
	static{
		VAR_DATE_TYPE_MAPPING.put(ANZConstants.ONE_DAY,  "VARDATES");
		VAR_DATE_TYPE_MAPPING.put(ANZConstants.TEN_DAYS, "VARDATES");
		VAR_DATE_TYPE_MAPPING.put(ANZConstants.STRESS,   "VARSTRESSDATES");
		
		VAR_DATE_TYPE_MAPPING.put(ANZConstants.UAT_ONE_DAY,  "VARDATESUAT");
		VAR_DATE_TYPE_MAPPING.put(ANZConstants.UAT_TEN_DAYS, "VARDATESUAT");
		VAR_DATE_TYPE_MAPPING.put(ANZConstants.UAT_STRESS,   "VARSTRESSDATESUAT");
		VAR_DATE_TYPE_MAPPING.put(ANZConstants.VAR_1540,   "VARSIXYEARDATES");
		
	}
	
	
	
	public static final Map<String,Integer> VECTOR_LENGTH=new ConcurrentHashMap<String,Integer>(); 
	static{
		VECTOR_LENGTH.put(ANZConstants.ONE_DAY, ANZConstants.VECTOR_LENGTH);
		VECTOR_LENGTH.put(ANZConstants.TEN_DAYS, ANZConstants.VECTOR_LENGTH);
		VECTOR_LENGTH.put(ANZConstants.STRESS, ANZConstants.VECTOR_STRESS_LENGTH);
	
		VECTOR_LENGTH.put(ANZConstants.UAT_ONE_DAY,  ANZConstants.VECTOR_LENGTH);
		VECTOR_LENGTH.put(ANZConstants.UAT_TEN_DAYS, ANZConstants.VECTOR_LENGTH);
		VECTOR_LENGTH.put(ANZConstants.UAT_STRESS,  ANZConstants.VECTOR_STRESS_LENGTH);
		VECTOR_LENGTH.put(ANZConstants.VAR_1540,   ANZConstants.VAR_1540_VECTOR_LENGTH);
		
	}
	

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	protected  IDateService dateService;
	protected Properties originalDrillThroughHeaders;
	protected LinkedHashSet<String> attributesHeader;
	protected Map<String ,IField> outputFields;
	protected Set<String> headersToRemove;

	protected DrillThroughUtil drillUtil;
	
	public VarDataExtractDTO varDataExtract(VarQueryDTO varQueryDTO) {
		VarDataExtractDTO result = new VarDataExtractDTO();
		IActivePivot pivot = null;
		IContextSnapshot oldContext=null;
		try{
			//retrieve and check some params
			if (varQueryDTO == null){
				LOGGER.log(Level.SEVERE, MessagesANZ.NULL_DTO,"VarQueryDTO");
				return result;	
			}
			IDrillthroughQuery dtQuery = varQueryDTO.getDtQuery();
			if (dtQuery == null){
				LOGGER.log(Level.SEVERE, MessagesANZ.NULL_DT_QUERY);
				return result;	
			}

			//check the pivot
			String pivotId = dtQuery.getPivotId();
			pivot = checkAndRetrievePivot(pivotId);

			//setup the mapping
			if (ANZConstants.OFFSET_MAPPING.isEmpty()){
				populateMapping();
			}

			String containerName =  ANZUtils.getDrillThroughContainer(dtQuery);
		
			LOGGER.log(Level.INFO,"=======================================" + containerName + " ==================================================");
			
			if(containerName.equals(ANZConstants.VAR_CONTAINER)){
				containerName = "VAR_1D";
			}		
			
			ListProperties headersFromDb = drillUtil.getDrillThroughHeaders(containerName);
			
			attributesHeader = new LinkedHashSet<String>();
			Properties headerProperties = null;
			
			if(headersFromDb==null){
				headerProperties =originalDrillThroughHeaders;
				 for(Entry<Object, Object> e : originalDrillThroughHeaders.entrySet()) {
					 attributesHeader.add(  (String)e.getKey() );
			     }
				 
				 
			} else {
				 headerProperties = headersFromDb;
				 List<String> dimensionToExtract = new ArrayList<String>();
				 Enumeration<Object> hpKeys = headerProperties.keys();
				 LOGGER.info("hpKeys.toString():"+ hpKeys.toString() );
				 
				 while(hpKeys.hasMoreElements()){
					dimensionToExtract.add( (String)hpKeys.nextElement());
				 }
				 
				 
				 for (String header : dimensionToExtract) {
					 attributesHeader.add( header );
				 }
					
			}
			
			if (headersToRemove != null || !headersToRemove.isEmpty()){
				attributesHeader.removeAll(headersToRemove);
			}
			
			
			//get the location
			if (dtQuery.getLocations() == null || dtQuery.getLocations().isEmpty() || dtQuery.getLocations().size() >1){
				LOGGER.log(Level.SEVERE, MessagesANZ.LOCATION_ISSUE);
				return result;	
			}
			String location = dtQuery.getLocations().iterator().next().toString();//get the first location, we're supposed to receive only one
			LOGGER.log(Level.INFO , "location:" + location);
			
			String varType =  this.getVarType(getContainer(dtQuery), varQueryDTO);
			
			LOGGER.log(Level.INFO , "VarType:" + varType);
		    
			if (varType ==null || !ANZConstants.VAR_TYPES.contains(varType)){
				LOGGER.log(Level.SEVERE, MessagesANZ.WRONG_VAR_TYPE, ANZConstants.VAR_TYPES.toString());
				return result;
			}

			//check from and to
			int from = varQueryDTO.getFrom();
			int to = varQueryDTO.getTo();
			if ( !(from == to && from == 0)){
				if(!isValid(from, to)){
					LOGGER.log(Level.SEVERE, MessagesANZ.FROM_TO_INCONSISTENCY, new Object[]{from,to});
					return result;					
				}
			}

			
			ILocation locationToQuery = stringToLocation(location, CONTAINER_MAPPING.get( varType ));
			
			if(locationToQuery.isRange()){
				LOGGER.log(Level.SEVERE, MessagesANZ.RANGE_LOCATION_FORBIDDEN);
				return result;
			} 


			//convert the location: date member will be converted to IDate
			ConvertLocationsResult conversionResult = LocationUtil.convertLocations(pivot.getDimensions(), Collections.singletonList(locationToQuery));
			Set<ILocation> locations = conversionResult.getConvertedLocations();
	
			//Execute the query contextually
			if (!isContextValid(dtQuery)) return result;
			oldContext = ServicesUtil.applyContextValues(pivot, dtQuery.getContextValues(), true);
 
			List<IProjection> results = getProjections(pivot, dtQuery,locations);
			if (results ==null || results.isEmpty()){
				LOGGER.log(Level.INFO, MessagesANZ.RESULTS_SIZE,new Object[]{"varDataExtract", results.size()});
				return result;
			}
			//trace
			LOGGER.log(Level.INFO, MessagesANZ.RESULTS_SIZE,new Object[]{"varDataExtract", results.size()});

			List<VarDealVectorDTO> deals  = new ArrayList<VarDealVectorDTO>(results.size()); 

			//build and populate the DTO collection
			for(IProjection projection : results){
				//trace
				Long dealNumber =  (Long) projection.getContent()[ANZConstants.OFFSET_MAPPING.get(ANZConstants.DEAL_NUM)];
				double[] vector = projection.getContent()[ANZConstants.OFFSET_MAPPING.get(varType)] != null ? (double[]) projection.getContent()[ANZConstants.OFFSET_MAPPING.get(varType)] : null;

				if (dealNumber != null && vector != null ){
					deals.add(new VarDealVectorDTO(dealNumber.longValue(), extractSubVector(vector, from, to), retrieveAttributes(projection, outputFields)));
				}
			} 
			//set the scenarioDate, same from / to params are applied on the dates vector
			result.setScenarioDates( extractSubVector(String.class,
			dateService.retrieveDates(extractCobDate(locations.iterator().next()), VAR_DATE_TYPE_MAPPING.get( varType ) ),  from, to ) );

			//set headers
			//result.setAttributesHeader(attributesHeader);
			result.setAttributesHeader(ANZUtils.aliasHeaders(attributesHeader,headerProperties));
			
			//set the deals
			result.setDeals(deals);

			//trace
			LOGGER.log(Level.INFO, MessagesANZ.RESULTS_TO_SEND,new Object[]{"varDataExtractResults", result.getDeals().size()});

		}catch(Exception e){
			String msg = ANZUtils.formatMessage(MessagesANZ.WS_ERR, "varDataExtract");
			LOGGER.log(Level.SEVERE, msg,e);		
		}finally{
			if (oldContext != null){
				ServicesUtil.replaceContextValues(pivot, oldContext);
			}
		}
		return result;
	}
   
	private String[] reverseArray(String[] arr){
		String[] ret = new String[arr.length];
		int idx = 0;
		
		for (int i = arr.length -1; i >= 0 ; i--) {
			ret[idx] = arr[i];
			idx++;
		}
		return ret;
	}


	public VarDrillthroughDTO varDrillthrough(VarQueryDTO varQueryDTO) {
		VarDrillthroughDTO result = new VarDrillthroughDTO();
		IActivePivot pivot = null;
		IContextSnapshot oldContext=null;
		try{
			//retrieve and check some params
			if (varQueryDTO == null){
				LOGGER.log(Level.SEVERE, MessagesANZ.NULL_DTO);
				return result;	
			}
			IDrillthroughQuery dtQuery = varQueryDTO.getDtQuery();
			if (dtQuery == null){
				LOGGER.log(Level.SEVERE, MessagesANZ.NULL_DT_QUERY);
				return result;	
			}

			//check the pivot
			String pivotId = dtQuery.getPivotId();
			pivot = checkAndRetrievePivot(pivotId);

			//setup the mapping
			if (ANZConstants.OFFSET_MAPPING.isEmpty()){
				populateMapping();
			}

			LOGGER.log(Level.INFO,"===============================XXXXXXXXXXXXXXXXX==========================================================");
			ListProperties headersFromDb = drillUtil.getDrillThroughHeaders( ANZUtils.getDrillThroughContainer(dtQuery));
						attributesHeader = new LinkedHashSet<String>();
			if(headersFromDb==null){
				
				 for(Entry<Object, Object> e : originalDrillThroughHeaders.entrySet()) {
					 attributesHeader.add(  (String)e.getValue() );
			      }
				 
					
			} else {
				
				 for( Object e : headersFromDb.values() ) {
					 attributesHeader.add(  (String)e );
			      }
			} 

			if (headersToRemove != null || !headersToRemove.isEmpty()){
				attributesHeader.removeAll(headersToRemove);
			}
			
			
			
			//get the location
			if (dtQuery.getLocations() == null || dtQuery.getLocations().isEmpty() || dtQuery.getLocations().size() >1){
				LOGGER.log(Level.SEVERE, MessagesANZ.LOCATION_ISSUE);
				return result;	
			}
			String location = dtQuery.getLocations().iterator().next().toString();//get the first location, we're supposed to receive only one
			LOGGER.log(Level.INFO , "location:" + location);
			
			 
			 
						 
			
			
			
			//check the varType
			String varType =  this.getVarType(getContainer(dtQuery), varQueryDTO);
 
			LOGGER.log(Level.INFO , "VarType:" + varType);
		    
			if (varType ==null || !ANZConstants.VAR_TYPES.contains(varType)){
				LOGGER.log(Level.SEVERE, MessagesANZ.WRONG_VAR_TYPE, ANZConstants.VAR_TYPES.toString());
				return result;
			}
			//confidenceLevel
			double confidenceLevel = varQueryDTO.getConfidenceLevel();
			if (!ANZConstants.CONFIDENCE_LEVELS.contains(Double.valueOf(confidenceLevel))){
				LOGGER.log(Level.SEVERE, MessagesANZ.WRONG_CONFIDENCE_LEVEL, ANZConstants.CONFIDENCE_LEVELS.toString());
				return result;
			}

			
			//ILocation locationToQuery = stringToLocation(location,ANZConstants.VAR_CONTAINER); 
			ILocation locationToQuery = stringToLocation(location,CONTAINER_MAPPING.get( varType)); 
			
			 
			
			if(locationToQuery.isRange()){
				LOGGER.log(Level.SEVERE, MessagesANZ.RANGE_LOCATION_FORBIDDEN);
				return result;
			}

			//convert the location: date member will be converted to IDate
			ConvertLocationsResult conversionResult = LocationUtil.convertLocations(pivot.getDimensions(), Collections.singletonList(locationToQuery));
			Set<ILocation> locations = conversionResult.getConvertedLocations();

			//Execute the query contextually
			if (!isContextValid(dtQuery)) return result;
			oldContext = ServicesUtil.applyContextValues(pivot, dtQuery.getContextValues(), true);

			List<IProjection> results = getProjections(pivot, dtQuery,locations);
			if (results ==null || results.isEmpty()){
				LOGGER.log(Level.INFO, MessagesANZ.RESULTS_SIZE,new Object[]{"varDrillthrough", results.size()});
				return result;
			}
			//trace
			LOGGER.log(Level.INFO, MessagesANZ.RESULTS_SIZE,new Object[]{"varDrillthrough", results.size()});

			//get the index that matches the confidenceLevel
//			int index = VaRHelper.getIndexFromVectorLength(ANZConstants.VECTOR_LENGTH, confidenceLevel); //vectorlength
			int index = VaRHelper.getIndexFromVectorLength(VECTOR_LENGTH.get( varType ), confidenceLevel); //vectorlength
			 
			//get the vector, notice that the measures should not be hidden else we can not retrieve them
			IGetAggregatesQuery gaquery = new GetAggregatesQuery(locations,Arrays.asList(MEASURES_MAPPING.get(varType)));
			ICellSet cellSet = pivot.execute(gaquery);
			ILocation locationToQueryConverted = locations.iterator().next();
			double[] vector = (double[]) cellSet.getCellValue(locationToQueryConverted,MEASURES_MAPPING.get(varType));
			if (vector == null){
				LOGGER.log(Level.INFO, MessagesANZ.VECTOR_NULL,locationToQueryConverted.toString());
				return result;
			}

			//retrieve the scenario name
			int scenarioName = retrieveScenarioName(vector, index);

			//retrieve the dates from dateService
			String scenarioDate = retrieveScenarioDate(locationToQueryConverted, scenarioName-1,VAR_DATE_TYPE_MAPPING.get( varType ));//-1 here because scenario names are from 1 to 500 however vector offsets are from 0 to 499



			//build and populate the DTO collection
			List<VarDealValueDTO> deals = new ArrayList<VarDealValueDTO>(results.size());

			for(IProjection projection : results){
				//trace
				Long dealNumber =  (Long) projection.getContent()[ANZConstants.OFFSET_MAPPING.get(ANZConstants.DEAL_NUM)];
				double[] childVector = (double[]) projection.getContent()[ANZConstants.OFFSET_MAPPING.get(varType)];

				if (dealNumber != null && childVector != null){
					//-1 here because scenario names are from 1 to 500 however vector offsets are from 0 to 499
					deals.add(new VarDealValueDTO(dealNumber.longValue(), childVector[scenarioName-1], retrieveAttributes(projection, outputFields)));
				}
			}
			//set the scenario index
			result.setScenarioIndex(scenarioName);

			//set the scenarioDate
			result.setScenarioDate(scenarioDate);

			//set headers
			result.setAttributesHeader(attributesHeader);

			//set the deals
			result.setDeals(deals);  

			//trace
			LOGGER.log(Level.INFO, MessagesANZ.RESULTS_TO_SEND,new Object[]{"varDrillthroughtResults", result.getDeals().size()});


		}catch(Exception e){
			String msg = ANZUtils.formatMessage(MessagesANZ.WS_ERR, "varDrillthrough");
			LOGGER.log(Level.SEVERE, msg,e);
		}finally{
			if (oldContext != null){
				ServicesUtil.replaceContextValues(pivot, oldContext);
			}		}
		return result;

	}



	protected List<IProjection> getProjections(IActivePivot pivot,IDrillthroughQuery dtQuery, Set<ILocation> locations)
			throws UnsupportedQueryException, QueryException {
		//perform the query on the converted location with the maxResults
		int maxResults = dtQuery.getMaxResults();
		LocationLookupQuery llquery = new LocationLookupQuery(locations);
		llquery.setMaxResults(maxResults);
		List<IProjection> results = pivot.execute(llquery);
		return results;
	}

	protected boolean isContextValid(IDrillthroughQuery dtQuery){
		if (dtQuery.getContextValues()==null){
			return true;
		}
		for (IContextValue contextValue : dtQuery.getContextValues()) {
			if (contextValue instanceof ISubCubeProperties){
				Set<List<?>> members = ((ISubCubeProperties)contextValue).getGrantedMembers(ANZConstants.COB_DATE_DIM_NAME);
				if (members != null && !members.isEmpty()){
					if (members.size() > 1){
						LOGGER.log(Level.SEVERE, MessagesANZ.MULTISELECT_COB_FORBIDDEN);
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private String retrieveScenarioDate(ILocation location, int scenarioIndex, String varType){
		String scenarioDateStr = ANZConstants.UNAVAILABLE;
		IDate scenarioDate = dateService.retrieveDateByIndex(extractCobDate(location), scenarioIndex, varType);
		if (scenarioDate == null) return scenarioDateStr;
		StringBuilder sb = new StringBuilder();
		sb.append(scenarioDate.day()).append("/").append(scenarioDate.month()).append("/").append(scenarioDate.year());
		return sb.toString();
	}

	private IDate extractCobDate(ILocation location){
		return (IDate) location.getCoordinate(1, 0);
	}
	private int retrieveScenarioName(double[] vector, int index){
		//populate the Pair[] array with vector, we preserve the orginal index after sorting Pair[] array
		Pair[] vectorSorted = new Pair[vector.length];
		for (int i=0;i<vector.length;i++){
			vectorSorted[i]=new Pair(i+1,vector[i]);//+1 for i here because scenario name 1 is the one you have at offset zero
		}
		Arrays.sort(vectorSorted);
		return vectorSorted[index].getOriginalIndex();//getIndex returns the scenario name
	}


	@SuppressWarnings("unchecked")
	private <T> T[] extractSubVector(Class<T> clazz, T[] originalVector, int from, int to) {
		T[] result;
		originalVector = (T[]) reverseArray( (String[]) originalVector);
		if(from==0 && to==0){
			return  originalVector;
		}else {
			
			result = (T[]) Array.newInstance(clazz, to-from+1);
			System.arraycopy(originalVector, from-1, result, 0, result.length);
		}
		return result;
	}

	private double[] extractSubVector(double[] originalVector, int from, int to){
		double[] result;
		if(from==0 && to==0){
			return originalVector;
		}else {
			result = new double[to-from+1];
			System.arraycopy(originalVector, from-1, result, 0, result.length);
		}
		return result;
	}

	private boolean isValid(int from, int to){
	/*	//from < to
		if(to-from < 0)
			return false;
		//from
		if  (from <= 0 || from > ANZConstants.VECTOR_LENGTH)
			return false;

		//to
		if  (to <= 0 || to > ANZConstants.VECTOR_LENGTH)
			return false;*/

		return true;
	}

	private void populateMapping()throws Exception{
		IActivePivotSchema schema = checkAndRetrieveSchema(ANZConstants.SCHEMA_NAME);
		outputFields = schema.getClassifier().getOutputFields();
		
		ANZConstants.OFFSET_MAPPING.put(ANZConstants.ONE_DAY,  Integer.parseInt(outputFields.get(ANZConstants.ONE_DAY_FIELD).getProperty().getExpression()));
		ANZConstants.OFFSET_MAPPING.put(ANZConstants.TEN_DAYS, Integer.parseInt(outputFields.get(ANZConstants.TEN_DAYS_FIELD).getProperty().getExpression()));
		
		ANZConstants.OFFSET_MAPPING.put(ANZConstants.DEAL_NUM, Integer.parseInt(outputFields.get(ANZConstants.DEAL_NUM_FIELD).getProperty().getExpression()));
	
		ANZConstants.OFFSET_MAPPING.put(ANZConstants.STRESS, Integer.parseInt(outputFields.get(ANZConstants.STRESS_FIELD).getProperty().getExpression()));
		
	
		ANZConstants.OFFSET_MAPPING.put(ANZConstants.UAT_ONE_DAY,  Integer.parseInt(outputFields.get(ANZConstants.ONE_DAY_FIELD).getProperty().getExpression()));
		ANZConstants.OFFSET_MAPPING.put(ANZConstants.UAT_TEN_DAYS, Integer.parseInt(outputFields.get(ANZConstants.TEN_DAYS_FIELD).getProperty().getExpression()));
		
	
		ANZConstants.OFFSET_MAPPING.put(ANZConstants.UAT_STRESS, Integer.parseInt(outputFields.get(ANZConstants.STRESS_FIELD).getProperty().getExpression()));
	
		ANZConstants.OFFSET_MAPPING.put(ANZConstants.VAR_1540, Integer.parseInt(outputFields.get(ANZConstants.VAR_1540_FIELD).getProperty().getExpression()));
		  

	}

	protected boolean populateAttributesHeader(){
		if (originalDrillThroughHeaders == null || originalDrillThroughHeaders.isEmpty()) return false;
		
		 for(Entry<Object, Object> e : originalDrillThroughHeaders.entrySet()) {
			 attributesHeader.add(  (String)e.getValue() );
	      }
		 
		
		
		if (headersToRemove != null || !headersToRemove.isEmpty()){
			attributesHeader.removeAll(headersToRemove);
		}
		return true;

	}
	protected static ILocation stringToLocation(String stringLocation,String containerName) {
		String[] levels = stringLocation.split(Pattern.quote(ILocation.DIMENSION_SEPARATOR));
		String[][] results = new String[levels.length][];
		//override the container in order to target container
		results[0] = new String[2];
		results[0][0] = ILevel.ALLMEMBER;
		results[0][1] = containerName;

		//then loop over other levels
		for (int i = 1; i < levels.length; i++) {
			String[] members = levels[i].split(Pattern.quote(ILocation.LEVEL_SEPARATOR), -1);
			results[i] = new String[members.length];
			for (int j = 0; j < members.length; j++) {
				if (members[j].equals("null"))
					results[i][j] = null;
				else
					results[i][j] = members[j];
			}
		}
		return new Location(results);
	}


	protected List<Object> retrieveAttributes(IProjection projection,Map<String ,IField> outputFields){
		List<Object>attributeValue = new ArrayList<Object>();
		for (String header : attributesHeader) {
			attributeValue.add(projection.getContent()[ Integer.valueOf( outputFields.get(header).getProperty().getExpression())]);	
		}
		return attributeValue;
	}

	public void setDateService(IDateService dateService) {
		this.dateService = dateService;
	}

	public void setOriginalDrillThroughHeaders(
			Properties originalDrillThroughHeaders) {
		this.originalDrillThroughHeaders = originalDrillThroughHeaders;
	}



	public void setHeadersToRemove(Set<String> headersToRemove) {
		this.headersToRemove = headersToRemove;
	}
	

	protected static String getContainer(IDrillthroughQuery dtQuery) {
				       SubCubeProperties subProps = (SubCubeProperties)dtQuery.getContextValues().get(0);
				  		Set<List<?>> dimContainer = subProps.getAllGrantedMembers().get("Container");
				  		List<String> container = (List<String>)dimContainer.iterator().next();
				  		
						if(container!=null ) {
							return container.get(1);
						}
				  return ANZConstants.VAR_CONTAINER;
	} 
	
	private String getVarType(String containerName, VarQueryDTO varQueryDTO){
		String varType = varQueryDTO.getVarType();
		if(containerName.equals(ANZConstants.UAT_VAR_CONTAINER) || containerName.equals(ANZConstants.UAT_STRESS ) ){
	    	varType = "UAT_" +varQueryDTO.getVarType();
		}else if(containerName.equals("UAT_VAR_STRESS" )){	
			varType = ANZConstants.UAT_STRESS;
	    }else if(containerName.equals(ANZConstants.VAR_1540_CONTAINER) ){
	    	varType = ANZConstants.VAR_1540;
	    }
		return varType;
	}

	public DrillThroughUtil getDrillUtil() {
		return drillUtil;
	}

	public void setDrillUtil(DrillThroughUtil drillUtil) {
		this.drillUtil = drillUtil;
	}
}
