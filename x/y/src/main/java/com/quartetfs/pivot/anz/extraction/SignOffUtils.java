package com.quartetfs.pivot.anz.extraction;

import java.io.File;
import java.io.FileFilter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.export.ExportPnlDataHelper;
import com.quartetfs.pivot.anz.service.export.ExtractAPQueryPool;
import com.quartetfs.pivot.anz.service.export.ExtractFileWriterPool;
import com.quartetfs.pivot.anz.service.export.ExtractNonVarTask;
import com.quartetfs.pivot.anz.service.export.ExtractObject.ExtractType;
import com.quartetfs.pivot.anz.service.export.ExtractVarTask;
import com.quartetfs.pivot.anz.service.export.VectorizerPool;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.DBUtils;
import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;



public class SignOffUtils implements Cloneable {
	
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private int nodeGroupingCount;
	private DBUtils      dbUtils;
	private ExtractUtils extractUtils;
	private ExportPnlDataHelper exportPnlDataHelper;
	private int retryTime;
	private int retryCount;
	private boolean debug;
	
	private static final Map<String,String> varContainerMapping = new HashMap<String,String>();	
	static {
			varContainerMapping.put("B1AL0", "HYPO");
			varContainerMapping.put("VXAL0", "VAR_10D_AGG");
			varContainerMapping.put("V1AL0", "VAR_1D_AGG");
			
	}
	

	public SignOffUtils(
			DBUtils dbUtils, ExtractFileWriterPool extractFileWriterPool,
			VectorizerPool vectorizerPool, ExtractAPQueryPool extractAPQueryPool,
			ExtractUtils extractUtils,int nodeGroupingCount, ExportPnlDataHelper exportPnlDataHelper, 
			int retryTime,int retryCount,boolean debug) {
		
		
		this.dbUtils = dbUtils;
		this.extractFileWriterPool = extractFileWriterPool;
		this.vectorizerPool = vectorizerPool;
		this.extractAPQueryPool = extractAPQueryPool;
		this.extractUtils = extractUtils;
		this.nodeGroupingCount = nodeGroupingCount;
		this.exportPnlDataHelper = exportPnlDataHelper;
		this.retryTime = retryTime;
		this.retryCount = retryCount;
		this.debug = debug;
	}

	private ExtractFileWriterPool extractFileWriterPool;
    private VectorizerPool vectorizerPool;
    private ExtractAPQueryPool extractAPQueryPool;
   
   public void doSignOff( String cobDate, String previousCobDate, String[] containerList) {
		        
				Validate.notNull(cobDate,"Cob Date name can't be null");
				LOGGER.info("SignOff by container start:" );
			try{	
			  if(containerList==null) {	
				String sql =  " select distinct(containerName) from DW.vw_SignOffAndExclude " +
						      " where isExcluded='0' and COBDate=" + cobDate + 
						      " AND portfolioID is not null";
				
			 	  List<Map<String,Object>> listOfContainers = dbUtils.executeSQL(sql);
			 	  LOGGER.info("Number of container to be sign off:" + listOfContainers.size() );
			 	  containerList = new String[listOfContainers.size()];
			 	  int ctr = 0; 
			 	  for (Map<String, Object> record : listOfContainers) {
			 		 containerList[ctr] = (String) record.get("containerName");
			 		 ctr++;
				  }  
			 	  
			 	 listOfContainers = null;
			 	 sql = null;
			  } else {
				  LOGGER.info("containerList is NOT null");
			  }
	 		  LOGGER.info("Number of containerList to be sign off:" + containerList.length);
				for (int i = 0; i < containerList.length; i++) { // container list
					 String container = containerList[i];
					 List<Map<String,Object>> nodes = getNodes(cobDate,container);
					 if(nodes!=null && nodes.size()  > 0 ) {
						 processNode( container, cobDate,  previousCobDate,  nodes);
					 }else {
						 LOGGER.info("No Data location from the Database: No status to be updated in the table:" + container);
					 }
					 nodes = null;
					 container = null;
				}
				containerList = null;
				
			}catch(Exception e){
				e.printStackTrace();
			}	finally{
				
			
				
			}
				
				
	}
   
   private void processNode(String container,String cobDate, String previousCobDate, List<Map<String,Object>> nodes){
	   LOGGER.log(Level.INFO, "processNode - containerName:" + container);
	   
	   StringBuilder nodeIds = new StringBuilder(100);
		 int nodeGroupingCtr =-1;
		 int nodeGrouping = nodeGroupingCount; // Number of node to be consolidated in cube request
		 nodeGrouping = 50000;   
		 
		 int nodeSize = nodes.size();
		 int nodeTotalPartition = this.nodePartionCount(nodeSize, nodeGrouping);
		 
		 
		 int nodesCtr = 0;
		 Map<String,Long> batchIDs = new HashMap<String,Long>();
		 
		 for (Map<String, Object> node : nodes) { //node list
			 nodeGroupingCtr++;
			 
			 nodeIds.append( String.valueOf(node.get("NodeID")) ).append(",")  ;
			 
			 if(nodeGroupingCtr==nodeGrouping && nodesCtr == nodeSize ){
				 doNodeSignOff(container,cobDate,previousCobDate,nodeIds, true, batchIDs,nodeTotalPartition); 
				 nodeGroupingCtr = -1;
				 nodeIds = new StringBuilder(100);
			 } else if(nodeGroupingCtr ==nodeGrouping && nodesCtr != nodeSize ){
				 doNodeSignOff(container,cobDate,previousCobDate,nodeIds,false, batchIDs,nodeTotalPartition); 
				 nodeGroupingCtr = -1;
				 nodeIds = new StringBuilder(100);
			 }
			 nodesCtr++;
		 }
	
		 if(nodeGroupingCtr>=0){
			 doNodeSignOff(container,cobDate,previousCobDate,nodeIds, true,batchIDs,nodeTotalPartition); 
			 nodeGroupingCtr = -1;
			 nodeIds = new StringBuilder(100);
		 }
		 
		 nodes = null;
		 nodeIds = null;
		 
	   
   }
   private void doNodeSignOff(String container,String cobDate, String previousCobDate,
    		           StringBuilder nodeIds, boolean done, Map<String,Long> batchIDs, int nodeTotalPartition ){
    
    	 StringBuilder sql  = new StringBuilder(700);	
		 //if(container .equals( ANZConstants.VAR_CONTAINER )  ){
		 if( isVarContainer(container)  ){
					
			 
			
			 sql.append(  " select SignOff.PortfolioName, SignOff.PortfolioID,SignOff.containerName, SignOff.LocationPath, SignOff.NodeId  " + 
				      " from DW.vw_SignOffAndExclude SignOff " +
				      " where SignOff.isExcluded='0'and SignOff.containerName = '" + container + "' " +
				      " and SignOff.COBDate = " + cobDate + " and SignOff.NodeId in("+ nodeIds.substring(0,nodeIds.length() - 1)  +") order by SignOff.NodeID asc");
			
		 } else {
			 sql.append(  "select SignOff.PortfolioName, SignOff.PortfolioID, ID,ContainerName,LocationPath,COBDate from DW.vw_SignOffAndExclude SignOff where isExcluded='0'" +
			 	      	" and COBDate=" + cobDate + 
						" AND containerName in('" + container + "') " +
						" AND SignOff.NodeId in("+ nodeIds.substring(0,nodeIds.length() - 1) +") " +
						" order by SignOff.NodeID asc");
		 }
		  
	    long batchId = System.currentTimeMillis() + System.nanoTime();
		try {
			long totalProcTime = executeSignOff( dbUtils.executeSQL( sql.toString()),batchId, container, cobDate,previousCobDate,done,batchIDs,nodeTotalPartition );
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
    }
    
   public  List<Map<String,Object>> getNodes(String cobDate, String container) throws SQLException {
        LOGGER.info("getNodes containerName=" + container + ",cobDate=" + cobDate );
        
    	String nodeSql = "Select distinct NodeID from DW.SignOffAndExclude where cobdate =" + cobDate 
    			+"  and containerName='"+container+ "' order by NodeID asc";
    	return dbUtils.executeSQL(nodeSql);
   }
   
 
 
   public long executeSignOff(List<Map<String, Object>> listOfPortfolio,
			long batchId, String containerName, String cobDate, 
			String previousCobDate, boolean done, Map<String,Long> batchIDs, int nodeTotalPartition ) {
		
	   try{
		
			  			if ( isVarContainer(containerName.toUpperCase())  ) {
				 				
								 if( containerName.equalsIgnoreCase("VAR_1D_AGG") ){
									 signOffVar1Day(listOfPortfolio, batchId,ANZConstants.VAR_CONTAINER, cobDate,previousCobDate,done,batchIDs,nodeTotalPartition );
									
								 } else if(containerName.equalsIgnoreCase("VAR_10D_AGG")) {	
						 	   		 signOffVar10Day(listOfPortfolio, batchId,ANZConstants.VAR_CONTAINER, cobDate,previousCobDate,done,batchIDs,nodeTotalPartition );
								 
								 } else if(containerName.equalsIgnoreCase("HYPO")){
									 signOffHypo(listOfPortfolio, batchId,ANZConstants.VAR_CONTAINER, cobDate,previousCobDate,done,batchIDs,nodeTotalPartition );
								
								 } else if(containerName.equalsIgnoreCase("VAR_1540_AGG")){
									 signOffVarSixYears(listOfPortfolio, batchId,ANZConstants.VAR_SIX_YEAR_CONTAINER, cobDate,previousCobDate,done,batchIDs,nodeTotalPartition );
				
								 } else if( containerName.toUpperCase().equals("VAR_STRESS")) {
									 signOffVarStress(listOfPortfolio, batchId,ANZConstants.VAR_STRESS_CONTAINER, cobDate,previousCobDate,done,batchIDs,nodeTotalPartition );
								 
								 } else if( containerName.toUpperCase().equals("VAR_STRESS_AGG")) {
									 signOffVarStress(listOfPortfolio, batchId,ANZConstants.VAR_STRESS_CONTAINER, cobDate,previousCobDate,done,batchIDs,nodeTotalPartition );
								}
						}else {
							signOffNonVAR(listOfPortfolio, batchId,containerName, cobDate,previousCobDate,done,batchIDs,nodeTotalPartition );
							
						}
			  
		
		LOGGER.info("Number of records to Signoff:" + listOfPortfolio.size());
      
		return 0l;
	   }catch(Exception e){
		   
		   e.printStackTrace();
	   }
		return 0l;
		   
	}
 
   private ExtractParamsDTO createParamDTO(String container, String fileBatchId, String cobDate, boolean isDone,
		                                     int nodeTotalPartition, ExtractType extractType , 
		                                     String varContainer,
		                                     List<String> buildLocationFilter, String previousCobDate, String varRefDateType ) {
	   
		ExtractParamsDTO extractParamDTO =   new ExtractParamsDTO( container,
                null, fileBatchId, cobDate,
                isDone,nodeTotalPartition, extractType);
		
		extractParamDTO.setPreviousCobDate(previousCobDate);
		
		extractParamDTO.setContainer(varContainer);
		extractParamDTO.setLocationPaths(buildLocationFilter);
		extractParamDTO.setVarRefDateType(varRefDateType);
		extractParamDTO.setDebug(this.debug);
		
		
		
		return extractParamDTO;
		
   }
   private  List<String> buildLocationFilter(List<Map<String, Object>> toBeExtracted,String containerName, ExtractType extractType){
		  List<String> locFilter = new ArrayList<String>();
		     
			   for (Map<String, Object> records : toBeExtracted) {
					String locPath = null;
					if (records.get("LocationPath") != null) {
						locPath = (String) records.get("LocationPath");
					}
					
					
					if (containerName.toUpperCase().contains("VAR")) {
						
						if (containerName.equalsIgnoreCase(ANZConstants.VAR_CONTAINER)||containerName.equalsIgnoreCase(ANZConstants.VAR_SIX_YEAR_CONTAINER)) {
							
							  if(extractType.equals( ExtractType.VAR_PNL) || extractType.equals( ExtractType.VAR_SIX_YEAR_PNL) ) {	
								   if(records.get("PortfolioID") != null ){
							    	 locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" +  ILocation.WILDCARD );
								   }
								   
							  } else if( extractType.equals( ExtractType.VAR_CONFIDENCE) || extractType.equals( ExtractType.VAR_SIX_YEAR_CONFIDENCE  ) ){
								    	 locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" +  ILocation.WILDCARD );
								  
							  }  else if( extractType.equals( ExtractType.HYPO  ) ){
									   if(records.get("PortfolioID") != null ){
										 locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" +  ILocation.WILDCARD );
									   }
									   
							  } else if(extractType.equals(ExtractType.HYPO_NODE)) {
								  locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" +  ILocation.WILDCARD );
								  
							  } else if(extractType.equals(ExtractType.VAR_PNL_PORTFOLIO) || extractType.equals(ExtractType.VAR_SIX_YEAR_PNL_PORTFOLIO) ) {
								  if(records.get("PortfolioID") != null ) {
									  locFilter.add(locPath + "|" + "M_PTFOLIO@Trading Portfolio" + ":" +  records.get("PortfolioName") );
								  }
							 }
							
						} else {
							if(extractType.equals(ExtractType.VAR_STRESS_PNL)) {
								  if(records.get("PortfolioID") != null ){ 
									 locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" +  ILocation.WILDCARD );
								   }
							}else if(extractType.equals(ExtractType.VAR_STRESS_PNL_PORTFOLIO)) {
								 if(records.get("PortfolioID") != null ) {
									  locFilter.add(locPath + "|" + "M_PTFOLIO@Trading Portfolio" + ":" +  records.get("PortfolioName") );
								  }
							} else if( extractType.equals( ExtractType.VAR_STRESS_CONFIDENCE  ) ){
							    	 locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" +  ILocation.WILDCARD );
							}	
						}
					} else {
						    
						       if(records.get("PortfolioID") != null ){
								 locFilter.add(locPath);
							   }
					}
				}
 			   LOGGER.info("Total Number of location/s:" + locFilter.size());
			   return locFilter;
	}		
   
   private long getBatchId(Map<String,Long> batchIDs, String batchKeyID){
	   
		if(batchIDs.get(batchKeyID) == null ){
		    long batchId = System.currentTimeMillis()+ System.nanoTime();
			batchIDs.put(batchKeyID, batchId);
		} 
		return batchIDs.get(batchKeyID);
   }
  
   
    private void createVarFuture(ExtractParamsDTO extractParamsDTO) {
		try {
        LOGGER.info("CREATING VAR FUTURE:"+ extractParamsDTO.getFileBatchId() + " - APQueryPool Size:" + extractAPQueryPool.getQueryQueue().size() + ",VarExtract type:" + extractParamsDTO.getExtractType());
			
        	dbUtils.spUpdateExtractStatus(extractParamsDTO.getCobDate(), getDbContainer(extractParamsDTO), "CubeDataExtract");
			
			extractAPQueryPool.getQueryQueue().put(	new ExtractVarTask( extractUtils, extractParamsDTO,vectorizerPool.getVectorizerQueue(),
							extractFileWriterPool.getFileWriterQueue(),extractAPQueryPool, retryTime,retryCount, dbUtils ) );
	
		} catch(Exception e){
			e.printStackTrace();
		}  
	}

   private void createNonVarFuture(ExtractParamsDTO extractParamsDTO) {
		try {
			
			LOGGER.info("CREATING NON-VAR FUTURE:"+ extractParamsDTO.getFileBatchId() + " - APQueryPool Size:" + extractAPQueryPool.getQueryQueue().size() );
			dbUtils.spUpdateExtractStatus(extractParamsDTO.getCobDate(), getDbContainer(extractParamsDTO), "CubeDataExtract");
			extractAPQueryPool.getQueryQueue().put(	new ExtractNonVarTask(extractUtils, extractParamsDTO, extractFileWriterPool.getFileWriterQueue(),
					extractAPQueryPool, retryTime,retryCount, dbUtils));
			
		}catch (InterruptedException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();  
		}
	}
   
   private int nodePartionCount( int totalNode, int nodeGrouping ){
   	int x = totalNode / nodeGrouping;
   	int y = totalNode % nodeGrouping;
   
   	if(y > 0 ){
   		x++;
   	}
   	return x;
   }
   
    private boolean isVarContainer(String containerName){
	   
        return (containerName.equalsIgnoreCase("HYPO")           ||
		   containerName.equalsIgnoreCase("VAR_10D_AGG")         ||
		   containerName.equalsIgnoreCase("VAR_1D_AGG")          ||
		   containerName.equalsIgnoreCase("VAR_STRESS_AGG")      ||
		   containerName.equalsIgnoreCase("VAR_STRESS") 	     ||
		   containerName.equalsIgnoreCase("VAR_1540_AGG") );
	}
   
    private String getDbContainer(ExtractParamsDTO extractParamDto){
    	
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
    
    private boolean gotExclude(String container, String cobDate){
        
    	String excludeSql = " select count(*) as count from DW.vw_SignOffAndExclude SignOff " + 
			                " where SignOff.isExcluded = '1' " +
			                " and SignOff.containerName = '" + container + "' " +
			                " and SignOff.COBDate = " + cobDate ;
			      
    	try {
			List<Map<String,Object>> excluded =  dbUtils.executeSQL(excludeSql);
			if(excluded!=null && excluded.size()>0){
				
				
				LOGGER.info("Got excluded count:" +  excluded.get(0).get("count") );
				
				if( (Integer) excluded.get(0).get("count") > 0){
					return true;
				} else {
					return false;
				}
				
			 //	return  excluded.get(0).get("count").equals("0")==true;
			}
    	} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
        
    	return false;   	
    }
    
    private boolean renameExistingAPX(String dir, String psrCode,String container,
    		String varType, String date, String fileExtension, String newExtension){
          
        final String  fileName  =   psrCode+ "#" + container + "#"+varType+"#.*.#" + date + "#.*."+ fileExtension;
        
    	File filesArray[] = new File(dir).listFiles(new FileFilter(){
    		private Pattern refPattern = Pattern.compile(fileName, Pattern.CASE_INSENSITIVE);;
   			@Override
				public boolean accept(File file) {
					return refPattern.matcher(file.getName()).matches();
				}
    		}
    	);
    	if(filesArray!=null && filesArray.length>0){
    		String newApxName = filesArray[0].getName().replace(fileExtension, newExtension);
    		return  filesArray[0].renameTo( new File( filesArray[0].getParent()+ filesArray[0].separator + newApxName )  );
    	}
    	return false;
    }
     
    private void signOffVar1Day(List<Map<String, Object>> listOfPortfolio,
			                   long batchId, String containerName, String cobDate, 
			                   String previousCobDate, 
			                   boolean done, Map<String,Long> batchIDs, int nodeTotalPartition ){
	
    	  createVarFuture( createParamDTO( ANZConstants.VAR_CONTAINER, String.valueOf( getBatchId( batchIDs,  "VAR_1D")),
					cobDate, done, nodeTotalPartition, ExtractType.VAR_CONFIDENCE, "VAR_1D",
					buildLocationFilter(listOfPortfolio, ANZConstants.VAR_CONTAINER,    ExtractType.VAR_CONFIDENCE ),previousCobDate,"VAR" ) );
			 
		  
		   createVarFuture( createParamDTO(  ANZConstants.VAR_CONTAINER, String.valueOf( getBatchId( batchIDs,  "VAR_1D_PNL")),
					cobDate, done, nodeTotalPartition, ExtractType.VAR_PNL, "VAR_1D",
					buildLocationFilter(listOfPortfolio,ANZConstants.VAR_CONTAINER, ExtractType.VAR_PNL ),previousCobDate,"VAR"));
			   
			   createVarFuture( createParamDTO( ANZConstants.VAR_CONTAINER, String.valueOf( getBatchId( batchIDs,  "VAR_1D_PNL_PORTFOLIO")),
					   cobDate, done, nodeTotalPartition, ExtractType.VAR_PNL_PORTFOLIO, "VAR_1D",
						buildLocationFilter(listOfPortfolio,ANZConstants.VAR_CONTAINER, ExtractType.VAR_PNL_PORTFOLIO ),previousCobDate,"VAR") );

    	
    }

    private void signOffVar10Day(List<Map<String, Object>> listOfPortfolio,
            long batchId, String containerName, String cobDate, 
            String previousCobDate, 
            boolean done, Map<String,Long> batchIDs, int nodeTotalPartition ){
    	
         	createVarFuture( createParamDTO( ANZConstants.VAR_CONTAINER, String.valueOf( getBatchId( batchIDs,  "VAR_10D")),
        	cobDate, done, nodeTotalPartition, ExtractType.VAR_CONFIDENCE,"VAR_10D",
			buildLocationFilter(listOfPortfolio,ANZConstants.VAR_CONTAINER, ExtractType.VAR_CONFIDENCE ),previousCobDate,"VAR"));
 
  
	   		createVarFuture( createParamDTO( ANZConstants.VAR_CONTAINER, String.valueOf( getBatchId( batchIDs,  "VAR_10D_PNL")),
	   				cobDate, done, nodeTotalPartition, ExtractType.VAR_PNL, "VAR_10D",
			buildLocationFilter(listOfPortfolio,ANZConstants.VAR_CONTAINER, ExtractType.VAR_PNL ),previousCobDate,"VAR"));
	   		
	   		createVarFuture( createParamDTO( ANZConstants.VAR_CONTAINER, String.valueOf( getBatchId( batchIDs,  "VAR_10D_PNL_PORTFOLIO")),
	   				cobDate, done, nodeTotalPartition, ExtractType.VAR_PNL_PORTFOLIO, "VAR_10D",
			buildLocationFilter(listOfPortfolio,ANZConstants.VAR_CONTAINER, ExtractType.VAR_PNL_PORTFOLIO ),previousCobDate,"VAR"));

    }
    
    private void signOffHypo(List<Map<String, Object>> listOfPortfolio,
            long batchId, String containerName, String cobDate, 
            String previousCobDate, 
            boolean done, Map<String,Long> batchIDs, int nodeTotalPartition ){
    	
      	        createVarFuture( createParamDTO( ANZConstants.VAR_CONTAINER, String.valueOf( getBatchId( batchIDs,  "HYPO_PORTFOLIO")),
				cobDate, done, nodeTotalPartition, ExtractType.HYPO_NODE, "HYPO",
				buildLocationFilter(listOfPortfolio,ANZConstants.VAR_CONTAINER, ExtractType.HYPO_NODE ),previousCobDate,"VAR"));
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				createVarFuture( createParamDTO( ANZConstants.VAR_CONTAINER, String.valueOf( getBatchId( batchIDs,  "HYPO")),
				cobDate, done, nodeTotalPartition, ExtractType.HYPO,"HYPO",
				buildLocationFilter(listOfPortfolio,ANZConstants.VAR_CONTAINER, ExtractType.HYPO ),previousCobDate,"VAR" )  );
  
    } 
    
    private void signOffVarStress(List<Map<String, Object>> listOfPortfolio,
            long batchId, String containerName, String cobDate, 
            String previousCobDate, 
            boolean done, Map<String,Long> batchIDs, int nodeTotalPartition ){
    	
    
	   		createVarFuture( createParamDTO( ANZConstants.VAR_STRESS_CONTAINER, String.valueOf( getBatchId( batchIDs,  ANZConstants.VAR_STRESS_CONTAINER + "_PNL")),
	   				cobDate, done, nodeTotalPartition, ExtractType.VAR_STRESS_PNL, "VAR_STRESS",
			buildLocationFilter(listOfPortfolio,ANZConstants.VAR_STRESS_CONTAINER, ExtractType.VAR_STRESS_PNL ),cobDate, "VAR_STRESS"));
	   		
	   		
	   		createVarFuture( createParamDTO( ANZConstants.VAR_STRESS_CONTAINER, String.valueOf( getBatchId( batchIDs,   ANZConstants.VAR_STRESS_CONTAINER +  "_PNL_PORTFOLIO")),
	   				cobDate, done, nodeTotalPartition, ExtractType.VAR_STRESS_PNL_PORTFOLIO,  "VAR_STRESS",
			buildLocationFilter(listOfPortfolio,ANZConstants.VAR_STRESS_CONTAINER, ExtractType.VAR_STRESS_PNL_PORTFOLIO ),cobDate,"VAR_STRESS"));


         	createVarFuture( createParamDTO( ANZConstants.VAR_STRESS_CONTAINER , String.valueOf( getBatchId( batchIDs,  ANZConstants.VAR_STRESS_CONTAINER +   "_CONFIDENCE")),
        	cobDate, done, nodeTotalPartition, ExtractType.VAR_STRESS_CONFIDENCE,"VAR_STRESS",
			buildLocationFilter(listOfPortfolio,ANZConstants.VAR_STRESS_CONTAINER, ExtractType.VAR_STRESS_CONFIDENCE),cobDate,"VAR_STRESS"));
 
  
    
    }
    
    private void signOffVarSixYears(List<Map<String, Object>> listOfPortfolio,
            long batchId, String containerName, String cobDate, 
            String previousCobDate, 
            boolean done, Map<String,Long> batchIDs, int nodeTotalPartition ){
    	
         	createVarFuture( createParamDTO( ANZConstants.VAR_SIX_YEAR_CONTAINER, String.valueOf( getBatchId( batchIDs,  "VAR_SIX_YEAR")),
        	cobDate, done, nodeTotalPartition, ExtractType.VAR_SIX_YEAR_CONFIDENCE, ANZConstants.VAR_SIX_YEAR_CONTAINER,
			buildLocationFilter(listOfPortfolio,ANZConstants.VAR_SIX_YEAR_CONTAINER, ExtractType.VAR_SIX_YEAR_CONFIDENCE ),previousCobDate,"VARSIXYEARDATES"));
 
  
	   		createVarFuture( createParamDTO( ANZConstants.VAR_SIX_YEAR_CONTAINER, String.valueOf( getBatchId( batchIDs,  "VAR_SIX_YEAR_PNL")),
	   				cobDate, done, nodeTotalPartition, ExtractType.VAR_SIX_YEAR_PNL, ANZConstants.VAR_SIX_YEAR_CONTAINER,
			buildLocationFilter(listOfPortfolio,ANZConstants.VAR_SIX_YEAR_CONTAINER, ExtractType.VAR_SIX_YEAR_PNL ),previousCobDate,"VARSIXYEARDATES"));
	   		
	   		createVarFuture( createParamDTO( ANZConstants.VAR_SIX_YEAR_CONTAINER, String.valueOf( getBatchId( batchIDs,  "VAR_SIX_YEAR_PNL_PORTFOLIO")),
	   				cobDate, done, nodeTotalPartition, ExtractType.VAR_SIX_YEAR_PNL_PORTFOLIO, ANZConstants.VAR_SIX_YEAR_CONTAINER,
			buildLocationFilter(listOfPortfolio,ANZConstants.VAR_SIX_YEAR_CONTAINER, ExtractType.VAR_SIX_YEAR_PNL_PORTFOLIO ),previousCobDate,"VARSIXYEARDATES"));

    }
    
    private void signOffNonVAR(List<Map<String, Object>> listOfPortfolio,
            long batchId, String containerName, String cobDate, 
            String previousCobDate, 
            boolean done, Map<String,Long> batchIDs, int nodeTotalPartition ){
    	
    	ExtractParamsDTO nonVar = new ExtractParamsDTO( containerName ,
                null, String.valueOf(batchId), cobDate,
                done,nodeTotalPartition, ExtractType.NON_VAR);
		
		nonVar.setFileBatchId( String.valueOf( getBatchId( batchIDs, nonVar.getContainer() ) ));
		nonVar.setPreviousCobDate( cobDate);
		
		nonVar.setLocationPaths( buildLocationFilter(listOfPortfolio,containerName, ExtractType.NON_VAR ));
		nonVar.setDebug(this.debug);
		
		createNonVarFuture(nonVar);
	
    }

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
    
}