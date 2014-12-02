package com.anz.rer.etl.polling.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.anz.rer.etl.dto.ApFile;
import com.anz.rer.etl.transform.TransposerTask;
import com.anz.rer.etl.utils.DbUtils;
import com.anz.rer.etl.utils.FileUtils;

public class ApSubCubeStatusUpdater  implements IDirectoryTask<Long> {

	private DbUtils dbUtils; 
	private Map<String,String> dbMapStatus;
	private Map<String,String> varContainerMapping;
	 
	private Set<String> alreadyUpdated = new TreeSet<String>();
	private final static Logger logger = Logger.getLogger(ApSubCubeStatusUpdater.class);
	private String mainCubeSrcDir;
	private String subCubeSrcDir;
	
	
	public ApSubCubeStatusUpdater(Map<String,String> dbMapStatus, Map<String,String> varContainerMapping, String mainCubeSrcDir, String subCubeSrcDir) {
		
		this.dbMapStatus = dbMapStatus;
		this.varContainerMapping = varContainerMapping;
	    this.mainCubeSrcDir = mainCubeSrcDir;
	    this.subCubeSrcDir = subCubeSrcDir;
	}  
	  
	
	/*@Override  
	public Long call() throws Exception {
		long start = System.currentTimeMillis();
		updateStatusInDB( new ApFile( apFileName, "#"),dbStatus );
	    return System.currentTimeMillis() - start;
	}*/
	
     
	private boolean updateStatusInDB(ApFile apFile){
		String containerName = apFile.getContainerName();
		
		String cobDate       = apFile.getCobDate();
        String status        = this.dbMapStatus.get(apFile.getFileExtension());
        		
			    try { 
			    	if(containerName.equalsIgnoreCase( "VAR AND P&L" )){
			    		containerName = varContainerMapping.get( apFile.getPsrCode() );
			    	}else if(containerName.equalsIgnoreCase( "VAR_STRESS" )){
			    		containerName = "VAR_STRESS_AGG";
			    	}else if(containerName.equalsIgnoreCase( "VAR_1540" )){
			    		containerName = "VAR_1540_AGG";
			    	}
			    	
			    	
			         
			    	if ( dbUtils.executeSp("{ call [DW].[UpdateSignoffAndExclude] (" +cobDate + ",'"+ 
			    	                 containerName + "','" + status+ "') }")){
			        }
			 	} catch (SQLException e) {
					e.printStackTrace();
				/*} catch (IOException e) {
					e.printStackTrace();*/
				} catch (Exception e){
					e.printStackTrace();
				}
		
		         return false;
	}
	
	public DbUtils getDbUtils() {
		return dbUtils;
	}

	public void setDbUtils(DbUtils dbUtils) {
		this.dbUtils = dbUtils;
	}

    private void movePnlData(File mainFinPnlFile) throws IOException{
    	    String timeStamp = String.valueOf(System.currentTimeMillis());
  		    logger.info("Copying MainCube FinancePNL File to SubCube");
			FileUtils.copyFile(mainFinPnlFile, new File(this.subCubeSrcDir + "\\Finance\\FIN01_" + timeStamp + ".csv_UVR.tmp"), true);
			FileUtils.renameFile(new File(this.subCubeSrcDir + "\\Finance\\FIN01_" + timeStamp + ".csv_UVR.tmp"), "FIN01_" + timeStamp + ".csv_UVR.gz");
			logger.info("Loading FinancePNL to SubCube started.");
		
    }
	@Override
	public boolean execute(File file, String action) {
		if(!alreadyUpdated.contains(  file.getName() ) && !file.getName().contains("FIN01") ){
			String oldFileName = file.getName();
			FileUtils.renameFile(file, oldFileName.toUpperCase().replace(".DONE", ".d").replace(".ERR",".e" ) );
			return updateStatusInDB( new ApFile( file.getName(), "#"));
			
		}
		alreadyUpdated.add(file.getName());
		return true;
	}

	public boolean removeFile(File file){
		//
		String CONTAINER = "CONTAINER=ACTUAL_PNL" +  "\n";
		String fileName  = "FinancePNL.DELETE";
	    String deleteDir = this.subCubeSrcDir;
	    
		FileWriter ryt = null;
		try {
			
			String removeFileName = deleteDir + "\\"  + fileName.toUpperCase().replace(".APX", ".DELETE");
			logger.info("creating remove file:" + removeFileName );
			ryt = new FileWriter( removeFileName );
			
			BufferedWriter out=new BufferedWriter(ryt);
			out.write(CONTAINER);
			out.close();
			
			fileName = fileName.toUpperCase().replace(".DELETE", ".remove" );
			
			FileUtils.renameFile(  new File(removeFileName) , fileName);
			logger.info("Droping remove file:" + fileName);
			if(startPolling( deleteDir ,  fileName )){
				logger.info("Remove File successfully executed");
				return true;
			}
			
			
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}
	
      private boolean startPolling( String dir, String fileName ) {
		
		
		String oldUvrFile =  "*" + fileName;// + ".*remove";
		
		if( FileUtils.getFile(dir, oldUvrFile + ".DONE")!=null || FileUtils.getFile(dir, oldUvrFile + ".EMT")!=null  ){
	      return true;
		} else {
		
		  try {
			Thread.sleep(5000);
		    startPolling(dir, fileName);
		  } catch (InterruptedException e) {
			e.printStackTrace();
		  }
		}
		return true;
	}
	
	
	public static void main(String[] args){
	
	File financePnlSubCube = 	FileUtils.getFile("C:\\subcube\\APData\\Finance", "*FIN01*.*csv_UVR.gz");
		System.out.println( financePnlSubCube.getAbsolutePath() );
		
		
		logger.info("Deleting Previous Subcube FinancePNL File ");
			File prevFinPnlSubCube = 	FileUtils.getFile( "C:\\subcube\\APData" + "\\Finance", "*FIN01*.*csv_UVR.gz");
			
			if (FileUtils.deleteQuietly(prevFinPnlSubCube)){
				logger.info("Deleting Previous Subcube FinancePNL File:SUCCESS" );
				
				File mainFinPnlFile  = 	FileUtils.getFile( "c:" + "\\Finance", "*FIN01*.*csv_UVR.gz");
	  		String timeStamp = String.valueOf(System.currentTimeMillis());
	  		logger.info("Copying MainCube FinancePNL File to SubCube");
  			try {
				FileUtils.copyFile(mainFinPnlFile, new File( "C:\\subcube\\APData"+ "\\Finance\\FIN01_" + timeStamp + ".csv_UVR.tmp"), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  			logger.info("Loading FinancePNL to SubCube");
  			FileUtils.renameFile(new File("C:\\subcube\\APData" + "\\Finance\\FIN01_" + timeStamp + ".csv_UVR.tmp"), "FIN01_" + timeStamp + ".csv_UVR.gz");
  			logger.info("Loading FinancePNL to SubCube started.");
    	  	
			}
		
		
	/*	Map<String,String> mapp = new HashMap<String,String>();
					mapp.put("done", "doneccc");
					mapp.put("DONE", "ccccdoneccc");
					
		ApFile a = new ApFile( "SECR0#EQ_CORRELATIONS#NON-VAR#1680476621846184#20121219#1680476621846184.APX.gz.tmp_UVR.gz.1356923722039.DONE", "#");
	   System.out.println( mapp.get( a.getFileExtension() ));	
	   System.out.println( mapp.size() );*/
	}
	
	
}
