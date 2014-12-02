package com.anz.rer.etl.csvToTable;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.anz.rer.etl.directory.impl.BcpDirectoryFileProcessor;
import com.anz.rer.etl.utils.FileUtils;

public class BcpCleaner implements Runnable {

	private String archiveDir;
	private String srcDir;
	private BlockingQueue<BcpData> cleanUpQueue;

	private final static Logger logger = Logger.getLogger(BcpCleaner.class);
	
	
	public BcpCleaner(String archiveDir, String srcDir,	BlockingQueue<BcpData> cleanUpQueue) {
		super();
		this.archiveDir = archiveDir;
		this.srcDir = srcDir;
		this.cleanUpQueue = cleanUpQueue;
	}

	@Override
	public void run() {
		while(true){
	   	    BcpData bcpData = null;
			try {
				bcpData = cleanUpQueue.take();
				logger.debug(">>>>>>>>>>>>>>>>>>Cleanup Queue Taking:" + bcpData.getName() + ":" + bcpData.hashCode());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			   if(null != bcpData){
				  logger.info("Going to do cleanup:" + bcpData.getName());
				  this.archiveData(bcpData);
			   }
			   
			}		
		
	}
	
	private boolean archiveData(BcpData bcpData){
		try{
			
			String srcFile = srcDir + File.separator + bcpData.getName();
			String archiveTempDir = archiveDir;
			logger.debug("Status:" + bcpData.getStatus());
			
			if(bcpData!=null && bcpData.getStatus()!=null && bcpData.getStatus().equalsIgnoreCase("failed")){
				archiveTempDir+=  File.separator + "db-error" + File.separator;
			}else if(bcpData!=null && bcpData.getStatus()!=null && bcpData.getStatus().equalsIgnoreCase("failed-bcp")){
				archiveTempDir+=  File.separator + "db-error" + File.separator;
						
			} else if(bcpData!=null && bcpData.getStatus()!=null && bcpData.getStatus().equalsIgnoreCase( "success-no-insert")){
				archiveTempDir+=  File.separator + "no-subcube" + File.separator ;
		
			} else if(bcpData!=null && bcpData.getStatus()!=null && bcpData.getStatus().equalsIgnoreCase( "no-data")){
				archiveTempDir+=  File.separator + "no-data" + File.separator ; 
			
			} else if(bcpData!=null && bcpData.getStatus()!=null && bcpData.getStatus().equalsIgnoreCase( "hypo-node")){
				archiveTempDir+=  File.separator + "hypo-node" + File.separator ; 
				
			} else {
				String[] split = bcpData.getName().split("#");
				if( split.length > 8){
					String exType = split[8];
					 logger.info("Extract Type:" + exType);
					 if(exType.equals("VAR_PNL_PORTFOLIO") || exType.equals("VAR_STRESS_PNL_PORTFOLIO") || exType.equals("HYPO_NODE") ){
						 archiveTempDir+=  File.separator + "portfolio" + File.separator ;
					 }
				}
			
				
				
			}
			
	        FileUtils.archiveFile( srcFile, archiveTempDir );
		}catch(Exception e){
			e.printStackTrace();
		}
        
        	
		return true;
	}
	
	 
       
}
