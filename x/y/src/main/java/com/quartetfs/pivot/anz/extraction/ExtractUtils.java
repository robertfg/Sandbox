package com.quartetfs.pivot.anz.extraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel.ClassificationType;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationSet;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.pivot.anz.dto.ExportContainerMapping;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.export.ExtractHeader;
import com.quartetfs.pivot.anz.service.impl.DateService;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.QueryHelper;
import com.quartetfs.pivot.anz.utils.VectorConfig;
import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;

/**
 * Utilities for Aggregation Extraction Service.
 * 
 */
public class ExtractUtils {
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	public ExtractUtils() {
		parser = (DateParser) Registry.getPlugin(IParser.class).valueOf("date[yyyyMMdd]");
	}

	private IActivePivot pivot;
	private String signOffSql;
	private String nonVarHeader;
	private DateParser parser;
	private String defaultDimension;
	private String extractionDirectory;

	private List<ExtractHeader> nonVarCsvOutputMapping;
	private ContainerMappingManager mappingManager;

	private Properties crossJoinProperties;
	private DateService dateService;
	
	private String xmlConfigLocation;

	private List<String> vectorizeContainer = new ArrayList<String>();
	private Map<String, String> containerNameToPsrName = new HashMap<String, String>();
	private Map<String, String> vectorContainerMeasureMapping = new HashMap<String, String>();
	private Map<String, VectorConfig> vectorConfigurations = new HashMap<String, VectorConfig>(); //
	private static final Map<String,String> varContainerMapping = new HashMap<String,String>();	
	static {
			varContainerMapping.put("B1AL0", "HYPO");
			varContainerMapping.put("VXAL0", "VAR_10D_AGG");
			varContainerMapping.put("V1AL0", "VAR_1D_AGG");
			
	}
	

	private List<String> transformsMeasures(List<String> measureList,
			String container) {
		List<String> measures = new ArrayList<String>();
		int index = 0;
		for (String measureName : measureList) {
			if (measureName.equalsIgnoreCase(ANZConstants.M_RESULT_MEASURE)) {
				measures.add(index,
						vectorContainerMeasureMapping.get(container + "-"
								+ ANZConstants.M_RESULT_MEASURE));
			} else if (measureName
					.equalsIgnoreCase(ANZConstants.M_RESULTV_MEASURE)) {
				measures.add(
						index,
						vectorContainerMeasureMapping.get(container + "-"
								+ ANZConstants.M_RESULTV_MEASURE));
			} else {
				measures.add(index, measureName);
			}
			index++;
		}
		return measures;
	}

	private List<String> transformsVarMeasures(List<String> measureList,
			String container) {
		List<String> measures = new ArrayList<String>();
		int index = 0;
		for (String measureName : measureList) {
			if (measureName.equalsIgnoreCase(ANZConstants.M_RESULT_MEASURE)) {
				measures.add(index, "HypoPL_scenario_AUD.SUM"); // this can be
																// put in in
																// containerMapping.csv
			} else if (measureName
					.equalsIgnoreCase(ANZConstants.M_RESULTV_MEASURE)) {
				measures.add(index, "HypoPL_scenario_AUD.SUM");
			} else {
				measures.add(index, measureName);
			}
			index++;
		}
		return measures;
	}

	public List<String> transformMeasure(String containerName,
			List<String> allMeasure) {
		if (isContainerVectorize(containerName)) {
			return transformsMeasures(allMeasure, containerName);
		} else if (containerName.equalsIgnoreCase("HYPO")
				|| containerName.equalsIgnoreCase(ANZConstants.VAR_CONTAINER)) {
			return transformsVarMeasures(allMeasure, containerName);

		}
		return allMeasure;
	}

	private boolean isContainerVectorize(String containerName) {
		return vectorizeContainer.contains(containerName);// containerName.equalsIgnoreCase(ANZConstants.FXO_CONTAINER);
	}

	public Object getValue(Object value) {
		if (value instanceof double[]) {
			double[] vector = (double[]) value;
			return doubleToString(vector);
		}
		return value;
	}

	private String doubleToString(double[] values) {

		StringBuilder sb = new StringBuilder();
		for (double o : values) {
			sb.append(o).append("|");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public synchronized String buildFilePath(ExtractParamsDTO extractParamsDTO,	String extractType, String fileExt, String batchID) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(extractionDirectory).append(System.getProperty("file.separator"));
		sb.append(extractParamsDTO.getPsrCode()).append(ANZConstants.HASH_SEPARATOR);
		sb.append(extractParamsDTO.getContainer() ).append(ANZConstants.HASH_SEPARATOR);
		sb.append(extractType).append(ANZConstants.HASH_SEPARATOR);
		sb.append(extractParamsDTO.getCobDate()).append(ANZConstants.HASH_SEPARATOR);
		sb.append(batchID );
		sb.append(ANZConstants.HASH_SEPARATOR).append(extractParamsDTO.getJobId());	
		sb.append(ANZConstants.HASH_SEPARATOR).append(extractParamsDTO.getPreviousCobDate());	
		sb.append(ANZConstants.HASH_SEPARATOR).append(extractParamsDTO.getExtractType());
		sb.append(ANZConstants.HASH_SEPARATOR);
		
		sb.append(fileExt);
		return sb.toString();
	}



	public IDimension findDimensionByName(final String name, IActivePivot pivot) {
		return (IDimension) CollectionUtils.find(pivot.getDimensions(),
				new Predicate() {
					@Override
					public boolean evaluate(Object object) {
						IDimension dim = (IDimension) object;
						return dim.getName().equals(name);
					}
				});
	}

	public List<String> getLevels(List<String> dimensions, IActivePivot pivot) {
		List<String> result = new ArrayList<String>();
		for (String dimension : dimensions) {
			IDimension dim = findDimensionByName(dimension, pivot);
			for (ILevel level : dim.getLevels()) {
				if (level.getClassificationType() == ClassificationType.ALL) {
					continue;
				}
				result.add(String.format("%s@%s", level, dimension));
			}
		}

		return result;
	}

	/**
	 * This utility method provides all levels (except AllMember level) in map
	 * in qualified format Level Name@Dimension Name and set location wildcard *
	 * as value.
	 * 
	 * @param dimensionName
	 * @param pivot
	 * @return
	 */

	public List<Map<String, Object>> getAllLevelPath(String dimensionName,
			IActivePivot pivot) {
		IDimension dimension = findDimensionByName(dimensionName, pivot);
		List<Map<String, Object>> paths = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < dimension.getLevels().size(); i++) {
			Map<String, Object> mapping = new HashMap<String, Object>();
			// // skip All member
			if (dimension.getLevels().get(i).getClassificationType() == ClassificationType.ALL) {
				continue;
			}
			for (int k = 1; k <= i; k++) {
				ILevel level = dimension.getLevels().get(k);
				String path = String.format("%s@%s", level.getName(),
						dimension.getName());
				mapping.put(path, ILocation.WILDCARD);
			}
			paths.add(mapping);
		}
		return paths;
	}

	
	public boolean buildNonVarCsvHeader() {
		StringBuilder sb = new StringBuilder();
		for (ExtractHeader extractHeader : this.nonVarCsvOutputMapping) {
			sb.append(extractHeader.getName());
			sb.append(ANZConstants.DATA_EXTRACT_SEPARATOR);
		}
		sb.deleteCharAt(sb.length() - 1);
		this.nonVarHeader = sb.toString();//
		return true;
	}

	public Map<String, String> getVectorContainerMeasureMapping() {
		return vectorContainerMeasureMapping;
	}

	public void setVectorContainerMeasureMapping(
			Map<String, String> vectorContainerMeasureMapping) {
		this.vectorContainerMeasureMapping = vectorContainerMeasureMapping;
	}

	public List<String> getVectorizeContainer() {
		return vectorizeContainer;
	}

	public void setVectorizeContainer(List<String> vectorizeContainer) {
		this.vectorizeContainer = vectorizeContainer;
	}

	public List<ExtractHeader> getNonVarCsvOutputMapping() {
		return nonVarCsvOutputMapping;
	}

	public void setNonVarCsvOutputMapping(
			List<ExtractHeader> nonVarCsvOutputMapping) {
		this.nonVarCsvOutputMapping = nonVarCsvOutputMapping;
	}

	public void setContainerNameToPsrName(
			Map<String, String> containerNameToPsrName) {
		this.containerNameToPsrName = containerNameToPsrName;
	}

	public Map<String, String> getContainerNameToPsrName() {
		return containerNameToPsrName;
	}

	public String getExtractionDirectory() {
		return extractionDirectory;
	}

	public void setExtractionDirectory(String extractionDirectory) {
		this.extractionDirectory = extractionDirectory;
	}

	public void setNonVarHeader(String nonVarHeader) {
		this.nonVarHeader = nonVarHeader;
	}

	public String getNonVarHeader() {
		return nonVarHeader;
	}

	public void setParser(DateParser parser) {
		this.parser = parser;
	}

	public DateParser getParser() {
		return parser;
	}

	public String getUniqueID(String filePath) {

		return filePath.substring(
				filePath.lastIndexOf(ANZConstants.HASH_SEPARATOR) + 1,
				filePath.length() - 4);
	}

	public void validateRequest(ExtractParamsDTO extractParamsDTO) {
		try{
		Validate.notNull(extractParamsDTO.getContainer(),"Container name can't be null");
		Validate.notNull(extractParamsDTO.getCobDate(),	"COB date can't be null");
		Validate.isTrue( mappingManager.getMappings().containsKey(extractParamsDTO.getContainer()), String.format("Container %s is not mapped for export",
						extractParamsDTO.getContainer()));
		}catch(Exception e){
		    
		  LOGGER.severe(e.getLocalizedMessage());
		}
	}

	public ContainerMappingManager getMappingManager() {
		return mappingManager;
	}

	public void setMappingManager(ContainerMappingManager mappingManager) {
		this.mappingManager = mappingManager;
	}

	public void applyLocationFilter(ExtractParamsDTO param,
			Map<String, Object> queryParameters) {

		if (StringUtils.isBlank(param.getLocationPath()))
			return;

		String[] locationParts = param.getLocationPath().split(	Pattern.quote(ILocation.LEVEL_SEPARATOR));

		String dimName = StringUtils.isBlank(param.getDimensionName()) ? defaultDimension: param.getDimensionName();

		IDimension dimension = this.findDimensionByName(dimName, pivot);

		Validate.notNull(dimension,	String.format("Invalid dimension name %s", dimName));
		boolean allMember = dimension.getLevels().get(0).getClassificationType().equals(ClassificationType.ALL);

		Validate.isTrue(dimension.getLevels().size() >= locationParts.length,String.format(	"Invalid location. Max level for dimension %s is %s",	dimension.getName(), dimension.getLevels().size()));
		Validate.isTrue(!allMember || locationParts[0].equals(ILevel.ALLMEMBER),"Invalid location. AllMember is missing");

		for (int cnt = allMember ? 1 : 0; cnt < locationParts.length; cnt++) {
			ILevel level = dimension.getLevels().get(cnt);
			queryParameters.put(String.format("%s@%s", level.getName(),	dimension.getName()), locationParts[cnt]
					.equals(ILocation.WILDCARD) ? ILocation.WILDCARD : locationParts[cnt]);
		}
		
		dimName = null;
		dimension = null;
		locationParts = null;
		
	}

	public Map<String, Object> generateQueryParameters(
			ExtractParamsDTO extractParamsDTO,
			ExportContainerMapping containerMapping) {
		
		//LOGGER.info( "generateQueryParameters:extractParamsDTO.getLocationPath()=" +   extractParamsDTO.getLocationPath());
		Map<String, Object> queryParameters = new LinkedHashMap<String, Object>();
		//IDate cobDate = Registry.create(IDate.class, parser.parse(extractParamsDTO.getCobDate()).getTime());
		queryParameters.put("COB Date@COB Date",extractParamsDTO.getIDate());
		queryParameters.put("Container@Container", extractParamsDTO.getContainer());
		queryParameters.put("Base Currency@Base Currency", ILocation.WILDCARD);
		queryParameters.put("Data Snapshot@Data Snapshot", ANZConstants.END_OF_DAY);
		
		
		for (String levelName : containerMapping.getLevels()) {
			queryParameters.put(levelName, ILocation.WILDCARD);
		}

		this.applyLocationFilter(extractParamsDTO, queryParameters); 
		//cobDate = null;
		return queryParameters;
		
		
	}

	public List<Map<String, Object>> generateMultipleQueryParameters(
			ExtractParamsDTO extractParamsDTO,
			ExportContainerMapping containerMapping) {
		
		List<Map<String, Object>> multiLocs = new ArrayList<Map<String,Object>>();
	
		for (String location: extractParamsDTO.getLocationPaths()) {
			 //LOGGER.info(location);
			 extractParamsDTO.setLocationPath(location);
			 multiLocs.add(generateQueryParameters(extractParamsDTO, containerMapping));
		}
		
		if(multiLocs.size()==0){
			 multiLocs.add(generateQueryParameters(extractParamsDTO, containerMapping));
		}
		return multiLocs;
	}
	

	public String getDefaultDimension() {
		return defaultDimension;
	}

	public void setDefaultDimension(String defaultDimension) {
		this.defaultDimension = defaultDimension;
	}

	public IActivePivot getPivot() {
		return pivot;
	}

	public void setPivot(IActivePivot pivot) {
		this.pivot = pivot;
	}

	public void setCrossJoinProperties(Properties crossJoinProperties) {
		this.crossJoinProperties = crossJoinProperties;
	}

	public Properties getCrossJoinProperties() {
		return crossJoinProperties;
	}

	public void loadConfig() {
		this.buildNonVarCsvHeader();
	}

	public String getXmlConfigLocation() {
		return xmlConfigLocation;
	}

	public void setXmlConfigLocation(String xmlConfigLocation) {
		this.xmlConfigLocation = xmlConfigLocation;
	}

	public void setSignOffSql(String signOffSql) {
		this.signOffSql = signOffSql;
	}

	public String getSignOffSql() {
		return signOffSql;
	}

	public Map<String, VectorConfig> getVectorConfigurations() {
		return vectorConfigurations;
	}

	public void setVectorConfigurations(
			Map<String, VectorConfig> vectorConfigurations) {
		this.vectorConfigurations = vectorConfigurations;
	}

	@SuppressWarnings("unchecked")
	public List<String> getCrossJoinProperty(String varType,
			String containerName, String propertyName) {
		String property = getCrossJoinProperties().getProperty(	varType + containerName + propertyName);
		validateForNull(property);
		
		return org.springframework.util.CollectionUtils.arrayToList(StringUtils.split(property, ","));
	}

	private void validateForNull(Object... args) {
		for (Object value : args) {
			Validate.notNull(value);
		}
	}

	public String buildVarHeader(List<String> attributes) {
		StringBuilder sb = new StringBuilder();
		for (String attribute : attributes) {
			sb.append(attribute).append(ANZConstants.DATA_EXTRACT_SEPARATOR);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public List<Map<String, Object>> generateVarQueryParameters(
			ExtractParamsDTO extractParamsDTO,
			ExportContainerMapping containerMapping, boolean isPNL) {

	    String locs[] = extractParamsDTO.getLocationPath().split("\\\\");
		String location = locs[0];
		IDate cobDate = Registry.create(IDate.class,getParser().parse(extractParamsDTO.getCobDate()).getTime());
		ExtractParamsDTO exDto = extractParamsDTO;
		List<Map<String, Object>> queryParam = new ArrayList<Map<String, Object>>();

		for (int i = 1; i < locs.length; i++) {  
			location += "\\" + locs[i];
		}
		
		exDto.setLocationPath(location);
		
		Map<String, Object> queryParameters = new LinkedHashMap<String, Object>();
		
		queryParameters.put("COB Date@COB Date",cobDate);
		queryParameters.put("Container@Container", extractParamsDTO.getContainer());
		queryParameters.put("Base Currency@Base Currency", ILocation.WILDCARD);
		queryParameters.put("Data Snapshot@Data Snapshot", ANZConstants.END_OF_DAY);
		
		if (isPNL) {
			if(extractParamsDTO.getDimensionFilter()!=null && extractParamsDTO.getDimensionFilter().indexOf(":")>0 ){
				if(extractParamsDTO.getDimensionFilter().split(":")[0].length()>0){
					queryParameters.put(extractParamsDTO.getDimensionFilter().split(":")[0],extractParamsDTO.getDimensionFilter().split(":")[1]);
				}
				applyLocationFilter(exDto, queryParameters);
		    	queryParam.add(queryParameters);

			}else{
				applyLocationFilter(exDto, queryParameters);
		    	queryParam.add(queryParameters);
				//LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>location:" + extractParamsDTO.getDimensionFilter());
			}
		}  else {
		
			applyLocationFilter(exDto, queryParameters);
			queryParam.add(queryParameters);
		}
    	locs = null;
    	location = null;
    	queryParameters = null;
    	cobDate = null;
    	
		return queryParam;
	}

	public List<Map<String, Object>> crossjoin(
			List<Map<String, Object>> dimension1,
			List<Map<String, Object>> dimension2,
			Map<String, Object> queryParameters) {

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> mapping1 : dimension1) {
			for (Map<String, Object> mapping2 : dimension2) {
				Map<String, Object> queryMap = new HashMap<String, Object>();
				queryMap.putAll(queryParameters);
				queryMap.putAll(mapping1);
				queryMap.putAll(mapping2);
				result.add(queryMap);
			}
		}
		return result;
	}
	
	 public boolean renameFile(String filePath, String find, String replace){
		    LOGGER.log(Level.INFO,"Starting renaming file:" + filePath);
			File old = new File(filePath);
			filePath = filePath.replace( find,replace );
			boolean success =  old.renameTo( new File(filePath) );
	         if(success){
	         	LOGGER.log(Level.INFO,"Success renaming file to:" + filePath);
	         }else {
	         	LOGGER.log(Level.SEVERE,"Unable to rename the file");
	         }
			return success;
	 }
	 
	public Set<ILocation> buildLocations(QueryHelper queryHelper,
			ExportContainerMapping containerMapping, boolean isPNL,ExtractParamsDTO extractParamsDTO) {

		Set<ILocation> locations = new LocationSet();

		if (extractParamsDTO.getLocationPath() == null) {
			Map<String, Object> queryParameters = this.generateQueryParameters(extractParamsDTO, containerMapping);
			// generate locations
			List<List<Map<String, Object>>> allDimensions = new ArrayList<List<Map<String, Object>>>();
			for (String dimension : containerMapping.getDimensions()) {
				allDimensions.add(this.getAllLevelPath(dimension,this.getPivot()));
			}

			Iterator<List<Map<String, Object>>> iterator = allDimensions.iterator();
			List<Map<String, Object>> first = iterator.next();

			while (iterator.hasNext()) {
				List<Map<String, Object>> other = iterator.next();
				first = this.crossjoin(first, other, queryParameters);
			}
			for (Map<String, Object> mapping : first) {
				mapping.putAll(queryParameters);
				ILocation location = queryHelper.computeLocation(mapping);
				locations.add(location);
			}
		
		} else {
			
				List<Map<String, Object>> queryParameters = this.generateVarQueryParameters(extractParamsDTO,	containerMapping, isPNL);
				for (Map<String, Object> queryParameter : queryParameters) {
					ILocation location = queryHelper.computeLocation(queryParameter);
					//LOGGER.log(Level.INFO, location.toString());
					locations.add(location);
				}
			
			
		}
		return locations;
	}
	public Set<ILocation> buildLocationsX(QueryHelper queryHelper,
			ExportContainerMapping containerMapping, boolean isPNL,ExtractParamsDTO extractParamsDTO) {

		Set<ILocation> locations = new LocationSet();

		if (extractParamsDTO.getLocationPath() == null) {
			Map<String, Object> queryParameters = this.generateQueryParameters(extractParamsDTO, containerMapping);
			// generate locations
			List<List<Map<String, Object>>> allDimensions = new ArrayList<List<Map<String, Object>>>();
			for (String dimension : containerMapping.getDimensions()) {
				allDimensions.add(this.getAllLevelPath(dimension,this.getPivot()));
			}

			Iterator<List<Map<String, Object>>> iterator = allDimensions.iterator();
			List<Map<String, Object>> first = iterator.next();

			while (iterator.hasNext()) {
				List<Map<String, Object>> other = iterator.next();
				first = this.crossjoin(first, other, queryParameters);
			}
			for (Map<String, Object> mapping : first) {
				mapping.putAll(queryParameters);
				ILocation location = queryHelper.computeLocation(mapping);
				locations.add(location);
			}
		} else {
			
			List<Map<String, Object>> queryParameters = this.generateVarQueryParameters(extractParamsDTO,	containerMapping, isPNL);
			for (Map<String, Object> queryParameter : queryParameters) {
				ILocation location = queryHelper.computeLocation(queryParameter);
				locations.add(location);
			}
			
		}
		return locations;
	}
	public Set<ILocation> reBuildLocations(
			ExportContainerMapping containerMapping, Set<ILocation> locations,QueryHelper queryHelper) {

		Set<ILocation> rebuildLocations = new LocationSet();
        for (ILocation iLocation : locations) {
        	Object[][] locArray = iLocation.arrayCopy();
        	
        	rebuildLocations.add(new Location(locArray));
		}
        
    
		return rebuildLocations;
	}

	public ExecutorService getExecutors(int threadCount, final String threadName){
		  return Executors.newFixedThreadPool(threadCount,new ThreadFactory() {
			AtomicInteger threadCtr = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				 Thread t = new Thread(r, threadName + ":" + threadCtr.incrementAndGet());
				 return t;
			}
		});
	  }
	
	
	
	public ExportContainerMapping getContainerMapping(boolean isPNL, String varType, String containerName){
		
		 String prefix = isPNL? "pnl." : "var.";
			
		 	List<String> levelsList    = getCrossJoinProperty( prefix, varType , ".levels");
			List<String> measuresList  = getCrossJoinProperty( prefix, varType , ".measures");
			List<String> dimensionList = getCrossJoinProperty( prefix, varType, ".dimensions");
		
			return new ExportContainerMapping(containerName,levelsList,measuresList,dimensionList);
		
		
	}
	
	public StringBuilder buildInSqlCondition(String[] strArr){
		StringBuilder str = new StringBuilder(200);
	  
		if(strArr.length>1){
			str.append("'");
				str.append(strArr[0]);
				str.append("',");
			
			for (int i = 1; i < strArr.length; i++) {
				str.append("'");
				str.append(strArr[i]);
				str.append("'");
			}
		}else{
			str.append("'");
			str.append(strArr[0]);
			str.append("'");
		
		}
		return str;
		
		
	}
	public void initialize(){
		loadConfig();
	}

	public DateService getDateService() {
		return dateService;
	}

	public void setDateService(DateService dateService) {
		this.dateService = dateService;
	}
	
	 public String getRefDate(String cobDate,String refDateType){
		  
		  String[] refDates =    dateService.retrieveDates(Registry.create(IDate.class, parser.parse( cobDate ).getTime()), refDateType);
		  StringBuilder refDate = new StringBuilder();
		  
		  for (int i = refDates.length-1;  i >= 0 ; i--) {
     	    if(refDates[i].length()==7){
     	    	refDates[i] = "0" + refDates[i];
     	    } 
			  refDate.append( "20" + refDates[i].substring(6) +  refDates[i].substring(3,5) + refDates[i].substring(0,2)   ).append("|");
		  }
		  return refDate.toString();
	  }
	 
	  public void createRefDate(String fileDirectory, String batchId, String refDates){
			
			OutputStream outputStream;
			try {
				String fileName = fileDirectory + "//" + batchId + "_VarRefDates.ref";
				LOGGER.info( "Creating refDates:" + fileName);
				   
				outputStream = new FileOutputStream(fileName);
			    outputStream.write(refDates.getBytes());
				outputStream.flush();				
				outputStream.close();	
				
				LOGGER.info( "Done creating refDates:" + fileName);
		
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
					
		} 
	
	   public String getDbContainer(ExtractParamsDTO extractParamDto){
	    	
	    	LOGGER.info( "dbContainer:" + extractParamDto.getContainer() );
	    	
	    	if( extractParamDto.getContainer().equals(ANZConstants.VAR_CONTAINER) ){
	    		return varContainerMapping.get( extractParamDto.getPsrCode()  );
	    		
	    	}else if(extractParamDto.getContainer().equals("VAR_STRESS")){
	    		return "VAR_STRESS_AGG";
	   
	    	}else if( extractParamDto.getContainer().equals(ANZConstants.VAR_SIX_YEAR_CONTAINER) ){
	    		return "VAR_1540_AGG";
	    		
	    	}else if( extractParamDto.getContainer().equals("VAR_1D") ){
	    		return "VAR_1D_AGG"; 
	    	
	    	} else if( extractParamDto.getContainer().equals("VAR_10D") ){
	    		return "VAR_10D_AGG";
	    	
	    	}	 
	    	return extractParamDto.getContainer();
	    	
	    }
}
