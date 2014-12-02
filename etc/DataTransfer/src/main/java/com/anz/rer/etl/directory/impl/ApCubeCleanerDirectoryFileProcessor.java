package com.anz.rer.etl.directory.impl;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.anz.rer.etl.csvToTable.BcpConfig;
import com.anz.rer.etl.csvToTable.BcpData;
import com.anz.rer.etl.utils.FileUtils;

public class ApCubeCleanerDirectoryFileProcessor  extends ADirectoryWatcher {
 
	
	private String srcDirectory;
	private String filePattern;
	
	
	
	private Set<String> alreadyProcessedFiles = new HashSet<String>();
	

	private BlockingQueue<BcpData> containerToRemove;// = new LinkedBlockingQueue<BcpData>(5);
	
	
	protected ExecutorService fileProcessor;
	
	private Map<String,BcpConfig> bcpConfig = new HashMap<String,BcpConfig>();
	
	
	private final static Logger logger = Logger.getLogger(BcpCommandDirectoryFileProcessor.class);
	

	public ApCubeCleanerDirectoryFileProcessor(Properties props,  int noOfContToRemove, int containerToRemoveQueueSize ) {
		
		super(props.getProperty("etl.csvToTable.src.directory"),
			  props.getProperty("etl.csvToTable.src.fileName.pattern"));
		
		this.srcDirectory = props.getProperty("etl.csvToTable.src.directory");
		this.setFilePattern(props.getProperty("etl.csvToTable.src.fileName.pattern"));
	    containerToRemove = new LinkedBlockingQueue<BcpData>(containerToRemoveQueueSize);
		

	}

	public ApCubeCleanerDirectoryFileProcessor(String path, String filter) {
		super(path, filter);
		this.srcDirectory = path;
		this.setFilePattern(filter);
	}

	public ApCubeCleanerDirectoryFileProcessor(String path) {
		super(path);
		this.srcDirectory = path;
	}
	
	  
	
	
	
	@Override
	public void onChange(File file, String action) {
		try{
	 	if(!action.equals("delete")){
	
			if(alreadyProcessedFiles.contains(file.getName())){
				logger.info( String.format("File %s already processed", file.getName()));
				FileUtils.archiveFile(file.getAbsolutePath(), file.getParent()  + File.separator + "archive" + File.separator);
			 } else {
				 alreadyProcessedFiles.add(file.getName());
				 containerToRemove.put(    this.buildBcpData(file) );
			 }
		} else {
			logger.info("File was deleted or move to archive:" + file.getName());
		}
		}catch(Exception e){
			 e.printStackTrace();
		}
	}
	
	private BcpData buildBcpData(File csvFile){
			return new BcpData(  csvFile.getName().toString().toUpperCase(), csvFile.getAbsolutePath().toString() );
	}
	

 
	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}

	public String getFilePattern() {
		return filePattern;
	}

	public void initialize(){
		
		String scrDir = this.srcDirectory;
		String destDir = this.srcDirectory + File.separator + "archive" + File.separator;
		logger.info( "Source Directory:"      + scrDir);
		logger.info( "Destination Directory:" + destDir);
		this.run();
		
	}
	
	
	private boolean doCleanUp(){
		
		String oldUvrFileName = "";
		String newUvrFileName = "";
		String destDir = "";
		String sourceDir = "";
		
		File oldUvr = this.getOldUvr(destDir, oldUvrFileName);
		File newUvr = this.getNewUvr(sourceDir, newUvrFileName );
		
		this.renameOldUvr(oldUvr,oldUvr.getName()    + ".FORDELETE");
		
		this.renameNewUvr( newUvr , newUvr.getName() +  ".NEWUVR");

		 
		String newUvrTempFileName = sourceDir + "\\" + newUvr.getName() +  ".NEWUVR";
		
		this.moveNewUvr(  new File( newUvrTempFileName ), destDir + "\\" +  newUvr.getName() +  ".NEWUVR" ); // move new uvr to destination
		
		
		String newUvrGzFileName = destDir + "\\" + newUvr.getName() +  ".NEWUVR";
		this.renameNewUvr(  new File(newUvrGzFileName), newUvr.getName() + ".gz");
		
		this.removeOldUvr(new File( destDir + "\\" +  oldUvr.getName()    + ".FORDELETE"));
		return true;
		
	}
	
	
	private File getOldUvr(String dir, String oldUvrFileName){
		    //oldUvrFileName =   "*VSAL0_A.A_BGM_OPT_AU*.*.gz";
		    
			return FileUtils.getFile(dir, oldUvrFileName);	
	}
	
	private File getNewUvr(String dir, String newUvrFileName){
		return FileUtils.getFile(dir, newUvrFileName);	
	}
	
	private boolean renameOldUvr(File oldUvr, String newFileName){
		return	FileUtils.renameFile(oldUvr, newFileName);
	}

	private boolean renameNewUvr(File uvr, String newFileName){
		return	FileUtils.renameFile(uvr, newFileName);
	}
	
	private boolean moveNewUvr(File uvr, String destDir){
		return	FileUtils.moveFile( uvr,  destDir);
	}
	

	private boolean removeOldUvr(File oldUvrFile){
	  return	FileUtils.deleteQuietly(oldUvrFile);
	}
	
	
}