/*
 * (C) Quartet FS 2007-2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.cxf.helpers.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.messaging.IFileListener;
import com.quartetfs.fwk.messaging.IFileWatcher;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.ANZUtils;
import com.quartetfs.pivot.anz.utils.CubeEventKeeper;

@ManagedResource
public class CleanerCoordinator  implements IFileListener {

	private CubeCleaner cubeCleaner;
	private CubeEventKeeper eventKeeper;	 
	
	private static Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private ConcurrentHashMap<String, ConcurrentHashMap<String, Date> > alreadyProcessedRemoveFiles;
	
	private int staleTime = 60;
	private TimerTask removalTaskChecker;
	private	Timer removalTaskTimer;
	private FastDateFormat dateFormat = FastDateFormat.getInstance( "MM/dd/yyyy HH:mm:ss.SSS" );;
	
	
	private boolean logEnable = false;
	private boolean apFileListener =  true;
	private boolean secondaryFileListener = true; 
	
	public CleanerCoordinator() {
		
		if(secondaryFileListener){
			this.initializeSecondaryFileListener();
		}
		
	}
	 
	 
	@Override
	public void onFileAction(IFileWatcher watcher, Collection<String> newFiles,
			Collection<String> modifiedFiles, Collection<String> deletedFiles) {
	
		if(apFileListener){
			this.logInfo("OnFileAction Start");
			//we focus only on modified and new files
			if (modifiedFiles != null && !modifiedFiles.isEmpty()){
				LOGGER.log(Level.INFO, MessagesANZ.MODIFIED_FILES, modifiedFiles);
				for(String fileName : modifiedFiles) {
					if(fileName!=null){
						processReceivedFile(fileName);
					} else {
						LOGGER.info("Some null mod files");
					}
				}
			} else if (newFiles != null && !newFiles.isEmpty()) {
				LOGGER.log(Level.INFO, MessagesANZ.NEW_FILES + newFiles);
				for(String fileName : newFiles) {
					if(fileName!=null){
					 processReceivedFile(fileName);
					}else {
						LOGGER.info("Some null new files");
					}
				}
				
			} 
			this.logInfo("OnFileAction End");
		}
	}

	private void processReceivedFile(String fileName) {
		try{
		
		
		if(removeFileAlreadyStarted(fileName) && removeFileAlreadyDONE(fileName)){
			LOGGER.info("The " + fileName + " was already processed.");
			cubeCleaner.generateFile(fileName, false, -2);
			return;
		} else if (removeFileAlreadyStarted(fileName)) {
			LOGGER.info("The " + fileName + " was already processing.");
			return;
		}
		
		logInfo("Processing file for deletion:" + fileName);
		this.updateRemovalFileState(fileName, "START", new Date() );
			
		Properties props = loadPropsFile(fileName);		
		if (props == null || props.isEmpty()){
			LOGGER.log(Level.SEVERE, MessagesANZ.NO_PROPS, fileName );
			cubeCleaner.generateFile(fileName, false, -1);
			this.updateRemovalFileState(fileName, "DONE", new Date() );
			return;
		}		
		
		
		String containerName = props.getProperty("CONTAINER",null);
		IDate cobDate = null; 
		String cobDateStr   = props.getProperty(ANZConstants.COB);
		String rebuild      =  props.getProperty(ANZConstants.REBUILD_PROPS,null);
		String removeLimits = props.getProperty("removeLimits",null);
		String oldRebuild   = props.getProperty("rebuild",null);
		
		cobDate = retrieveDate(cobDateStr);
		
		String condition=String.format("File %s Parameters %s",fileName,props);
	
		LOGGER.info("Processing file for deletion:" + fileName + ",condition:" + condition);
		
				if (cobDate ==null && (containerName!=null && containerName.equals("ACTUAL_PNL")) ){ //actual pnl removal
						cubeCleaner.removeByCondition(fileName,props,alreadyProcessedRemoveFiles);
			
				} else if ((cobDate ==null && containerName==null && removeLimits==null) &&
						(  (rebuild!=null && rebuild.equalsIgnoreCase("true"))  
							|| (oldRebuild!=null &&  oldRebuild.equalsIgnoreCase("true")) ) ){ //rebuild only
						cubeCleaner.rebuildIndex(fileName,alreadyProcessedRemoveFiles);
				
				} else if ((cobDate !=null && containerName==null 
						&& (oldRebuild!=null || rebuild!=null)) && ( removeLimits!=null ) ){ //daily removal with rebuild and limits removal
						cubeCleaner.removeByCondition(cobDate,fileName,props, alreadyProcessedRemoveFiles);
			
				} else if ((cobDate !=null && containerName==null && rebuild==null && oldRebuild == null )
						&& ( removeLimits!=null && removeLimits.equalsIgnoreCase("true") ) ){ //removelimits with date
				    	cubeCleaner.removeLimitDetailsByDate(cobDate,fileName, alreadyProcessedRemoveFiles);
			
				} else if(cobDate ==null) { 
						LOGGER.log(Level.SEVERE, MessagesANZ.NOT_VALID_DATE, cobDateStr);
						cubeCleaner.generateFile(fileName, false, -1);
						
				} else {
					cubeCleaner.removeByCondition(cobDate,fileName,props,alreadyProcessedRemoveFiles);
				}
				
			this.updateRemovalFileState(fileName, "DONE", new Date() );
			
		}catch(Exception e){
			LOGGER.info(e.getMessage());
		}
		
	}

	private IDate retrieveDate(String dateStr){
		try{
			int yyyy = Integer.parseInt(dateStr.substring(0,4));
			int mm = Integer.parseInt(dateStr.substring(4,6));
			int dd = Integer.parseInt(dateStr.substring(6,8));
			return new  com.quartetfs.biz.types.impl.Date(yyyy, mm, dd, 0, 0, 0);
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, MessagesANZ.NOT_VALID_DATE, dateStr);
			return null;
		}
	}
	
	private Properties loadPropsFile(String fileName){
		Properties props = null;
		FileInputStream fis=null;
		try{
			props=new Properties();
			fis = new FileInputStream(new File(fileName));
			props.load(fis);
		}catch(Exception e){
			String msg = ANZUtils.formatMessage(MessagesANZ.PROP_LOADING_ERR, fileName);
			LOGGER.log(Level.SEVERE, msg, e);
		}finally{
			if (null != fis)
				try {
					fis.close();
				} catch (IOException e) {
					String msg = ANZUtils.formatMessage(MessagesANZ.PROP_CLOSING_ERR, fileName);
					LOGGER.log(Level.SEVERE, msg, e);
				}
		}
		return props;
	}

	public void setCubeCleaner(CubeCleaner cubeCleaner) {
		this.cubeCleaner = cubeCleaner;
	}
	
	public void setEventKeeper(CubeEventKeeper eventKeeper) {
		this.eventKeeper = eventKeeper;
	}

	private List<File> getRemoveFiles(String directory){
		List<File> removeFiles = FileUtils.getFiles( new File(directory ), ".*remove");
		return removeFiles;
	}

	private void checkIfRemoveFilesNeedToReprocess(String directory){
		   logInfo("Checking remove file in directory:" + directory);
		    
			List<File> removeFiles = getRemoveFiles( directory ); 
			
 			if(removeFiles!=null && !removeFiles.isEmpty()){
 			
 				for (File file : removeFiles) {
 					
					 
					 Map<String,Date> state = getRemoveFileState(file.getAbsolutePath());
					
					 if( state == null) {
						logInfo("The removed file will now be process");
						 this.processReceivedFile( file.getAbsolutePath() );
						 
					 } else {
						       if( state.containsKey("START") && !state.containsKey("DONE")){
					        	   //already started, check if its processing too long
					        	   int procTime = this.getTimeDiff(new Date(), state.get("START"));
						           
					        	   if(state.containsKey("LAST_EXEC_TIME"))	{
						        	   procTime = this.getTimeDiff(new Date(), state.get("LAST_EXEC_TIME"));
						           } 
					        	   	   if(procTime >= staleTime) {
						        		   logInfo("Remove files was staled for more than:" + staleTime + " mins. The removal task handler will reprocessed it again");
						        		   alreadyProcessedRemoveFiles.remove( file.getAbsolutePath());
						        		   processReceivedFile(file.getAbsolutePath());
						        		   
					        	   	   } else {
						        		   if(state.containsKey("LAST_EXEC_TIME")){
						        			  logInfo("Remove files last removal execution:" + getLastremovalExecutionTime(state.get("LAST_EXEC_TIME")) );
						        		   }
						        	   }
					           } else if(state.containsKey("DONE")) {
					        	       LOGGER.info("The remove file was already processed and No file status was created, \n "
					        	       		+  " this can be the case of manual file renaming with the same file name used, \n"
					        	       		+  " the removal handler will processed this, and this might end up as already processed file. \n"
					        	       		+  " Note:\n"
					        	       		+  " This is a potential gap if this file was a legit file, the developer should check why it fails to create status file \n"
					        	       		+  " The module can also check if the internals of the file is legit and should check the data in the cube if there are still data need to be removed \n"
					        	       		+  " If there are still data that needs to be removed then the removal module should force to processed this file. \n"
					        	       		+  " * The module is not doing this at the moment\n"
					        	       		+  " Hope this log can give you breif idea of what is going on.\n"
					        	       		);
					        	       processReceivedFile(file.getAbsolutePath());
					        	       
					           }
					 }
				}
 			} else {
 				logInfo("No Remove files left unprocess");
 			}
	}
	
	private String getLastremovalExecutionTime(Date lastExecTime){
		return this.dateFormat.format(lastExecTime);
	}
	
	private synchronized void  updateRemovalFileState(String fileName,String status, Date dateTime){
		
		logInfo("Setting " + fileName + " state to:" + status);
		try{
			ConcurrentHashMap<String,Date> state =getRemoveFileState( fileName);
			if( state == null ){
				logInfo("State is nulll for" + fileName);
				 ConcurrentHashMap initState =  new ConcurrentHashMap();
				 				   initState.put(status, dateTime);
				 
				alreadyProcessedRemoveFiles.put(fileName, initState);
				 logInfo("alreadyProcessedRemoveFiles:" + alreadyProcessedRemoveFiles.size()); 	
			} else {
				logInfo("State is not nulll for" + fileName);
				state.put(status, dateTime);
				alreadyProcessedRemoveFiles.put(fileName,  state);
				logInfo("alreadyProcessedRemoveFiles:" + alreadyProcessedRemoveFiles.size());
			}
		}catch(Exception e){
			LOGGER.info(e.getLocalizedMessage());
		}
		logInfo("Remove file " + fileName + " set state to:" + status + ",alreadyProcessedRemoveFiles:" + alreadyProcessedRemoveFiles);
		
	}
	
	private int getTimeDiff(Date current, Date start ){
		 	DateTime dt1 = new DateTime(current);
			DateTime dt2 = new DateTime(start);
			return Minutes.minutesBetween(dt1, dt2).getMinutes() % 60;
	}
	
	private ConcurrentHashMap<String, Date> getRemoveFileState(String fileName){
		
		 logInfo( "Getting logs for:" + fileName  + "," + alreadyProcessedRemoveFiles.size());
		
		 return alreadyProcessedRemoveFiles.get(fileName);
	}
	
	
	public int getStaleTime() {
		return staleTime;
	}

	public void setStaleTime(int staleTime) {
		this.staleTime = staleTime;
	}
	
	public void initializeSecondaryFileListener() {
		alreadyProcessedRemoveFiles = new ConcurrentHashMap<String,ConcurrentHashMap<String,Date>>();
		removalTaskChecker = new RemovalFileChecker();
		removalTaskTimer = new Timer();
		removalTaskTimer.scheduleAtFixedRate(removalTaskChecker,60000, 20000);
		logInfo("Secondary File Listener is now Initialized");
	}
	
	private synchronized boolean removeFileAlreadyStarted( String removeFile ){
		 Map<String,Date> state = getRemoveFileState(removeFile);
		 if( state == null) {
			 return false;
		 }	else {
			 return true;
		 } 
	}
	
	private synchronized boolean removeFileAlreadyDONE( String removeFile ){
		 Map<String,Date> state = getRemoveFileState(removeFile);
		 
		
		 if( state == null) {
			 return false;
		 }	else {
			 if(state.containsKey("DONE")){
				 return true;
			 }
		 }
		 
		 return false;
	}
	
	public void restartSecondaryFileListener(){
		this.initializeSecondaryFileListener();
		LOGGER.info("Secondary file listener was restarted successfully");
	}
	
	private class RemovalFileChecker extends TimerTask {
		@Override
		public void run() {
			checkIfRemoveFilesNeedToReprocess(cubeCleaner.getDirectoryToWatch());
		}
		
	}
	
	private void logInfo(String msg){
		if(logEnable){
		  LOGGER.info(msg);
		}
	}
	
	public void setLoggingON(){
		logEnable = true;
		LOGGER.info("Set logging to TRUE");
	}
	
	public void setLoggingOFF(){
		logEnable = false;
		LOGGER.info("Set logging to FALSE");
	}
	
	public String getRemovalStatusLogs(String fileName){
		
		LOGGER.info("Retrieving logs:" + fileName);
		ConcurrentHashMap<String,Date> state = getRemoveFileState( fileName);
		StringBuilder removalLogs = new StringBuilder();
		
		if( state != null ){
			for (Map.Entry< String,Date > removalState : state.entrySet()) {
				removalLogs.append( removalState.getKey() ).append(" : ").append( getLastremovalExecutionTime(removalState.getValue())).append("\n");
			}
			return removalLogs.toString();
		}else {
			return "FILE WAS NOT PROCESS";
		}
	}
	
	public void getRemovalActivity(){
		
		if(	alreadyProcessedRemoveFiles !=null && !alreadyProcessedRemoveFiles.isEmpty()){
			StringBuilder removalLogs = new StringBuilder();	
			for (Map.Entry<String, ConcurrentHashMap<String,Date> > removalActivity: alreadyProcessedRemoveFiles.entrySet() ) {
				
				removalLogs.append("Remove File:" + removalActivity.getKey()).append("\n");
				
				for (Map.Entry< String,Date > removalState : removalActivity.getValue().entrySet()) {
					 removalLogs.append( removalState.getKey() ).append(" : ").append( getLastremovalExecutionTime(removalState.getValue())  ).append("\n");
				}
				removalLogs.append("========================================================\n");
			}
			
			LOGGER.info(removalLogs.toString());
		}else {
			LOGGER.info("Nothing on the removal event logs");
		}
	}

	public void enableApFileListener(){
		apFileListener = true;
		LOGGER.info("APFileListener is now enabled");
	}
	
	public void disAbleApFileListener(){
		apFileListener = false;
		LOGGER.info("APFileListener is now disabled");
	}
	
	public void enableSecondaryFileListener(){
		secondaryFileListener = true;
		LOGGER.info("Secondary FileListener is now Enabled");
	}
	
	public void disAbleSecondaryFileListener(){
		secondaryFileListener = false;
		removalTaskChecker = null;
		LOGGER.info("Secondary FileListener is now Disabled");
	}
	
	public static void main(String[] args){
		
		File f  = new File("C:\\devs\\XSpace\\estatement_20140620_5400126603461029_SGD.pdf");
		
		System.out.println(f.getAbsolutePath());
	}
}
