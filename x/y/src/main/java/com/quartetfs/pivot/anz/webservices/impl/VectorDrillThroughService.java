package com.quartetfs.pivot.anz.webservices.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.IActivePivotSchema;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.IProjection;
import com.quartetfs.biz.pivot.context.IContextSnapshot;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.impl.LocationUtil.ConvertLocationsResult;
import com.quartetfs.biz.pivot.query.IDrillthroughQuery;
import com.quartetfs.biz.pivot.webservices.impl.ServicesUtil;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.IProperty;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.postprocessing.impl.AnalysisDimensionHelper;
import com.quartetfs.pivot.anz.service.impl.VectorLabelService;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.ANZUtils;
import com.quartetfs.pivot.anz.utils.ListProperties;
import com.quartetfs.pivot.anz.webservices.IVectorDrillThrough;
import com.quartetfs.tech.indexer.IField;

@WebService(name="IVectorDrillThru",
		targetNamespace="http://webservices.quartetfs.com/activepivot",
		endpointInterface = "com.quartetfs.pivot.anz.webservices.IVectorDrillThrough",
		serviceName = "VectorDrillThroughService")
public class VectorDrillThroughService extends VaRQueryService implements IVectorDrillThrough {
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private Map<String, String>vectorContainers=new HashMap<String, String>();
	private VectorLabelService vectorLabelService; 
	private String dateDimension;
	private volatile int[] timeDimensionIdx;
	private DateParser parser;
	
	
	@Override
	public VectorDrillthroughDTO vectorDrillthrough(VectorQueryDTO vectorQueryDTO) {
		VectorDrillthroughDTO result = new VectorDrillthroughDTO();
		IActivePivot pivot = null;
		IContextSnapshot oldContext = null;
	
		try {
			// retrieve and check some params
			if (vectorQueryDTO == null) {
				LOGGER.log(Level.SEVERE, MessagesANZ.NULL_DTO, "VectorQueryDTO");
				return result;
			}
			IDrillthroughQuery dtQuery = vectorQueryDTO.getDtQuery();
			if (dtQuery == null) {
				LOGGER.log(Level.SEVERE, MessagesANZ.NULL_DT_QUERY);
				return result;
			}
			
			// check the container
			String container = vectorQueryDTO.getContainerName();
			
			if (container == null || !vectorContainers.keySet().contains(container)) {
				LOGGER.log(Level.SEVERE, MessagesANZ.WRONG_VECTOR_CONTAINER,vectorContainers.keySet().toString());
				return result;
			}


			// check the pivot
			String pivotId = dtQuery.getPivotId();
			pivot = checkAndRetrievePivot(pivotId);
			
			Map<String, Integer> measureToIndex=new HashMap<String, Integer>();
			measureToIndex=getMapping(container);

			
		

			LOGGER.log(Level.INFO,"=========================================================================================");
			LOGGER.log(Level.INFO,"Container Name:" + ANZUtils.getDrillThroughContainer(dtQuery) + ",drillUtil:" + drillUtil);
			
			
			ListProperties headersFromDb = drillUtil.getDrillThroughHeaders(ANZUtils.getDrillThroughContainer(dtQuery));
			final Properties headerProperties = headersFromDb==null?originalDrillThroughHeaders:headersFromDb; 
			attributesHeader = new LinkedHashSet<String>();
			 
			for(Entry<Object, Object> e : headerProperties.entrySet()) {
				 attributesHeader.add(  (String)e.getKey() );
		     }
			
			if (headersToRemove != null || !headersToRemove.isEmpty()){
				attributesHeader.removeAll(headersToRemove);
			}
			
			

			// get the location
			if (dtQuery.getLocations() == null|| dtQuery.getLocations().isEmpty()|| dtQuery.getLocations().size() > 1) {
				LOGGER.log(Level.SEVERE, MessagesANZ.LOCATION_ISSUE);
				return result;
			}
			String location = dtQuery.getLocations().iterator().next().toString();// get the first location, we're supposed to receive only one
			ILocation locationToQuery = stringToLocation(location,container);
			if (locationToQuery.isRange()) {
				LOGGER.log(Level.SEVERE, MessagesANZ.RANGE_LOCATION_FORBIDDEN);
				return result;
			}
			
			IDate locationDate=getDate(locationToQuery,pivot);
			List<String>labels=vectorLabelService.get(locationDate, container);
			//convert the location: date member will be converted to IDate
			ConvertLocationsResult conversionResult = LocationUtil.convertLocations(pivot.getDimensions(), Collections.singletonList(locationToQuery));
			Set<ILocation> locations = conversionResult.getConvertedLocations();

			//Execute the query contextually
			if (!isContextValid(dtQuery)) return result;
			oldContext = ServicesUtil.applyContextValues(pivot, dtQuery.getContextValues(), true);

			List<IProjection> results = getProjections(pivot, dtQuery,locations);
			if (results ==null || results.isEmpty()){
				return result;
			}
			
			LOGGER.log(Level.INFO, MessagesANZ.RESULTS_SIZE,new Object[]{"vectorDrillThrough", results.size()});
			
			List<VectorDealValueDTO> deals  = new ArrayList<VectorDealValueDTO>(results.size()); 

			//build and populate the DTO collection
			for(IProjection projection : results) {
				Long dealNumber =  (Long) projection.getContent()[measureToIndex.get(ANZConstants.DEAL_NUM)];
				double[] mresult = new double[labels.size()];
				double[] mresultV =new double[labels.size()];
				double[] tResult = null;
				Integer mresultIdx=measureToIndex.get(ANZConstants.MRESULT);
				Integer mresultVIdx=measureToIndex.get(ANZConstants.MRESULTV);   
				
				if (mresultIdx!=null){
					tResult =    projection.getContent()[mresultIdx] != null ?  (double[]) projection.getContent()[mresultIdx] : null;
					 System.arraycopy(tResult, 0, mresult, 0, labels.size());
				}
				
				if (mresultVIdx!=null){
					tResult = projection.getContent()[mresultVIdx] != null ? (double[]) projection.getContent()[mresultVIdx] : null;
					 System.arraycopy(tResult, 0, mresultV, 0, labels.size());
				}
				
				if (dealNumber != null ){
					deals.add(new VectorDealValueDTO(dealNumber.longValue(), retrieveAttributes(projection, outputFields),mresult, mresultV));
					
				}
			} 
			
			result.setAttributesHeader( aliasHeaders(attributesHeader,headerProperties));
			result.setDeals(deals);
			result.setVectorLabels(labels);
		
			//trace
			LOGGER.log(Level.INFO, MessagesANZ.RESULTS_TO_SEND,new Object[]{"vectorDrillthroughResults", result.getDeals().size()});
			
		} catch (Exception e) {
			String msg = ANZUtils.formatMessage(MessagesANZ.WS_ERR,"vectorDrillthrough");
			LOGGER.log(Level.SEVERE, msg, e);
		} finally {
			if (oldContext != null) {
				ServicesUtil.replaceContextValues(pivot, oldContext);
			}
		}
		return result;
}
	private Set<String> aliasHeaders(Set<String> attributeHeader,Properties properties){
		Set<String> aliasHeaders= new LinkedHashSet<String>();
		
		for (String header : attributeHeader) {
			aliasHeaders.add( (String) properties.get(header) );
		}
		return aliasHeaders;
	}

	private Properties getMeasureNames(String container){
		Properties result=new Properties();
		String measureStr=vectorContainers.get(container);
		
		if (measureStr!=null && measureStr.length()>0){
			String splited[] =measureStr.split(ANZConstants.OTHER_DISCRIMINATORS_SEPARATOR);
			result=StringUtils.splitArrayElementsIntoProperties(splited, ANZConstants.EQUALS_OP);
		}
		return result;
	}
	
	
	private Integer getIndexInSchema(String fieldName){
		IField field=outputFields.get(fieldName);
		if (field!=null){
			IProperty prop=field.getProperty();
			if (prop!=null){
				return Integer.valueOf(prop.getExpression());
			}
		}
		return null;
	}
	
	/**
	 * For a container return  index of M_RESULT and M_RESULTV 
	 * @param containerName
	 * @return
	 * @throws Exception
	 */
	private Map<String, Integer> getMapping(String containerName)throws Exception{
		IActivePivotSchema schema = checkAndRetrieveSchema(ANZConstants.SCHEMA_NAME);
		Map<String, Integer> mapping=new HashMap<String, Integer>();
		outputFields = schema.getClassifier().getOutputFields();
		Properties props=getMeasureNames(containerName);
		for (Object key:props.keySet()){
			mapping.put((String)key,getIndexInSchema(props.getProperty((String)key)));
		}
		mapping.put(ANZConstants.DEAL_NUM,getIndexInSchema(ANZConstants.DEAL_NUM_FIELD));
		return mapping;
	} 
	
	@Required
	public void setVectorContainers(Map<String, String> vectorContainers) {
		this.vectorContainers = vectorContainers;
	}
	
	@Required
	public void setVectorLabelService(VectorLabelService vectorLabelService) {
		this.vectorLabelService = vectorLabelService;
	}
	@Required
	public void setDateDimension(String dateDimensionStr) {
		this.dateDimension=dateDimensionStr;
	}

	private IDate getDate(ILocation location,IActivePivot pivot){
		Object[][]locationArray=location.arrayCopy();
		Object date[] = locationArray[getTimeDimensionIdx(pivot)[0]];
		String dateStr=(String)date[0];
		IDate dateResult=Registry.create(IDate.class, parser.parse(dateStr).getTime());
		return dateResult;
	}
	
	private int[] getTimeDimensionIdx(IActivePivot pivot){
		if (timeDimensionIdx!=null){
			return timeDimensionIdx;
		}
		
		timeDimensionIdx=new AnalysisDimensionHelper().getDimensionOrdinal(dateDimension, pivot);
		return timeDimensionIdx;
	}
	
	public void setDateFormat(String dateFormat) {
		parser =(DateParser) Registry.getPlugin(IParser.class).valueOf(dateFormat);
	}
	
	
}