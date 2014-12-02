package com.anz.rer.etl.csvToTable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.anz.rer.etl.utils.DbUtils;
import com.anz.rer.etl.utils.GlobalStatusUtil;

public class BcpUtil {
	
	private final static Logger logger = Logger.getLogger(BcpUtil.class);
	 
	private BlockingQueue<BcpData> cleanUpQueue;
	private BlockingQueue<BcpData> doneQueue;
	  
	private boolean insertToHeader;
	private boolean insertToFact;
	  
	private int retryThreshold;
	private DbUtils dbUtils;
	private GlobalStatusUtil globalStatus;
	
	 
	
	private static final Map<String,String> varContainerMapping = new HashMap<String,String>();	
	static {
			varContainerMapping.put("B1AL0", "HYPO");
			varContainerMapping.put("VXAL0", "VAR_10D_AGG");
			varContainerMapping.put("V1AL0", "VAR_1D_AGG");
			varContainerMapping.put("VSAL0", "VAR_STRESS_AGG");
			varContainerMapping.put("VFAL0", "VAR_1540_AGG");
	}
	
	public BcpUtil(BlockingQueue<BcpData> cleanUpQueue,
			BlockingQueue<BcpData> doneQueue, boolean insertToHeader,
			boolean insertToFact, int retryThreshold, DbUtils dbUtils,GlobalStatusUtil globalStatus) {
		super();
		this.cleanUpQueue = cleanUpQueue;
		this.doneQueue = doneQueue;
		this.insertToHeader = insertToHeader;
		this.insertToFact = insertToFact;
		this.retryThreshold = retryThreshold;
		this.dbUtils = dbUtils;
		this.globalStatus =  globalStatus;
	}

	public void process(BcpData bcpData) {

		StringBuilder headerDetails = new StringBuilder(this.strArrToStringBuilder( bcpData.getHeader() , ","));
		logger.info("Data header details length:" + headerDetails.length());
		logger.info("BcpData State:" + bcpData.getState() + " status:" + bcpData.getStatus()+",VarType:" + bcpData.getVarType());
		
		
		if (headerDetails != null && headerDetails.length() > 0) {
			try {
				
				if (!insertToHeader ) {
					logger.debug("Will not insert record to Header Table");
					bcpData.setState(BcpConstants.FACT); 
					
				} else {
					// insert to header table
					if(!bcpData.getStatus().equals("failed") &&  !bcpData.getStatus().equals("failed-bcp")){
					     if(insertToHeaderTable(bcpData)){
					    	
					     } else{
					    	 if( bcpData.getVarType().equals("VAR_1D") ||  bcpData.getVarType().equals("VAR_10D") 
					    			 ||  bcpData.getVarType().equals("VAR_STRESS") ||  bcpData.getVarType().equals("VAR_1540") ){
					    		 
					      		 globalStatus.getStatus().put( bcpData.getKey(), "error");
					      		
					      		 logger.info("setting status to error:" +  bcpData.getName() + "status:" + 
					      				 bcpData.getKey() + ",Failed Insert To Header"); 
								
					    	 } else  if(bcpData.getExtractType().equals("HYPO") ) {
					    		 globalStatus.getStatus().put(  "HYPO_NODE#HYPO_NODE#" + bcpData.getCobDate(), "error");
					    	 }
					     }
					}
				}	
		
				logger.debug( "Insert To Fact Table:" + insertToFact);
				if ( bcpData.getStatus().equals("failed")|| bcpData.getStatus().equals("failed-bcp") ) {
					bcpData.setStatus("failed");
					bcpData.setState(BcpConstants.CLEANUP);
					cleanUpQueue.put(bcpData);
 
			     } else if (insertToFact==false ) {
					logger.debug("Will not push to FACT table");
					bcpData.setStatus("success-no-insert");
					bcpData.setState(BcpConstants.CLEANUP);
					logger.debug("B. PUTTING TO CLEANUP QUEUE :" + bcpData.getName());
					cleanUpQueue.put(bcpData);
				} else {
					logger.info("Pushing to Fact Table:" + bcpData.getId());
					
					pushStagingToFactTable(bcpData, headerDetails);
					
					logger.info("Done publishg to fact table:" + bcpData.getId());
					
					
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					logger.info("Unexpected exception:" + e.getLocalizedMessage());
					bcpData.setStatus("PublishToWarehouseStorageError");	 
					dbUtils.updateStatusInDB(bcpData, null);
					
					if( bcpData.getVarType().equals("VAR_1D") ||  bcpData.getVarType().equals("VAR_10D") ||  bcpData.getVarType().equals("VAR_STRESS") 
							||  bcpData.getVarType().equals("VAR_1540")){
						
						dbUtils.updateStatusInDB(bcpData, "PublishToWarehouseStorageError");
						logger.info("setting status to error:" +  bcpData.getName() + "status:" +  bcpData.getKey() + ",PublishToWarehouseStorageError");
						
						globalStatus.getStatus().put( bcpData.getKey(), "error");
					} 
			
					bcpData.setStatus("failed");
					logger.debug("C. PUTTING TO CLEANUP QUEUE :" + bcpData.getName());
					cleanUpQueue.put(bcpData);
					
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}

		} else {
			logger.info("header is zero:");
		}
		return;
		
	}
	
	private boolean insertToHeaderTable(BcpData bcpData) throws InterruptedException {
		logger.info("insertToHeaderTable State:" + bcpData.getState());
		
		bcpData.setStart(System.currentTimeMillis());
		
		if (bcpData.getState().equals(BcpConstants.HEADER)	|| bcpData.getState().equals(BcpConstants.HEADER_RETRY)) {
			
			String headerSql = this.getHeaderSql(bcpData, this.getContainer(bcpData.getName()));

			if (bcpData.getState().equals(BcpConstants.HEADER)) {
				logger.info("Inserting to header table:" + headerSql);
			} else {
				logger.info("Retry " + bcpData.getRetry() + " Inserting to header table:" + headerSql);
			}

			try {
				
				if (!dbUtils.executeSp(headerSql)) {
					return false;
			
				} else {
					bcpData.setRetry(-1);
					bcpData.setState(BcpConstants.FACT);
					bcpData.setStatus("success");
					logger.info(bcpData.totalExecutionTime(	"Done insert to header table:" + bcpData.getState(),System.currentTimeMillis()));
				}
				
			} catch(SQLException e){
				
					 logger.info("InsertToHeaderTable SQL exception:" + e.getLocalizedMessage());
					 bcpData.setStatus("PublishingToWarehouseError");	//bcperror
					 dbUtils.updateStatusInDB(bcpData,null);
					 bcpData.setStatus("failed");
					 return false; 
				
			} catch (Exception e) {
				logger.debug("InsertToHeaderTable Unexpected exception:" + e.getLocalizedMessage());
				bcpData.setStatus("PublishingToWarehouseError");	//bcperror
				dbUtils.updateStatusInDB(bcpData,null);
				bcpData.setStatus("failed");
			    return false;	 
			}
		}
		return true;
	}
	
	private void pushStagingToFactTable(BcpData bcpData, StringBuilder headerDetails) throws InterruptedException {
		
		bcpData.setStart(System.currentTimeMillis());
		
		logger.info("Push Staging To FactTable:" + bcpData.getState());
		
		if(bcpData.getState().equals(BcpConstants.FACT) || bcpData.getState().equals(BcpConstants.FACT_RETRY) ||
				bcpData.getState().equals("staging-fact") ) {
		
			try{	
				 String stgToFctSql = this.stagingToFactSql(bcpData, headerDetails) ;
			     
			     dbUtils.updateStatusInDB(bcpData,"PublishingToWarehouse");
			     if( !dbUtils.executeSp(stgToFctSql) ) {
			    	    if( bcpData.getVarType().equals("VAR_1D") ||  bcpData.getVarType().equals("VAR_10D") 
			    	    		||  bcpData.getVarType().equals("VAR_STRESS") ||  bcpData.getVarType().equals("VAR_1540") ){ // any type of var
							
			    	    	dbUtils.updateStatusInDB(bcpData, "PublishToWarehouseStorageError");
							logger.info("setting status to error:" +  bcpData.getName() + "status:" +  bcpData.getKey() + ",PublishToWarehouseStorageError");
							globalStatus.getStatus().put( bcpData.getKey(), "error");
						} 
			    	 
			     } else {
			    	
			    	// handle hypo_node
			    	 if( bcpData.getExtractType().equals("HYPO") ){
			    	      if(!handleHyponode(bcpData) ){
			    	    	  bcpData.setStatus("failed");
			    	    	  globalStatus.getStatus().put(  "HYPO_NODE#HYPO_NODE#" + bcpData.getCobDate(), "error");
			    	          return;
			    	      } else {
			    	    	  globalStatus.getStatus().put(  "HYPO_NODE#HYPO_NODE#" + bcpData.getCobDate(), "success");
						    	 
			    	      }
			    	    
			    	 }
			    	 
			    	handleSuccess(bcpData, "Push from Staging to Fact Table:");
				 	
			    	/** if varconfidence is done executing the SP, do not update to PublishingToSubcube **/
				 	if( bcpData.getName().contains("VAR-VAR_1D") || bcpData.getName().contains("VAR-VAR_10D")
				 			|| bcpData.getName().contains("VAR-VAR_STRESS") || bcpData.getName().contains("VAR-VAR_1540") ) { //var confidence level
				 		 
				 		   logger.info("will not update status to PublishingToSubcube:" + bcpData.getName());
				 		   logger.info("setting status to suces:" +  bcpData.getName() + "status:" +  bcpData.getKey() );
						   globalStatus.getStatus().put( bcpData.getKey(), "success");
				 		  
				 	} else if(bcpData.getName().contains("PNL-VAR_1D") || bcpData.getName().contains("PNL-VAR_10D") 
				 			||bcpData.getName().contains("PNL-VAR_STRESS") ||bcpData.getName().contains("PNL-VAR_1540") ) {  //pnl node level
				 	
				 	      String conStatus = this.getSignOffStatus( varContainerMapping.get(bcpData.getPsrCode()),  bcpData.getCobDate()  );
						 
				 	      if(conStatus!=null && (conStatus.equals("PublishingToStagingError")
							       || conStatus.equals("PublishingToWarehouseError")
							       || conStatus.equals("PublishToWarehouseStorageError"))){
							
				 	    	  logger.info("setting status to error:" +  bcpData.getName() + "status:" +  bcpData.getKey());
							  globalStatus.getStatus().put( bcpData.getKey(), "error");
						  } else {
							  globalStatus.getStatus().put( bcpData.getKey(), "success");
							  dbUtils.updateStatusInDB(bcpData, "PublishingToSubcube"); 
			 				  logger.info("setting status to success:" +  bcpData.getName() + "status:" +  bcpData.getKey());
						  }
				 		
				 	} else {
				 		  dbUtils.updateStatusInDB(bcpData,"PublishingToSubcube");
				 	}
				 	
				 	
				 	 excludeToSubCube(bcpData);
				 	 cleanUpQueue.put(bcpData);  
				     
			     }   
		      } catch(SQLException e){
		    	  logger.info("Catching error:" + e.getLocalizedMessage());
		    	
		    		  logger.info("No Retry");
		    	      
	    		  	if( bcpData.getVarType().equals("VAR_1D") ||  bcpData.getVarType().equals("VAR_10D") 
	    		  			||  bcpData.getVarType().equals("VAR_STRESS") ||  bcpData.getVarType().equals("VAR_1540") ){
						
	    		  		dbUtils.updateStatusInDB(bcpData, "PublishToWarehouseStorageError");
						logger.info("setting status to error:" +  bcpData.getName() + "status:" +  bcpData.getKey() + ",PublishToWarehouseStorageError");
						globalStatus.getStatus().put( bcpData.getKey(), "error");
					} 
		    		  
		    		  bcpData.setStatus("PublishingToWarehouseError");	//SP Error
		    		  dbUtils.updateStatusInDB(bcpData,null);
					  
					  bcpData.setStatus("failed");	//SP Error
					  cleanUpQueue.put(bcpData);
		    	
		      } catch(Exception e){
		    	  e.printStackTrace();
		    	     if( bcpData.getVarType().equals("VAR_1D") ||  bcpData.getVarType().equals("VAR_10D") 
		    	    		 ||  bcpData.getVarType().equals("VAR_STRESS") ||  bcpData.getVarType().equals("VAR_1540") ){
						
		    	    	dbUtils.updateStatusInDB(bcpData, "PublishToWarehouseStorageError");
						logger.info("setting status to error:" +  bcpData.getName() + "status:" +  bcpData.getKey() + ",PublishToWarehouseStorageError");
						globalStatus.getStatus().put( bcpData.getKey(), "error");
					 } 
		    		  logger.info("UnExpected Error:" + e.getLocalizedMessage());
		    		  bcpData.setStatus("PublishingToWarehouseError");	//SP Error
		    		  dbUtils.updateStatusInDB(bcpData,null);
					  
					  bcpData.setStatus("Failed");	//SP Error
					  logger.debug(">>>>>>>> PUTTING TO CLEANUP QUEUE Unexpected Error pushStagingToFactTable");
		    		  cleanUpQueue.put(bcpData);
		    
		      }
		      
		    }
	}
	
	private boolean handleHyponode(BcpData bcpData) {
		 logger.info( "handling hypo node:" + bcpData.getBatchId()   + " hNodeID:" + bcpData.gethUid() );
		
		String headerSql = " {call [Staging].[LoadRiskMeasureHeader]  (" + bcpData.gethUid() 
	 			  + "," + this.getCobDate(bcpData.getName()) +",'"+ "HYPO_NODE" + "') } ";
		
		String hypoRollBack = "{ call [DW].[usp_CleanupFactTablesVaR] (" + bcpData.getCobDate() + ",'"+ "HYPO" + "') }";
		
	  
		try {
			if( dbUtils.executeSp( headerSql ) ) {
				
				 String  stgToFctSql =  " {call [DW].[LoadFactHypoNodeLevel]  (" + bcpData.gethUid()  + ")}";
				 if(!dbUtils.executeSp( stgToFctSql )){
					 dbUtils.executeSp( hypoRollBack );
				 }
				 return true;
			} else {
				 dbUtils.executeSp( hypoRollBack );
			}
		} catch (SQLException e) {
			 try {
				dbUtils.executeSp( hypoRollBack );
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
				
			e.printStackTrace();
		}
		
		 
				 
		return false;
	}
	
	private void handleSuccess(BcpData bcpData,  String currentExecutionPhaseMsg ){
		
	     bcpData.setStatus("success");
	     logger.info( bcpData.totalExecutionTime("DONE:" + currentExecutionPhaseMsg, System.currentTimeMillis() ));
	     logger.debug("doing a cleanup");
	     
	}

	private void handleRetry(BcpData bcpData, String state, String stepError) throws InterruptedException{
		bcpData.setStart(System.currentTimeMillis());
		bcpData.setState( state );
		bcpData.setRetry(bcpData.getRetry() + 1);

		if (bcpData.getRetry() >= retryThreshold) {
			logger.info("Maximum number of retry reached: BatchID:"	+ bcpData.getName());
			bcpData.setStatus("failed");
			 bcpData.setStatus("PublishToWarehouseStorageError");	//SP Error
			 dbUtils.updateStatusInDB(bcpData, null);
			 logger.info("Putting to cleanup queue:"	+ bcpData.getName());
			 cleanUpQueue.put(bcpData);
		} else {
			logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Putting to done queue<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			doneQueue.put(bcpData);
		}
	}

	private String getHeaderSql(BcpData bcpData, String container) {
		
		String sql = null;
    	String pnlContainer = container;
    	
    	 if(bcpData.getName().indexOf("NON-VAR")!=-1 ) {
    		 
			 sql = " {call [Staging].[LoadRiskMeasureHeader]  (" + this.getUniqueId(bcpData.getName())
						  + "," + this.getCobDate(bcpData.getName()) +",'" +  container + "') } ";
		
    	 
    	 } else if(bcpData.getName().indexOf("PNL")!=-1){
			container = getVarContainer(bcpData.getName());
			logger.debug("PNL:" + bcpData.getName());
			
			if(container.equalsIgnoreCase("VAR_STRESS")){
				pnlContainer = "VAR_STRESS_AGG";
			}else if (container.equalsIgnoreCase("VAR_1540")){
				pnlContainer = "VAR_1540_AGG";
			}
			
			sql = " {call [Staging].[LoadRiskMeasureHeader]  (" + this.getUniqueId(bcpData.getName()) 
			  + "," + this.getCobDate(bcpData.getName()) +",'"+ container +"') } ";
    	
    	 } else if(bcpData.getName().indexOf("HYPO")!=-1){
 			container = getVarContainer(bcpData.getName());
 			logger.debug("HYPO:" + bcpData.getName());
 			
 			sql = " {call [Staging].[LoadRiskMeasureHeader]  (" + this.getUniqueId(bcpData.getName()) 
 			  + "," + this.getCobDate(bcpData.getName()) +",'"+ container +"') } ";
 		
			
		} else if( bcpData.getName().indexOf("VAR")!=-1){
		
			logger.debug( "VAR:" + bcpData.getName());
			logger.debug( "container:" + container);
			container = getVarContainer(bcpData.getName());
			
				if( container.equalsIgnoreCase("VAR_1D")){
					pnlContainer = "VAR_1D_AGG";
				} else if(container.equalsIgnoreCase("VAR_10D")){
					pnlContainer = "VAR_10D_AGG";
				} else if(container.equalsIgnoreCase("VAR_STRESS")){
					pnlContainer = "VAR_STRESS_AGG";
				} else if(container.equalsIgnoreCase("VAR_1540")){
					pnlContainer = "VAR_1540_AGG";
					
				} else if(container.equalsIgnoreCase("HYPO")){
					pnlContainer = "HYPO";
				} else if(container.equalsIgnoreCase("HYPO_1D")){
					pnlContainer = "HYPO";
				} 
			 sql = " {call [Staging].[LoadRiskMeasureHeader]  (" + this.getUniqueId(bcpData.getName()) 
			  + "," + this.getCobDate(bcpData.getName()) +",'"+ pnlContainer +"') } ";
		 
			 
		} 
 	   return sql;
	}
	
	public String stagingToFactSql(BcpData bcpData, StringBuilder headerDetails) {
		String sql = null;
		if(bcpData.getName().indexOf("NON-VAR")!=-1){
			 sql =  "{ call [DW].[LoadRiskMeasureNonVar] (" + this.getUniqueId(bcpData.getName()) + ") }"; 
		
		} else if( bcpData.getName().indexOf("HYPO_NODE")!=-1   ){
			 sql =  " {call [DW].[LoadFactVaR]  (" + this.getUniqueId(bcpData.getName()) + ")}";
		
		}else if(bcpData.getName().indexOf("HYPO")!=-1){
			 sql =  "{ call [DW].[LoadRiskMeasureNonVar] (" + this.getUniqueId(bcpData.getName()) + ") }"; 
				
		} else if(bcpData.getName().indexOf("PNL-VAR_1540")!=-1 ){
			
		     String sortedDates = this.sortRefDates( headerDetails.toString() );
			 sql =  "{ call [DW].[LoadRiskMeasureVaR1540PnLVector] (" + this.getUniqueId(bcpData.getName()) + ",'" + sortedDates + "') }"; 
	
		} else if(bcpData.getName().indexOf("PNL")!=-1 ){
			
			     String sortedDates = this.sortRefDates( headerDetails.toString() );
	 			 sql =  "{ call [DW].[LoadRiskMeasureVaRPnLVector] (" + this.getUniqueId(bcpData.getName()) + ",'" + sortedDates + "') }"; 
		} else if( bcpData.getName().indexOf("VAR")!=-1   ){
			 sql =  " {call [DW].[LoadFactVaR]  (" + this.getUniqueId(bcpData.getName()) + ")}";
 		}
		
	   return sql;
	}
	
	private String getUniqueId(String fname){
		fname = fname.replace(".APX", "");
		return fname.split("#")[5];
	}

	private String getCobDate(String fname){
		fname = fname.replace(".APX", "");
		return fname.split("#")[4];
	}
	
	private String getContainer(String fname){
		fname = fname.replace(".APX", "");
		return fname.split("#")[1];
	}

	private StringBuilder strArrToStringBuilder(String[] strArr, String delimeter){
		if(strArr!=null && strArr.length>0){
			StringBuilder ret = new StringBuilder(strArr[0]);
			
			for (int i = 1; i < strArr.length; i++) {
			  ret.append(delimeter);
			  ret.append(strArr[i]);
			}
			return ret;
		} else {
			return null;
		}
	}
		
	private String getVarContainer(String fname){
		//V1AL0#VAR AND P&L#VAR-VAR_1D#NULL#20121012#739222732547514.APX
		fname = fname.replace(".APX", "");
		
		String varName = fname.split("#")[2];
		if(varName.indexOf("-")!= -1  ){
			varName = fname.split("#")[2].split("-")[1];
		
		}else if(varName.indexOf("VAR_STRESS")!= -1 ){
			varName="VAR_STRESS";
		}else if(varName.indexOf("HYPO")!= -1 ){
			varName="HYPO";
		}
		return varName;
		
		
	}
	
	
	
	 
	
	private String getSignOffStatus(String containerName,int cobDate){
		
		String sql = " select status  " + 
				     " from DW.vw_SignOffAndExclude "+
				     " whate containerName = '" + containerName + "' and cobDate=" + cobDate; 
		try {
			List<Map<String,Object>> status = dbUtils.executeSql(sql);
			if(status!=null && status.size()>0){
			  return (String)status.get(0).get( "status");
			}
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			return null;
		}
		
		return null;
	}

	public static void main(String[] args){
		
		System.out.println("christopher|anabo|xxx".split("\\|")[0] );
		
		
		String fname = "B1AL0#VAR AND P&L#HYPO#1740026891198257#20130204#1740026891198257.APX";
		fname = "V1AL0#VAR AND P&L#PNL-VAR_1D#2031419687883300#20130131#2031419687883300.APX";
		
		
		String containerName = fname.split("#")[1];
		
		if(containerName.equalsIgnoreCase( "VAR AND P&L" ) ){
    		System.out.println("OK");
    	}
		
		fname = fname.replace(".APX", "");
		
		String varName = fname.split("#")[2];
		if(varName.indexOf("-")!= -1  ){
			varName = fname.split("#")[2].split("-")[1];
		}

	
		System.out.println(varName);
		
		
	}


  public String getSignOffStatus(String containerName,String cobDate){
		
		String sql = " select top 1 status  " + 
				     " from DW.vw_SignOffAndExclude "+
				     " where containerName = '" + containerName + "' and cobDate=" + cobDate; 
		try {
			List<Map<String,Object>> status = dbUtils.executeSql(sql);
			if(status!=null && status.size()>0){
			  return (String)status.get(0).get( "status" );
			}
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			return null;
		}
		
		return null;
	}


  private String sortRefDates( String headerDetails ){
	  String[] refDates= headerDetails.split("\\|");
	 TreeSet<Integer> dates = new TreeSet<Integer>();
	 for (int i = refDates.length-1;  i >= 0 ; i--) {
    		  dates.add( new Integer(refDates[i].trim() ));
	 }
    StringBuilder refDate = new StringBuilder();
			  for (Integer date : dates) {
				  refDate.append( date.toString()  ).append("|");
			}
	 return refDate.toString();
  }
  
  private void excludeToSubCube(BcpData bcpData){
	  
	   for (String exclude : dbUtils.getExcludeToSubCube()) {
		   if(exclude.equals(bcpData.getContainerName())){
			   bcpData.setStatus("success-no-insert");
			   break;
		   }
	   }
	  
  }
}
	
	


