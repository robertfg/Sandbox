package com.quartetfs.pivot.anz.extraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.webservices.impl.AManagerService;
import com.quartetfs.pivot.anz.dto.ExportContainerMapping;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.export.ExportPnlDataHelper;
import com.quartetfs.pivot.anz.service.export.ExtractAPQueryPool;
import com.quartetfs.pivot.anz.service.export.ExtractFileWriterPool;
import com.quartetfs.pivot.anz.service.export.ExtractNonVarTask;
import com.quartetfs.pivot.anz.service.export.ExtractObject.ExtractType;
import com.quartetfs.pivot.anz.service.export.ExtractVarTask;
import com.quartetfs.pivot.anz.service.export.GetPnlDataFromIndexerTest;
import com.quartetfs.pivot.anz.service.export.VectorizerPool;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.webservices.IExtractorService;
import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;

@ManagedResource		
@WebService(name="IExtractorService",
		targetNamespace="http://webservices.quartetfs.com/activepivot",
		endpointInterface = "com.quartetfs.pivot.anz.webservices.IExtractorService",
		serviceName = "ExtractorService")
public class ExtractorService extends AManagerService implements IExtractorService {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private ExtractUtils extractUtils; 
	private SignOffUtils signOffUtils;
    
    private ExtractFileWriterPool extractFileWriterPool;
    private VectorizerPool vectorizerPool;
    private ExtractAPQueryPool extractAPQueryPool;
    
   
    
     
    
	public ExtractorService( ExtractFileWriterPool extractFileWriterPool,	VectorizerPool vectorizerPool,ExtractAPQueryPool extractAPQueryPool,
			ExtractUtils extractUtils,SignOffUtils signOffUtils )	{
		
		LOGGER.info( " Starting ExtractorService ");
		this.extractUtils = extractUtils;
		extractUtils.loadConfig();
		this.extractFileWriterPool = extractFileWriterPool;
		this.vectorizerPool = vectorizerPool;
		this.extractAPQueryPool = extractAPQueryPool;
		this.signOffUtils = signOffUtils;
		
		
	} 
	
	@Override
	public void extractAtHierarchyLevel(ExtractParamsDTO extractParamsDTO) {
		ContainerMappingManager mappingManager = new ContainerMappingManager("properties", "ContainerMapping.csv");
		                        mappingManager.init();
		                        
		LOGGER.info("============================================================================================================");
	     extractUtils.setMappingManager(mappingManager);
	     for (Map.Entry<String,ExportContainerMapping > mapping :extractUtils.getMappingManager().getMappings().entrySet())
		 {
			LOGGER.info( "Container:" + mapping.getValue().getContainer() + ",Dimensions:" + mapping.getValue().getLevels().toString() + ",Measures:" + mapping.getValue().getMeasures().toString() );
		 }
		
		 LOGGER.info("============================================================================================================");
		
	}
	
	@Override
	public void extract(ExtractParamsDTO extractParamsDTO) {  
		extractUtils.validateRequest(extractParamsDTO);
		if(extractParamsDTO.getLocationPath()!=null){
			extractParamsDTO.getLocationPaths().add(extractParamsDTO.getLocationPath());
		}
	}
	
	@Override
	public void extract(ExtractParamsDTO extractParamsDTO, String[] containers) 
	{
		for (int i = 0; i < containers.length; i++) {
			extractParamsDTO.setContainer(containers[i]);
			extract(extractParamsDTO );
		}
	} 
	
	@Required
	public void setPivotName(String name) {
		extractUtils.setPivot( checkAndRetrievePivot(name) );
	}
	
	protected static final List<Class<?>> UPDATABLE_TYPES = Arrays.asList(new Class<?>[] {String.class,
			Double.class,
			Float.class,
			Integer.class,
			Long.class});
	
	@Override
	public void doSignOff(String batchId) {
		LOGGER.info("SignOff start");
	}
	
	@Override
	@Oneway
	@WebMethod
	public void doSignOff( String cobDate, String previousCobDate, String[] containerList) {
		 signOffUtils.doSignOff(cobDate, previousCobDate, containerList);
	}

	public ExtractUtils getExtractUtils() {
		return extractUtils;
	}

	public void setExtractUtils(ExtractUtils extractUtils) {
		this.extractUtils = extractUtils;
	}

	public ExtractFileWriterPool getExtractFileWriterPool() {
		return extractFileWriterPool;
	}

	public void setExtractFileWriterPool(ExtractFileWriterPool extractFileWriterPool) {
		this.extractFileWriterPool = extractFileWriterPool;
	}

	public VectorizerPool getVectorizerPool() {
		return vectorizerPool;
	}

	public void setVectorizerPool(VectorizerPool vectorizerPool) {
		this.vectorizerPool = vectorizerPool;
	}

	public ExtractAPQueryPool getExtractAPQueryPool() {
		return extractAPQueryPool;
	}

	public void setExtractAPQueryPool(ExtractAPQueryPool extractAPQueryPool) {
		this.extractAPQueryPool = extractAPQueryPool;
	}

	@Override
	@WebMethod(operationName = "reloadConfig")
	public void reloadConfig() {
		
		LOGGER.info("============================================================================================================");
		ContainerMappingManager mappingManager = new ContainerMappingManager("properties", "ContainerMapping.csv");
        mappingManager.init();
		extractUtils.setMappingManager(mappingManager);
	     for (Map.Entry<String,ExportContainerMapping > mapping :extractUtils.getMappingManager().getMappings().entrySet())
		 {
			LOGGER.info( "Container:" + mapping.getValue().getContainer() + ",Dimensions:" + mapping.getValue().getLevels().toString() + ",Measures:" + mapping.getValue().getMeasures().toString() );
		 }
		
		 LOGGER.info("============================================================================================================");
		    
	
	}

	@Override
	@WebMethod(operationName = "extractVAR")
	public void extractVAR(@WebParam(name = "extractParamsDTO") ExtractParamsDTO extractParamsDTO) {
		
		extractUtils.validateRequest(extractParamsDTO);
		
		if(extractParamsDTO.getLocationPath()!=null) {
			extractParamsDTO.getLocationPaths().add(extractParamsDTO.getLocationPath());
		}
		
		/* ExtractVarTask task = new ExtractVarTask( extractUtils,extractParamsDTO,
				 vectorizerPool.getVectorizerQueue(), extractFileWriterPool.getFileWriterQueue());
	*/	  
		 try{
		//   this.extractAPQueryPool.getQueryQueue().put(task);
		 }catch(Exception e){
			e.printStackTrace();
		 }	
		
		
	}

	@Override
	@WebMethod(operationName = "extractNonVAR")
	public void extractNonVAR(@WebParam(name = "extractParamsDTO") ExtractParamsDTO extractParamsDTO) {
		 
	}

	@Override
	@WebMethod(operationName = "triggerExtractFromFeedMonitoring")
	public void triggerExtractFromFeedMonitoring(@WebParam(name="currentCobDateInYYYYMMDD") int currentCobDate, 
			@WebParam(name="previousCobDateInYYYYMMDD") int previousCobDate,
			@WebParam(name="jobId") int jobId, @WebParam(name="containerName") String containerName ){
	
		
		  
		String locPath =  "AllMember\\ANZ Group";
		
		List<String> locFilter = new ArrayList<String>();
		             locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" +  ILocation.WILDCARD );
		
		if(containerName==null || containerName.equals("VAR_1D_AGG")){
			
			ExtractParamsDTO VAR_1D =   new ExtractParamsDTO( "VAR_1D", locPath, 
					String.valueOf(System.currentTimeMillis()+ System.nanoTime()) ,  
					String.valueOf(currentCobDate),
	                true,100, ExtractType.VAR_PNL  );
			        
			VAR_1D.setPsrName("VAR_1D");
			VAR_1D.setPreviousCobDate( String.valueOf(currentCobDate));
			VAR_1D.setLocationPaths(locFilter);											
			VAR_1D.setFromFM(true);
			VAR_1D.setJobId(String.valueOf(jobId));
			VAR_1D.setVarRefDateType("VAR");
			
		/*	ExtractVarTask taskVar1D = new ExtractVarTask( extractUtils,VAR_1D,
					 vectorizerPool.getVectorizerQueue(), extractFileWriterPool.getFileWriterQueue());
			 
			 try{
				 this.extractAPQueryPool.getQueryQueue().put(taskVar1D);
			 }catch(Exception e){
				e.printStackTrace();
			 }*/
		}
	
		if(containerName==null || containerName.equals("VAR_10D_AGG")){
			
			ExtractParamsDTO VAR_10D =   new ExtractParamsDTO( "VAR_10D", locPath, 
					String.valueOf(System.currentTimeMillis()+ System.nanoTime()) ,  
					String.valueOf(currentCobDate),
					true,100, ExtractType.VAR_PNL  );
			        
				 VAR_10D.setPsrName("VAR_10D");
				 VAR_10D.setPreviousCobDate( String.valueOf(currentCobDate));
				 VAR_10D.setLocationPaths(locFilter);
				 VAR_10D.setFromFM(true);
				 VAR_10D.setJobId(String.valueOf(jobId));
				 VAR_10D.setVarRefDateType("VAR");
				 VAR_10D.setVarRefDateType("VAR");
					
			
			/* ExtractVarTask taskVar10D = new ExtractVarTask( extractUtils,VAR_10D,
					 vectorizerPool.getVectorizerQueue(), extractFileWriterPool.getFileWriterQueue());
			 try{
				 this.extractAPQueryPool.getQueryQueue().put(taskVar10D);
			 }catch(Exception e){
				e.printStackTrace();
			 }*/
		}	 
		
		if(containerName==null || containerName.equals("VAR_STRESS")){
			
			 ExtractParamsDTO VAR_STRESS =   new ExtractParamsDTO( "VAR_STRESS", locPath, 
						String.valueOf(System.currentTimeMillis()+ System.nanoTime()) ,  
						String.valueOf(currentCobDate),
						true,100, ExtractType.VAR_STRESS_PNL );
				        
						 VAR_STRESS.setPsrName("VAR_STRESS");
						 VAR_STRESS.setPreviousCobDate( String.valueOf(currentCobDate));
						 VAR_STRESS.setLocationPaths(locFilter);
						 VAR_STRESS.setFromFM(true);
						 VAR_STRESS.setJobId(String.valueOf(jobId));
						 VAR_STRESS.setVarRefDateType("VAR_STRESS");	 
						
					/*	 ExtractVarTask taskVAR_STRESS = new ExtractVarTask( extractUtils,VAR_STRESS,
								 vectorizerPool.getVectorizerQueue(), extractFileWriterPool.getFileWriterQueue());
						 try{
							 this.extractAPQueryPool.getQueryQueue().put(taskVAR_STRESS);
						 }catch(Exception e){
							e.printStackTrace();
						 }*/
		 
		}
		
	}
	
	@Override
	@WebMethod(operationName="testCode")
	public void testCode(@WebParam(name="cobDate") int cobDate ){
		
		String locPath =  "AllMember\\ANZ Group";
		
		List<String> locFilter = new ArrayList<String>();
		String uId = "" + System.currentTimeMillis() ;
		String token = "c:\\temp\\" + "V1AL0#VAR AND P&L#VAR AND P&L#"+uId+"#"+cobDate+" #"+uId+".TMP";
		
		locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" +  ILocation.WILDCARD );
		
		
		ExtractParamsDTO extractParamsDTO =   new ExtractParamsDTO( "VAR_1D", locPath, 
												  String.valueOf(System.currentTimeMillis()+ System.nanoTime()) ,  
												  String.valueOf(cobDate),
												  false,100, ExtractType.VAR_PNL  );
		
		extractParamsDTO.setPsrName("VAR_1D");
		extractParamsDTO.setPreviousCobDate( String.valueOf(cobDate));
		extractParamsDTO.setLocationPaths(locFilter);											
		extractParamsDTO.setContainer("VaR and P&L");
		
	  
		
		GetPnlDataFromIndexerTest pnl = new GetPnlDataFromIndexerTest( extractUtils, 
				extractUtils.getContainerMapping(true, "VAR_1D",extractParamsDTO.getContainer()), super.manager );
		 
				pnl.test1DayVarPnlExtract(extractParamsDTO, token);
	
				
	}
	
	
}
