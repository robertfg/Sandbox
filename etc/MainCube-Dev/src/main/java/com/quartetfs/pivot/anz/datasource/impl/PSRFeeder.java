package com.quartetfs.pivot.anz.datasource.impl;

import static com.quartetfs.pivot.anz.utils.ANZUtils.extractPSRName;
import static com.quartetfs.pivot.anz.utils.ANZUtils.updateFileName;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

import com.quartetfs.fwk.messaging.ICSVSource;
import com.quartetfs.fwk.messaging.ICSVSourceListener;
import com.quartetfs.fwk.messaging.IFileInfo;
import com.quartetfs.fwk.messaging.IParsingInfo;
import com.quartetfs.fwk.messaging.ISource;
import com.quartetfs.fwk.messaging.ITransaction;
import com.quartetfs.pivot.anz.datasource.factory.DataSourceServiceFactory;
import com.quartetfs.pivot.anz.datasource.factory.DataSourceServiceFactory.BeanName;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.model.IVRParsingEntry;
import com.quartetfs.pivot.anz.model.impl.Deal;
import com.quartetfs.pivot.anz.service.IValueHolderService;
import com.quartetfs.pivot.anz.service.impl.PSRDetail;
import com.quartetfs.pivot.anz.service.impl.PSRDetail.PublisherType;
import com.quartetfs.pivot.anz.service.impl.PSRService;
import com.quartetfs.pivot.anz.service.impl.ValueHolderService.ValueType;
import com.quartetfs.pivot.anz.service.impl.VectorLabelService;
import com.quartetfs.pivot.anz.source.impl.PSRData;
import com.quartetfs.pivot.anz.utils.CubeEventKeeper;

public class PSRFeeder implements ICSVSourceListener{

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private ConcurrentMap<String, PSRData> psrDataMap = new ConcurrentHashMap<String, PSRData>();
	private Pattern psrPattern;	
	private Map<String, String> vectorPSRs;
	private PSRPublisherService publisherService;
	private DataSourceServiceFactory factory;
	private PSRService psrService; 
	private String statusFilePath;
	
	private PSRReducerService reducerService;
	private CubeEventKeeper eventKeeper; 
	private PSRDetail psrDetails;
	private String statusDirPath;
	
	public PSRFeeder(Properties prop,Map<String, String> vectorPSRs,DataSourceServiceFactory factory)
	{
		super();
		init(prop, vectorPSRs, factory);
	}


	private void init(Properties prop, Map<String, String> vectorPSRs,DataSourceServiceFactory factory) 
	{
		this.factory=factory;
		this.psrPattern = Pattern.compile(prop.getProperty("psr.pattern"));
		this.vectorPSRs = vectorPSRs;		
		this.psrService = factory.getPSRService(BeanName.psrService.name());
		this.reducerService = factory.getReducerService(BeanName.PSRReducerService.name());
		this.eventKeeper = factory.getCubeEventKeeper(BeanName.cubeEventKeeper.name());
		this.publisherService = factory.getPublisherService(BeanName.PSRPublisherService.name());
		
		Properties triggerProp=  factory.getFileTriggerProperties(BeanName.psrFileTriggerProperties.name());
		this.statusFilePath = new File(triggerProp.getProperty("directoryToWatch")).getParent();
		this.psrDetails = factory.getPSRDetails(BeanName.psrDetail.name());
		this.statusDirPath = triggerProp.getProperty("statusDirectory");
	}
	

	@Override
	public void receive(String subject, Object content, Properties properties,ITransaction trans) 
	{
       
		File file =new File(subject);
		String psrName = extractPSRName(psrPattern, file.getName());		
		
		
		@SuppressWarnings("unchecked")
		List<IVRParsingEntry> vrEntries = (List<IVRParsingEntry>) content;		
		if(CollectionUtils.isEmpty(vrEntries)) return;		
				
		IVRParsingEntry entry=vrEntries.get(0);
		String containerName = entry.getContainerName();
		Deal deal = entry.getDeal(); 		
		LOGGER.info("deal.getPsrName=" + deal.getPsrName() );
		loadReferenceData(file, psrName, containerName, deal);		
		PSRData psrData = createOrGetPSRData(file, psrName, containerName);
		
		reducerService.submit(psrData, vrEntries);			
	}
	

	private void loadReferenceData(File file, String psrName,String containerName, Deal deal) 
	{
		PublisherType publisherType  = psrDetails.identifyPublisherType(psrName);
		
		if (factory.getPSRDetails(BeanName.psrDetail.name()).retrieveVarPSRs().contains(psrName))
		{
			String varType = psrDetails.getVarPsrToDateNameMapping().get(psrName);
					
			if (!factory.getDateService(BeanName.dateService.name()).isVaRDatesLoadedFor(deal.getDate(),varType  )){
				IllegalStateException ex=new IllegalStateException(String.format(" VaR Dates missing for date [%s], cannot load VaR UVR [%s]",deal.getDate(),file.getAbsolutePath()));
				psrService.errorWithFileLoad(file.getName(), ex);
				updateFileName(new File(statusFilePath),file,true, this.statusDirPath);
				throw ex;
			}
		}
		
		String columnName=getVectorColumn(psrName);		
		if(columnName==null) return;
		
		VectorLabelService vectorLabelService = factory.getVectorLabelService(BeanName.vectorLabelService.name());
		
		if (publisherType.equals(PublisherType.Vector)  && !vectorLabelService.isLabelLoaded(deal.getDate(), containerName)) {
			String labelCSV=(String)deal.getAttributes().get(columnName);
			String[] labelsArray=StringUtils.delimitedListToStringArray(labelCSV, "|");
			vectorLabelService.put(deal.getDate(), containerName, Arrays.asList(labelsArray));			
			LOGGER.info(String.format("Vector label for [%s %s %s] is loaded from %s",containerName,deal.getDate(),labelCSV,file.getAbsolutePath()));
		}
	}

	private PSRData createOrGetPSRData(File file, String psrName, String containerName) 
	{
		IValueHolderService valueHolderService = factory.getValueHolderService(BeanName.valueHolderService.name());
		ValueType valueType = valueHolderService.getValueHolder(psrName);
		PSRData psrData = new PSRData(psrName, containerName, valueType, file.getAbsolutePath(), psrService);
		PSRData existingPsrData = psrDataMap.putIfAbsent(file.getAbsolutePath(), psrData);
		if(existingPsrData!=null)
		{
			psrData=existingPsrData;
		}
		return psrData;
	}
	
	@Override
	public void sourceStarted(ISource source) {}

	@Override
	public void sourceStopped(ISource source) {}

	@Override
	public void sourcePaused(ISource source) {}

	@Override
	public void sourceResumed(ISource source) {}

	@Override
	public void fileParsingStarted(ICSVSource source, IFileInfo fileInfo) 
	{	

		eventKeeper.fileParsingStarted(fileInfo.getFileName());
		LOGGER.log(Level.INFO, MessagesANZ.START_PARSING_FILE , fileInfo.getFileName());
	}

	@Override
	public void fileParsingCompleted(ICSVSource source, IFileInfo fileInfo,IParsingInfo parsingInfo) {
		
		try
		{

			eventKeeper.fileParsingCompleted(fileInfo.getFileName());
			LOGGER.log(Level.INFO, MessagesANZ.END_PARSING_FILE , fileInfo.getFileName());
			
			String psrName = extractPSRName(psrPattern, fileInfo.getFile().getName());			
			reducerService.waitForReduce(fileInfo.getFileAbsolutePath());
			PSRData data = psrDataMap.remove(fileInfo.getFileAbsolutePath());
			psrService.offer(psrName, data.getContainerName());
			
			LOGGER.info(String.format("File %s submitted for publishing.", fileInfo.getFileAbsolutePath()));
			publisherService.enqueue(data);
			
		}
		catch(Throwable e)
		{
			psrService.errorWithFileLoad(fileInfo.getFileName(), e);
			updateFileName(new File(statusFilePath),fileInfo.getFile(),true, this.statusDirPath);
			LOGGER.log(Level.SEVERE, String.format("Error while processing file %s", fileInfo.getFileAbsolutePath()),e);
		}
		finally
		{
			psrDataMap.remove(fileInfo.getFileAbsolutePath()); // Remove it if it is left due to error in reduce.
		}
				
	}
	
	
	
	private String getVectorColumn(String containerName){
		return vectorPSRs.get(containerName);
	}

}
