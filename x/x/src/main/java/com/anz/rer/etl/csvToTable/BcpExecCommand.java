package com.anz.rer.etl.csvToTable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.anz.rer.etl.utils.DbUtils;
import com.anz.rer.etl.utils.GlobalStatusUtil;

public class BcpExecCommand implements Runnable {

	private BcpData bcpData;
	private BlockingQueue<BcpData> doneQueue;
	private BcpConfig bcpConfig;
	private final static Logger logger = Logger.getLogger(BcpExecCommand.class);
	private DbUtils dbUtils;
	private GlobalStatusUtil globalStatus;
	private BlockingQueue<BcpData> cleanUpQueue;
	
	private static final Map<String,String> varContainerMapping = new HashMap<String,String>();	
	static {
			varContainerMapping.put("B1AL0", "HYPO");
			varContainerMapping.put("VXAL0", "VAR_10D_AGG");
			varContainerMapping.put("V1AL0", "VAR_1D_AGG");
			varContainerMapping.put("VSAL0", "VAR_STRESS_AGG");
			varContainerMapping.put("VFAL0", "VAR_1540_AGG");
			
	}
	
	
	public BcpExecCommand(BcpData bcpData,BcpConfig bcpConfig, BlockingQueue<BcpData> doneQueue, DbUtils dbUtils,GlobalStatusUtil globalStatus,BlockingQueue<BcpData> cleanUpQueue ) {
		this.bcpData = bcpData;
		this.doneQueue = doneQueue;
		this.bcpConfig = bcpConfig;
		this.dbUtils = dbUtils; 
		this.globalStatus = globalStatus;
		this.cleanUpQueue = cleanUpQueue;
	}
	

	
	public Long call() throws Exception {
	    logger.info("Publishing to Staging:" + "bcpData.getExtractType():" + bcpData.getExtractType() +  ",bcpData.getVarType():" + bcpData.getVarType() + "," + bcpData.getName());
		
	    if(!bcpData.getExtractType().equals("HYPO_NODE")){
			
			 /* apply to var confi and var pnl */
			 if( bcpData.getVarType().equals("VAR_1D") ||  bcpData.getVarType().equals("VAR_10D") 
	    	    		||  bcpData.getVarType().equals("VAR_STRESS") ||  bcpData.getVarType().equals("VAR_1540") ) { // any type of var
				 
				    String conStatus = dbUtils.getSignOffStatus( varContainerMapping.get(bcpData.getPsrCode()),  bcpData.getCobDate()  );
				    logger.info("checking if previous var file encounter exception:" + conStatus );
				    
				    if(conStatus!=null && (conStatus.equals("PublishingToStagingError")
						       || conStatus.equals("PublishingToWarehouseError")
						       || conStatus.equals("PublishToWarehouseStorageError"))){
						
			 	    	    dbUtils.updateStatusInDB(bcpData, conStatus); 
			 	    	    logger.info("Setting status to error, previous var encounter error:" +  bcpData.getName() + "status:" +  bcpData.getKey());
						    globalStatus.getStatus().put( bcpData.getKey(), "error");
						    bcpData.setStatus("failed-bcp");
							bcpData.setState(BcpConstants.HEADER);
							doneQueue.put(bcpData); 
							return 0l;
							
				    } else {
				    	dbUtils.updateStatusInDB(bcpData,"PublishingToStaging");
				    }
				    
				 
			 } else {
			      dbUtils.updateStatusInDB(bcpData,"PublishingToStaging");
			 }     
		}
		
		Object result = null;
		
		if(bcpData.getExtractType().equals("VAR_SIX_YEAR_PNL_PORTFOLIO")){
			result = execute1540(bcpData);
		}else {
			result = executeSqlCommand(buildCommand(bcpData));
		}
		
		if( result != null &&  Integer.valueOf( (Integer)result ) > 0) {
			if(!bcpData.getExtractType().equals("HYPO_NODE")){
				bcpData.setStart( System.currentTimeMillis() );
			    bcpData.setStatus("nothing");
			    bcpData.setState(BcpConstants.HEADER);
				dbUtils.updateStatusInDB(bcpData,"PublishedToStaging");
				doneQueue.put(bcpData); 
			} else {
				
				
				globalStatus.getStatus().put( bcpData.getKey(), "success"); // put hypo_node status to globalStatus 
				globalStatus.getStatus().put( "HYPO_NODE#UniqueIdE#" + bcpData.getCobDate(), bcpData.getBatchId());
				bcpData.setStatus("hypo-node");
				cleanUpQueue.put(bcpData);
			}			
			return 1l;
		} else {
			
			dbUtils.updateStatusInDB(bcpData,"PublishingToStagingError");
			
			if(bcpData.getVarType().equals("PNL-VAR") || bcpData.getVarType().equals("VAR_STRESS_PNL") 
					|| bcpData.getExtractType().equals("HYPO_NODE") || bcpData.getExtractType().equals("VAR_SIX_YEAR_PNL") ){
				globalStatus.getStatus().put( bcpData.getKey(), "error");
			} 
			
			bcpData.setStatus("failed-bcp");
			bcpData.setState(BcpConstants.HEADER);
			doneQueue.put(bcpData); 
			
			
		} 
		return 0l;
	
	
	}
	
	private String buildCommand(BcpData bcpData){
		
		     String containerName = bcpData.getContainerName();
		
			 String tableName =  bcpConfig.getTableName( bcpData.getContainerName()) ;
		     if(containerName.equals("VAR AND P&L")||containerName.equals("VAR_STRESS") ||containerName.equals("VAR_1540") ){
		    	 tableName     =    	 bcpConfig.getTableName( bcpData.getName().split("#")[2] );
		    	 containerName = bcpData.getName().split("#")[2];
		     }  
		
		     String cmd =       bcpConfig.getBcpUtilLoc()    +  " " +
                                bcpData.getBatchId()         +  " " + 
                                "\"" +    bcpData.getFilePath()  + "\""      + " " + 
		                        tableName    + " " + 
		                        bcpHeaderMapping( containerName );
		    
		    logger.info("executing command:" + cmd );
		    
		    return cmd;
		
	}
	
	private String bcpHeaderMapping(String containerName){
		logger.info("Getting Header:" + containerName);    	
		String header = bcpConfig.getBcpHeader( containerName);
		
		if(header==null){
			header = bcpConfig.getBcpHeader( "NON-VAR" );
		}
		
		logger.info("returning Header:" + containerName + "=" + header);    	
		 
		return header;
	}
	
	private Object execute1540(BcpData bcpData){
		
		 String  containerName = bcpData.getName().split("#")[2];
		 
	 
		 
	     String cmdLine = build1540Cmd(containerName + "_1");   
	    
	     Object result = executeSqlCommand(cmdLine);
	     logger.info("VAR_1540 executing command:" + cmdLine ); 
	     if( result != null &&  Integer.valueOf( (Integer)result ) > 0) {
	    	 cmdLine = build1540Cmd(containerName + "_2");
	    	 result = executeSqlCommand(cmdLine);
	     } else{
	    	 result = 0;
	     }	 
	     return result;
	    
	    
		
	}
	
	private String build1540Cmd(String containerName){
	
	     String tableName =  bcpConfig.getTableName( containerName ) ;
	
		 
	     String cmdLine =   bcpConfig.getBcpUtilLoc()    +  " " +
                            bcpData.getBatchId()         +  " " + 
                            "\"" +    bcpData.getFilePath()  + "\""      + " " + 
	                        tableName    + " " + 
	                        bcpHeaderMapping( containerName  );
	    
	     return cmdLine;
	}
	private Object executeSqlCommand(String cmdLine){
		try {
			Process p = Runtime.getRuntime().exec(cmdLine);
			p.waitFor();
			
			logger.info("p.exitValue():" + p.exitValue());
			int retValue = p.exitValue();
			p.destroy();
			return retValue; 
	
		} catch (IOException e1) {
				e1.printStackTrace();
		} catch (InterruptedException e2) {
				e2.printStackTrace();
		}
       
		return null;
	}
	
	public Object exec(String cmdLine){
		return this.executeSqlCommand(cmdLine);
	}
	
	public static void main(String[] args) {
		
		
		if (args.length < 4) {
			System.out.println("please input correct number of parameter" + " CopyUtilPath UniqueID APX_FilePath TargetTable HeaderColumnMapping");
			System.exit(1);

		} else {
	  
			String cmd = "";
			for (int i = 0; i < args.length; i++) {
				cmd+= args[i]+ " ";
			}
		  	 BcpExecCommand exec = new BcpExecCommand(null,null,null,null,null,null);
			  int retval =     (Integer)exec.exec(cmd);
			  System.out.println("Return Exit Value:" + retval);
			  
		}
		
		
		
	}


	@Override
	public void run() {
		try {
			call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
