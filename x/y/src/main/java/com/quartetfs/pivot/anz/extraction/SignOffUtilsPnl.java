package com.quartetfs.pivot.anz.extraction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.export.ExtractAPQueryPool;
import com.quartetfs.pivot.anz.service.export.ExtractFileWriterPool;
import com.quartetfs.pivot.anz.service.export.ExtractNonVarTask;
import com.quartetfs.pivot.anz.service.export.ExtractObject.ExtractType;
import com.quartetfs.pivot.anz.service.export.ExtractVarTask;
import com.quartetfs.pivot.anz.service.export.VectorizerPool;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.DBUtils;
import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;

public class SignOffUtilsPnl implements Cloneable {
	
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private int nodeGroupingCount;
	private DBUtils      dbUtils;
	private ExtractUtils extractUtils;


	public SignOffUtilsPnl(
			DBUtils dbUtils, ExtractFileWriterPool extractFileWriterPool,
			VectorizerPool vectorizerPool, ExtractAPQueryPool extractAPQueryPool,
			ExtractUtils extractUtils,int nodeGroupingCount) {
		
		
		this.dbUtils = dbUtils;
		this.extractFileWriterPool = extractFileWriterPool;
		this.vectorizerPool = vectorizerPool;
		this.extractAPQueryPool = extractAPQueryPool;
		this.extractUtils = extractUtils;
		this.nodeGroupingCount = nodeGroupingCount;
		
	}

	private ExtractFileWriterPool extractFileWriterPool;
    private VectorizerPool vectorizerPool;
    private ExtractAPQueryPool extractAPQueryPool;
   
   public void doSignOff( String cobDate, String previousCobDate, String[] containerList) {
		        
				Validate.notNull(cobDate,"Cob Date name can't be null");
				LOGGER.info("SignOff by container start:" );
			try{	
			  if(containerList==null) {	
				String sql =  "select distinct(containerName) from DW.vw_SignOffAndExclude where isExcluded='0' and COBDate=" + cobDate + 
						" AND portfolioID is not null";
			 	  List<Map<String,Object>> listOfContainers = dbUtils.executeSQL(sql);
			 	  LOGGER.info("Number of container to be sign off:" + listOfContainers.size() );
			 	  containerList = new String[listOfContainers.size()];
			 	  int ctr = 0; 
			 	  for (Map<String, Object> record : listOfContainers) {
			 		 containerList[ctr] = (String) record.get("containerName");
			 		 ctr++;
				  }  
			  } else {
				  LOGGER.info("containerList is NOT null");
			  }
		
			 	 LOGGER.info("Number of containerList to be sign off:" + containerList.length);
					
				
				for (int i = 0; i < containerList.length; i++) { // container list
					 String container = containerList[i];
					 dbUtils.spUpdateExtractStatus(cobDate, container, "CubeDataExtract");
					
					 List<Map<String,Object>> nodes = getNodes(cobDate,container);
					 
					 if(nodes!=null && nodes.size()  > 0 ) {
						 processNode( container, cobDate,  previousCobDate,  nodes);
					 }
					 
					 nodes = null;
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}	finally{
				
				containerList = null;
				
			}
				
				
	}
   
   private void processNode(String container,String cobDate, String previousCobDate, List<Map<String,Object>> nodes){
	   
	   StringBuilder nodeIds = new StringBuilder(100);
		 int nodeGroupingCtr =-1;
		 int nodeGrouping = nodeGroupingCount; // Number of node to be consolidated in cube request
		 nodeGrouping = 100;   
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
	
		 if(nodeGroupingCtr>0){
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
		 if(container.equals( ANZConstants.VAR_CONTAINER )  ){
//			 sql.append(  " select SignOff.PortfolioID,PositionMaster.DimPositionMasterID,SignOff.containerName, SignOff.LocationPath, SignOff.NodeId  " + 
//				      " from DW.vw_SignOffAndExclude SignOff " +
//				      " left join DW.DimPositionMaster PositionMaster " +
//				      " on PositionMaster.DimPortfolioID = SignOff.PortfolioID " +
//				      " where SignOff.isExcluded='0'and SignOff.containerName = '" + container + "' " +
//				      " and SignOff.COBDate = " + cobDate + " and SignOff.NodeId in("+ nodeIds.substring(0,nodeIds.length() - 1)  +")");
//			
			
			 sql.append(  " select SignOff.PortfolioID,SignOff.containerName, SignOff.LocationPath, SignOff.NodeId  " + 
				      " from DW.vw_SignOffAndExclude SignOff " +
				      " where SignOff.isExcluded='0'and SignOff.containerName = '" + container + "' " +
				    
				      " and SignOff.COBDate = " + cobDate + " and SignOff.NodeId in("+ nodeIds.substring(0,nodeIds.length() - 1)  +") order by SignOff.NodeID asc");
			
		 } else {
			 sql.append(  extractUtils.getSignOffSql() + " and COBDate=" + cobDate + 
						" AND containerName in('" + container + "') " +
						" AND and SignOff.NodeId in("+ nodeIds.substring(0,nodeIds.length() - 1) +") order by SignOff.NodeID asc");
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
    	String nodeSql = "Select distinct NodeID from DW.SignOffAndExclude where cobdate =" + cobDate 
    			+"  and containerName='"+container+ "' order by NodeID asc";
    	return dbUtils.executeSQL(nodeSql);
   }
   
   public  List<Map<String,Object>> getChildPortfolio(String cobDate, String container, String nodeId) throws SQLException{
    	String childPortfolioSql =   " select SignOff.PortfolioID,PositionMaster.DimPositionMasterID,SignOff.containerName, SignOff.LocationPath, SignOff.NodeId  " + 
			      " from DW.vw_SignOffAndExclude SignOff " +
			      " left join DW.DimPositionMaster PositionMaster " +
			      " on PositionMaster.DimPortfolioID = SignOff.PortfolioID " +
			      " where SignOff.isExcluded='0'and SignOff.containerName = '" + container + "' " +
			      " and SignOff.COBDate = " + cobDate + " and SignOff.NodeId in("+ nodeId +")";
    	          return dbUtils.executeSQL(childPortfolioSql);
    }
    
   public long executeSignOff(List<Map<String, Object>> listOfPortfolio,
			long batchId, String containerName, String cobDate, 
			String previousCobDate, boolean done, Map<String,Long> batchIDs, int nodeTotalPartition ) {
		
		ExtractParamsDTO extractParamsDTO = new ExtractParamsDTO( containerName,null, String.valueOf(batchId), 
				           cobDate, previousCobDate,done,nodeTotalPartition, null);
		
		
			  if (extractParamsDTO.getContainer().toUpperCase().contains("VAR")) {
							if (extractParamsDTO.getContainer().toUpperCase().equals(ANZConstants.VAR_CONTAINER.toUpperCase())) {
								
								/*createVarFuture( createParamDTO( extractParamsDTO.getContainer(), String.valueOf( getBatchId( batchIDs,  "VAR_1D")),
										extractParamsDTO.getCobDate(), done, nodeTotalPartition, ExtractType.VAR_CONFIDENCE,"VAR_1D",
										buildLocationFilter(listOfPortfolio,extractParamsDTO, ExtractType.VAR_CONFIDENCE )));
					*/		
								/*createVarFuture( createParamDTO( extractParamsDTO.getContainer(), String.valueOf( getBatchId( batchIDs,  "VAR_1D_PNL")),
										extractParamsDTO.getCobDate(), done, nodeTotalPartition, ExtractType.VAR_PNL, "VAR_1D",
										buildLocationFilter(listOfPortfolio,extractParamsDTO, ExtractType.VAR_PNL )));*/
								
/*
								createVarFuture( createParamDTO( extractParamsDTO.getContainer(), String.valueOf( getBatchId( batchIDs,  "VAR_10D")),
										extractParamsDTO.getCobDate(), done, nodeTotalPartition, ExtractType.VAR_CONFIDENCE,"VAR_10D",
										buildLocationFilter(listOfPortfolio,extractParamsDTO, ExtractType.VAR_CONFIDENCE )));
						*/		
							   	
							
								createVarFuture( createParamDTO( extractParamsDTO.getContainer(), String.valueOf( getBatchId( batchIDs,  "VAR_10D_PNL")),
										extractParamsDTO.getCobDate(), done, nodeTotalPartition, ExtractType.VAR_PNL, "VAR_10D",
										buildLocationFilter(listOfPortfolio,extractParamsDTO, ExtractType.VAR_PNL )));
							
								
							/*	createVarFuture( createParamDTO( extractParamsDTO.getContainer(), String.valueOf( getBatchId( batchIDs,  "HYPO")),
										extractParamsDTO.getPreviousCobDate(), done, nodeTotalPartition, ExtractType.HYPO,"HYPO",
										buildLocationFilter(listOfPortfolio,extractParamsDTO, ExtractType.HYPO )));
							
		*/				
								
							
							} else {
								
								ExtractParamsDTO varStress = new ExtractParamsDTO(extractParamsDTO.getContainer(),
						                                                          null,extractParamsDTO.getFileBatchId(),
						                                                          extractParamsDTO.getCobDate(),done,nodeTotalPartition,
						                                                          ExtractType.VAR_STRESS);
								
								varStress.setFileBatchId( String.valueOf( getBatchId( batchIDs, varStress.getContainer() ) ));
								varStress.setLocationPaths(  buildLocationFilter(listOfPortfolio,extractParamsDTO, ExtractType.VAR_STRESS) );
								createVarFuture(varStress);
							}
						}else {
							ExtractParamsDTO nonVar = new ExtractParamsDTO(extractParamsDTO.getContainer(),
					                  null,extractParamsDTO.getFileBatchId(),extractParamsDTO.getCobDate(),
					                  done,nodeTotalPartition, ExtractType.NON_VAR);
							
							nonVar.setFileBatchId( String.valueOf( getBatchId( batchIDs, nonVar.getContainer() ) ));
							
							nonVar.setLocationPaths( buildLocationFilter(listOfPortfolio,extractParamsDTO, ExtractType.NON_VAR ));
							createNonVarFuture(nonVar);
						}
		
		LOGGER.info("Number of records to Signoff:" + listOfPortfolio.size());
      
		return 0l;
	}
 
   private ExtractParamsDTO createParamDTO(String container, String fileBatchId, String cobDate, boolean isDone,
		                                     int nodeTotalPartition, ExtractType extractType , 
		                                     String varContainer,List<String> buildLocationFilter ){
	   
		ExtractParamsDTO extractParamDTO =   new ExtractParamsDTO( container,
                null, fileBatchId, cobDate,
                isDone,nodeTotalPartition, extractType);
		
		extractParamDTO.setContainer(varContainer);
		extractParamDTO.setLocationPaths(buildLocationFilter);
		return extractParamDTO;
		
   }
   private  List<String> buildLocationFilter(List<Map<String, Object>> toBeExtracted,ExtractParamsDTO extractParamsDTO, ExtractType extractType){
		  List<String> locFilter = new ArrayList<String>();
		     
			   for (Map<String, Object> records : toBeExtracted) {
					String locPath = null;
					if (records.get("LocationPath") != null) {
						locPath = (String) records.get("LocationPath");
					}
					extractUtils.validateRequest(extractParamsDTO);
					if (extractParamsDTO.getContainer().toUpperCase().contains("VAR")) {
						
						if (extractParamsDTO.getContainer().toUpperCase().equals(ANZConstants.VAR_CONTAINER.toUpperCase())) {
							
							if(extractType.equals( ExtractType.VAR_PNL   )) {	
							    
							    if(records.get("DimPositionMasterID") == null) {
								   if(records.get("PortfolioID") != null ){
							    	 locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" +  ILocation.WILDCARD );
								   }
							    } else {
							    	 if(records.get("PortfolioID") != null ){
							    		 locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" + records.get("DimPositionMasterID"));
							    	 }
							    }
							     
							  } else if( extractType.equals( ExtractType.VAR_CONFIDENCE  ) ){
								   if(records.get("DimPositionMasterID") == null) {
										locFilter.add(locPath + "|" + "" + ":" +  "" );
									}else{
										locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" + records.get("DimPositionMasterID"));
									}  
								  
							  }  else if( extractType.equals( ExtractType.HYPO  ) ){
								   if(records.get("DimPositionMasterID") == null) {
									   if(records.get("PortfolioID") != null ){
										 locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" +  ILocation.WILDCARD );
									   }
									}else{
										if(records.get("PortfolioID") != null ){
											locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" + records.get("DimPositionMasterID"));
										}
									}  
							  } 
							  
							  
						} else { // var_stress container
		//					locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" + records.get("DimPositionMasterID"));
			
							 if(records.get("DimPositionMasterID") == null) {
								   if(records.get("PortfolioID") != null ){
									 locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" +  ILocation.WILDCARD );
								   }
								}else{
									if(records.get("PortfolioID") != null ){
										locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" + records.get("DimPositionMasterID"));
									}
								}  
						}
						
					} else {
						//locFilter.add(locPath);
						   if(records.get("DimPositionMasterID") == null) {
							   if(records.get("PortfolioID") != null ){
								 locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" +  ILocation.WILDCARD );
							   }
							}else{
								if(records.get("PortfolioID") != null ){
									locFilter.add(locPath + "|" + "Position ID@Position ID" + ":" + records.get("DimPositionMasterID"));
								}
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
	/*	try {
//LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> CREATING FUTURE:"+ extractParamsDTO.getFileBatchId() + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< isDone:" + extractParamsDTO.isDone());
			extractAPQueryPool.getQueryQueue().put(	new ExtractVarTask(extractUtils, extractParamsDTO,vectorizerPool.getVectorizerQueue(),
							extractFileWriterPool.getFileWriterQueue(),this ));
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}*/
	}

   private void createNonVarFuture(ExtractParamsDTO extractParamsDTO) {
		/*try {
			extractAPQueryPool.getQueryQueue().put(	new ExtractNonVarTask(extractUtils, extractParamsDTO, extractFileWriterPool.getFileWriterQueue()));
		
		}catch (InterruptedException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}*/
	}
   
   private int nodePartionCount( int totalNode, int nodeGrouping ){
   	int x = totalNode / nodeGrouping;
   	int y = totalNode % nodeGrouping;
   
   	if(y > 0 ){
   		x++;
   	}
   	return x;
   }
   
   
}