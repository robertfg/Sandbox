package com.quartetfs.pivot.anz.webservices.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.jws.WebService;

import org.apache.commons.lang.Validate;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StopWatch;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.context.subcube.impl.SubCubeProperties;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.definitions.IMeasureDescription;
import com.quartetfs.biz.pivot.dto.DrillthroughHeaderDTO;
import com.quartetfs.biz.pivot.dto.DrillthroughResultDTO;
import com.quartetfs.biz.pivot.dto.DrillthroughRowDTO;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.logging.impl.MessagesServer;
import com.quartetfs.biz.pivot.query.IDrillthroughQuery;
import com.quartetfs.biz.pivot.query.impl.DrillthroughQuery;
import com.quartetfs.biz.pivot.webservices.IQueriesService;
import com.quartetfs.pivot.anz.drillthrough.DrillThroughConstants;
import com.quartetfs.pivot.anz.drillthrough.DrillThroughConstants.Headers;
import com.quartetfs.pivot.anz.drillthrough.TransposeRequestInput;
import com.quartetfs.pivot.anz.drillthrough.TransposeRequestInput.ColumnDetails;
import com.quartetfs.pivot.anz.drillthrough.TransposeRequestInput.DimensionDetails;
import com.quartetfs.pivot.anz.drillthrough.TransposeRequestInput.FilterDetails;
import com.quartetfs.pivot.anz.drillthrough.impl.DrillThroughRowContent;
import com.quartetfs.pivot.anz.drillthrough.impl.TransposeDataHelperSR;
import com.quartetfs.pivot.anz.utils.ANZUtils;
import com.quartetfs.pivot.anz.utils.TenorComparator;
import com.quartetfs.pivot.anz.webservices.ITransposeDrillThroughServiceSR;
import com.quartetfs.tech.point.impl.Point;
@WebService(name="ITransposeDrillThroughServiceSR",
		targetNamespace="http://webservices.quartetfs.com/activepivot",
		endpointInterface = "com.quartetfs.pivot.anz.webservices.ITransposeDrillThroughServiceSR",
		serviceName = "TransposeDrillThroughSR")
@ManagedResource
public class TransposeDrillThroughServiceSR implements ITransposeDrillThroughServiceSR
{
	
	private IQueriesService queryService;
	static Logger logger = Logger.getLogger(MessagesServer.LOGGER_NAME, MessagesServer.BUNDLE);
	
	private IActivePivotManager manager;
	private Properties drillThroughAliases;
	private Properties transposeProperties;
	
	private Set<String> excludedFields=new HashSet<String>();		
	public static String ScenarioTermUnderlyingColumn="scenarioTermUnderlying";
	public static String spreadTypeColName="spreadType";
	public static String transposeColumn="Term";	
	private String measureName="M_RESULT";
	private String dealNumberCol="M_DEALNUM";
	private String termDimensionName;
	private String underlyingTermDimensionName;
	
	private volatile boolean debug;
		
	public TransposeDrillThroughServiceSR(IActivePivotManager manager,Properties drillThroughAliases, Properties transposeProperties) {
		super();
		this.manager = manager;
		this.drillThroughAliases = drillThroughAliases;
		this.transposeProperties = transposeProperties;
		
		init();
	}

	
	private Map<Headers,Integer> identifyHeadersIndex(List<String> headersCols )
	{
		Map<Headers,Integer> headerMap = new LinkedHashMap<Headers,Integer>();		
				
		headerMap.put(Headers.TRANSPOSE, headersCols.indexOf(transposeColumn));
		headerMap.put(Headers.MEASURE, headersCols.indexOf(measureName));
		headerMap.put(Headers.DEALNUMBER, headersCols.indexOf(dealNumberCol));
		headerMap.put(Headers.SPREADTYPE, headersCols.indexOf(spreadTypeColName));
		headerMap.put(Headers.SCENARIOTERMUNDERLYING, headersCols.indexOf(ScenarioTermUnderlyingColumn));
		
		for(Map.Entry<Headers,Integer> entry: headerMap.entrySet())
		{
			Validate.isTrue(entry.getValue()!=-1, String.format("Unable to resolve header cols. Header cols value %s ", entry));
		}		
		return headerMap;
	}
			
	@Override
	public DrillthroughResultDTO execute(IDrillthroughQuery query) 
	{
		StopWatch stopWatch = new StopWatch();
		
		stopWatch.start("DrillThrough");
		
		IActivePivot pivot = checkAndRetrievePivot(query.getPivotId());
		ILocation location= query.getLocations().iterator().next();		
		logger.info("Requested for Transpose: "+query);
		ILocation newLocation = overrideLocationIfRequired(pivot, location);
		String termFilter,underlyingtermFilter;
		termFilter=underlyingtermFilter=null;
		
		SubCubeProperties subCubeProps = (SubCubeProperties)query.getContextValues().get(0).clone()  ;
		List<String> scnTerm = (List<String>)sortSubCubeProp(subCubeProps);
		
		if(!newLocation.equals(location))
		{
			termFilter=extractTerm(location, pivot);
			underlyingtermFilter = extractUnderlyingTerm(location, pivot);
			
			logger.info("Location changed to range location:" + newLocation);
			DrillthroughQuery updatedQuery = new DrillthroughQuery(Collections.singletonList(newLocation), query.getFirstResult(), query.getMaxResults());
			updatedQuery.setContextValues(query.getContextValues());
			updatedQuery.setPivotId(query.getPivotId());
			query=updatedQuery;
		}
		
		checkIfNeedNextTermLocation( location,query,pivot);
		
		DrillthroughResultDTO result=queryService.execute(query);
		logger.info("original row count : "+result.getEstimatedTotalRowCount());
		
	//	result = checkIfNeedResultTrim( subCubeProps, result,pivot );
		
		stopWatch.stop();		
 		 
		if (result.getEstimatedTotalRowCount()<=0){
			return result;
		}
		
		logger.info("Transpose Started");
		stopWatch.start("Transpose");
			
		DrillthroughResultDTO newResultDTO = createTransposeHelper(pivot,result.getHeaders(),termFilter,underlyingtermFilter,result.getEstimatedTotalRowCount()).transpose(result, scnTerm );
		stopWatch.stop();	
		
		if (isDebug())
		{
			stopWatch.start("Creating file");
			logger.info("Debug Enabled. Creating file");
			writeToFile(query,newResultDTO);
			stopWatch.stop();
		}
		
		logger.info("Time taken to Process Request: "  + stopWatch);
		return newResultDTO;
	}


	private ILocation overrideLocationIfRequired(IActivePivot pivot,
			ILocation location) {
		IDimension termDim = ANZUtils.findDimensionByName(pivot, termDimensionName);
		IDimension underlyerTermDim = ANZUtils.findDimensionByName(pivot, underlyingTermDimensionName);
		Validate.notNull(termDim, "Term dimension is null");
		Validate.notNull(underlyerTermDim, "underlyerTermDim dimension is null");
		
		ILocation newLocation = createWildCardLocation(location,termDim,underlyerTermDim);
		return newLocation;
	}
	
	public String extractTerm(ILocation location,IActivePivot pivot)
	{
		IDimension termDim = ANZUtils.findDimensionByName(pivot, termDimensionName);
		if(hasTermValue(location,termDim))
		{
			return (String)location.getCoordinate(termDim.getOrdinal()-1, 1);
		}
		return null;
	}
	
	public String extractUnderlyingTerm(ILocation location,IActivePivot pivot)
	{
		IDimension termDim = ANZUtils.findDimensionByName(pivot, underlyingTermDimensionName);
		if(hasTermValue(location,termDim))
		{
			return (String)location.getCoordinate(termDim.getOrdinal()-1, 1);
		}
		return null;
	}
	
	private TransposeDataHelperSR createTransposeHelper(IActivePivot pivot, List<DrillthroughHeaderDTO> headers,String termFilter,String underlyingtermFilter,int rowCount)
	{
		List<String> headersCols = transformHeader(headers);
		Set<String> aggregateFields = identifyColsToAggregate(pivot);
		Map<Headers,Integer> headerIdx = identifyHeadersIndex(headersCols);
		
		Map<Point, DrillThroughRowContent> transposedData=new HashMap<Point, DrillThroughRowContent>(rowCount/2);	
		DimensionDetails dimDetails = new DimensionDetails(ANZUtils.findDimensionByName(pivot, termDimensionName), ANZUtils.findDimensionByName(pivot, underlyingTermDimensionName));
		FilterDetails filter = new FilterDetails(underlyingtermFilter, termFilter);
		ColumnDetails columnDetails = new ColumnDetails(headersCols, excludedFields, headerIdx,aggregateFields);
		
		TransposeRequestInput transposeDTO = new TransposeRequestInput(pivot,columnDetails,dimDetails,filter,transposedData) ;
		
		TransposeDataHelperSR helper = new TransposeDataHelperSR(transposeDTO);
		
		return helper;
		
	}
	
	private void writeToFile(IDrillthroughQuery query,	DrillthroughResultDTO newResultDTO){
		dump(newResultDTO);
	}

	
	/**
	 * method to debug and ump the output of webservice into file system
	 * @param dto
	 */
	private void dump(DrillthroughResultDTO dto){
		String temp=System.getProperty("java.io.tmpdir");
		logger.info("User temp directory "+temp);
		File f =new File(temp +"TransposeDrillThrough_"+System.currentTimeMillis()+".csv");
		PrintWriter pw= null;
		try {
			pw= new PrintWriter(f);
			List<DrillthroughHeaderDTO> headers=dto.getHeaders();
			StringBuilder builder=new StringBuilder();
			for (DrillthroughHeaderDTO header:headers){
				builder.append(header.getName()+",");
			}
			pw.println(builder.toString());
			for (DrillthroughRowDTO dtoRow:dto.getRows()){
				
				String rowStr=Arrays.toString(dtoRow.getContent());
				rowStr=rowStr.replace("[", "");
				rowStr=rowStr.replace("]", "");
				pw.println(rowStr);
			}
		}catch(FileNotFoundException fe)
		{}
		finally{
			pw.close();
		}
	}
	
	
	public void setQueryService(IQueriesService queryService) {
		this.queryService = queryService;
	}


	private void init() 
	{
		loadTransposeProp();
		identifyColumnsToExclude();
	}


	private void loadTransposeProp() {
		if (transposeProperties.getProperty(DrillThroughConstants.MEASURE_COL) != null) 
		{
			measureName = transposeProperties.getProperty(DrillThroughConstants.MEASURE_COL);
			String alias = drillThroughAliases.getProperty(measureName);
			measureName =(alias!=null)?alias:measureName;
		}
		
		if (transposeProperties.getProperty(DrillThroughConstants.DEAL_COL) != null) {
			dealNumberCol = transposeProperties.getProperty(DrillThroughConstants.DEAL_COL);
			String alias = drillThroughAliases.getProperty(dealNumberCol);
			dealNumberCol =(alias!=null)?alias:dealNumberCol;
		}
		
		termDimensionName=transposeProperties.getProperty("termDimension");
		underlyingTermDimensionName=transposeProperties.getProperty("underlyingtermDimension");
	}


	private void identifyColumnsToExclude() {
		String value = transposeProperties.getProperty(DrillThroughConstants.EXCLUDE_COL);
		if(value!=null)
		{
			String[] cols = value.split(",");
			for(String col : cols)
			{
				String alias= drillThroughAliases.getProperty(col);
				alias = alias == null ? col : alias;
				excludedFields.add(alias);
			}
		}
	}


	private Set<String> identifyColsToAggregate(IActivePivot pivot) 
	{
		Set<String> aggregateFields = new HashSet<String>();
		List<IMeasureDescription> measures = pivot.getDescription().getMeasures();
		// remove all measures from key
		for (IMeasureDescription desc : measures) {
			if (desc.getPreProcessedAggregations() != null) {
				String alias = drillThroughAliases.getProperty(desc.getMeasureName());
				if (alias != null) 
				{
					aggregateFields.add(alias);					
				}
			}
		}
		return aggregateFields;
	}
	
	protected IActivePivot checkAndRetrievePivot(String pivotId)
	{
		Validate.notNull(manager, "The ActivePivot's manager instance is not set.");
        IActivePivot pivot = (IActivePivot)manager.getActivePivots().get(pivotId);
        Validate.notNull(pivot,String.format("Unable to find pivot %s ", pivotId));
        return pivot;
    }	
	
	@ManagedAttribute
	public void setDebug(boolean isDebug) {
		this.debug = isDebug;
	}
	
	public boolean isDebug() {
		return debug;
	}
	
	private boolean hasTermValue(ILocation location,IDimension dim) 
	{
		return location.getLevelDepth(dim.getOrdinal()-1) >=2;
	}
	
	private boolean hasTermUnderlyingValue(ILocation location,IDimension dim) 
	{
		return location.getLevelDepth(dim.getOrdinal()-1) >=2;
	}
	
	private ILocation createWildCardLocation(ILocation location,IDimension term,IDimension underlyingTerm) 
	{
		Object[][] locationCopy = location.arrayCopy();
		if(hasTermValue(location,term))
		{
			logger.info("Term Value:" + locationCopy[term.getOrdinal()-1][1]);
			locationCopy[term.getOrdinal()-1][1]=null;
		}
		if(hasTermUnderlyingValue(location,underlyingTerm))
		{
			logger.info("Underlying Term Value:" + locationCopy[underlyingTerm.getOrdinal()-1][1]);
			locationCopy[underlyingTerm.getOrdinal()-1][1]=null;
		}	
		return new Location(locationCopy);		
		
	}


	private List<String> transformHeader(List<DrillthroughHeaderDTO> headers)
	{
		List<String> values = new ArrayList<String>(headers.size());
		for(DrillthroughHeaderDTO colHeader:headers)
		{
			values.add(colHeader.getName());
		}
		return values;
	}
	
	private IDrillthroughQuery checkIfNeedNextTermLocation(ILocation location, 
			 IDrillthroughQuery query,IActivePivot pivot){
	
		SubCubeProperties subProps = (SubCubeProperties)query.getContextValues().get(0);
		if (subProps.getAllGrantedMembers().get("Scenario-Term")!=null) {
			Set<List<?>> sceneTerms = subProps.getAllGrantedMembers().get("Scenario-Term");
			if(sceneTerms.size()==1) {
				
				List<String> scnTerm = (List<String>)sceneTerms.iterator().next();
				IDimension termDim = ANZUtils.findDimensionByName(pivot, termDimensionName);
				IDimension underlyerTermDim = ANZUtils.findDimensionByName(pivot, underlyingTermDimensionName);
				List<String> termBuckets = ANZUtils.getSortedTermBucket(termDim);
				boolean nextTerm = false;
				for (String termBucket : termBuckets) {
					
					if(nextTerm){  
						List<String> tScnTerm = new ArrayList();
						tScnTerm.add("AllMember");
						tScnTerm.add(termBucket);
						sceneTerms.add(tScnTerm);
						subProps.getAllGrantedMembers().get("Scenario-Term").add(tScnTerm);
						//break;
					} 
					
					if(termBucket.equals(scnTerm.get(1))){
						nextTerm = true;
					} 
				}
				DrillthroughQuery updatedQuery = new DrillthroughQuery(Collections.singletonList(location), query.getFirstResult(), query.getMaxResults());
				updatedQuery.setContextValues( query.getContextValues());
				updatedQuery.setPivotId(query.getPivotId());
				query=updatedQuery;
			    return query;
			} else if ( sceneTerms.size() > 1) {
				
				List<String> scnTerm = (List<String>)sortSubCubeProp(subProps);
				
				IDimension termDim = ANZUtils.findDimensionByName(pivot, termDimensionName);
				IDimension underlyerTermDim = ANZUtils.findDimensionByName(pivot, underlyingTermDimensionName);
				List<String> termBuckets = ANZUtils.getSortedTermBucket(termDim);
				boolean nextTerm = false;
				for (String termBucket : termBuckets) {
					
					if(nextTerm){
						List<String> tScnTerm = new ArrayList();
						tScnTerm.add("AllMember");
						tScnTerm.add(termBucket);
						sceneTerms.add(tScnTerm);
						subProps.getAllGrantedMembers().get("Scenario-Term").add(tScnTerm);
						//break;
					}
					if(termBucket.equals(scnTerm.get( scnTerm.size() -1 ))){
						nextTerm = true;
					}
				}
				DrillthroughQuery updatedQuery = new DrillthroughQuery(Collections.singletonList(location), query.getFirstResult(), query.getMaxResults());
				updatedQuery.setContextValues( query.getContextValues());
				updatedQuery.setPivotId(query.getPivotId());
				query=updatedQuery;
			    return query;
			}
		}
		return null;
		
	}
	
	private List<String> sortSubCubeProp(SubCubeProperties subProps ) {
			List<String> ret = new ArrayList<String>();
		if(subProps.getAllGrantedMembers().get("Scenario-Term")!=null){
			for (Iterator iter = subProps.getAllGrantedMembers().get("Scenario-Term").iterator(); iter.hasNext();) {
				List<String> field = (List<String>) iter.next();
				ret.add(field.get(1));
			}
			
			TenorComparator c = new TenorComparator();
			Collections.sort(ret,c );
			return   ret;	
		} else {
			return null;
		}
	}
}
