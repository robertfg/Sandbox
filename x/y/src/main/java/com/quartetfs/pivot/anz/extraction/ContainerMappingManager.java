package com.quartetfs.pivot.anz.extraction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import au.com.bytecode.opencsv.CSVReader;

import com.quartetfs.pivot.anz.dto.ExportContainerMapping;
import com.quartetfs.pivot.anz.impl.MessagesANZ;

@ManagedResource
public class ContainerMappingManager
{
	
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private Map<String,ExportContainerMapping> mappings=new LinkedHashMap<String, ExportContainerMapping>();
	private Set<String> allContainerLevels = new LinkedHashSet<String>();
	private Set<String> allContainerMeasures = new LinkedHashSet<String>();
	private List<String> headers = new ArrayList<String>();
	private String rootPath;
	private String mappingFileName;
	public ContainerMappingManager(String rootPath,String mappingFileName) 
	{
		this.rootPath=rootPath;
		this.mappingFileName=mappingFileName;
	}

	public void init() 
	{
		try{
			resetState();
			CSVReader reader = createCSVReader();	
			//First Line is header of columns
			List<String[]> rows = reader.readAll();
			if (rows.size()>0){
				Map<Integer,ExportContainerMapping> containerMappings = createMappingMap(rows.get(0));			
				readContainerMappings(rows, containerMappings);				
				mappings=transformMap(containerMappings);
				
				buildHeaders();
			}
		}catch(IOException e){
			LOGGER.warning(String.format("Unable to load container mapping for AgggregateExtract, Aggregation level extract would not be available. Error Message %s",e.getMessage()
			));
		}
	}

	private void resetState()
	{
		allContainerLevels.clear();
		allContainerMeasures.clear();
		headers.clear();
	}
	
	

	private CSVReader createCSVReader()
	{
		String path = String.format("/%s/%s",rootPath,mappingFileName);
		LOGGER.info(String.format("Loading data from file %s", path));
		
		URL location =  this.getClass().getResource(path);
	    String configPath = location.getPath();
		InputStream ioStream;
		try {
			ioStream = new FileInputStream(configPath);
			CSVReader reader = new CSVReader( new BufferedReader( new InputStreamReader(ioStream)));
			return reader;
		} catch (FileNotFoundException e) {
					
			e.printStackTrace();
		} 
		
		return null;
		 
		
		
	}

	
	private Map<String,ExportContainerMapping> transformMap(Map<Integer, ExportContainerMapping> containerMappings)
	{
		
		Map<String,ExportContainerMapping> mappings = new LinkedHashMap<String,ExportContainerMapping>();
		for(Map.Entry<Integer, ExportContainerMapping> entry : containerMappings.entrySet())
		{   
	//		LOGGER.info( "-------------------------reloading --------------------------------" + entry.getValue().getContainer() + "-:-" + entry.getKey() );
			mappings.put(entry.getValue().getContainer(), entry.getValue());			
		}
		LOGGER.info(String.format("Mapping found for %s container", mappings.keySet()));
		return mappings;
	}

	private void readContainerMappings(List<String[]> rows ,Map<Integer, ExportContainerMapping> containerMappings) 
	{
		
	//	LOGGER.info(" reloading config.............................");
		Iterator<String[]> dataRows = rows.listIterator(1);					
		String measureName=null;		
		for(;dataRows.hasNext();)
		{
			String[] rowValue = dataRows.next();
			String containerOrMeasureName = rowValue[0].trim();// First col is container or measure name
	//		LOGGER.info("containerOrMeasureName:" + containerOrMeasureName);
			
			boolean isMeasure = isMeasureName(containerOrMeasureName);
			
			if(isMeasure) measureName = reriveMeasureName(containerOrMeasureName);
			
	//		LOGGER.info("measureName:" +  measureName);
			
			
			@SuppressWarnings("unused")
			boolean dummyvalue = isMeasure ? allContainerMeasures.add(measureName) : allContainerLevels.add(containerOrMeasureName);
			
			for(int cntr=1;cntr<rowValue.length;cntr++)
			{
				
				if("T".equalsIgnoreCase(rowValue[cntr]))
				{
					@SuppressWarnings("unused")
					boolean dummy=isMeasure ? 
									containerMappings.get(cntr).getMeasures().add(measureName) : 
									containerMappings.get(cntr).getLevels().add(containerOrMeasureName);
					
					
				}
			}	
		}
	}
	
	private boolean isMeasureName(String value)
	{
		return value.indexOf("@MEASURE") > -1 ? true:false;
	}
	
	private String reriveMeasureName(String value)
	{
		return value.split("@")[0];
	}
	
	
	
	
	private Map<Integer,ExportContainerMapping> createMappingMap(String[] headers)
	{
		Map<Integer,ExportContainerMapping> containerMappings = new LinkedHashMap<Integer,ExportContainerMapping>();
		for(int cnt=1;cnt<headers.length;cnt++)
		{
			containerMappings.put(cnt,  new ExportContainerMapping(headers[cnt].trim()));
		}
		return containerMappings;
	}
	
	
	public Map<String, ExportContainerMapping> getMappings() {
		return mappings;
	}
	
	@ManagedOperation(description="Reload container mappings")
	public void reloadMapping() throws IOException
	{
		init();
	}
	
	public Set<String> getAllContainerLevels() {
		return allContainerLevels;
	}
	
	public Set<String> getAllContainerMeasures() {
		return allContainerMeasures;
	}
	
	private void buildHeaders() 
	{
		
		//Fixed Headers;
		//headers.add("COB Date");
	//	headers.add("Container");
		headers.add("Base Currency");
	  	
		//headers.addAll(keyList);
		headers.addAll(allContainerLevels);
		
		//headers.add("VaR-Scenario Date@VaR-Scenario Date");
		
		headers.addAll(allContainerMeasures);
	}
	
	public List<String> getHeaders() {
		return headers;
	}
}
