package com.anz.rer.etl.vectorizer;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.anz.rer.etl.cache.LookUp;
import com.anz.rer.etl.directory.IFileProcessor;
import com.anz.rer.etl.directory.impl.ADirectoryWatcher;
import com.anz.rer.etl.transform.TransposeDirectoryProcessor;
import com.anz.rer.etl.utils.FileUtils;

public class VectorDirectoryProcessor extends ADirectoryWatcher {
	
	private final static Logger logger = Logger.getLogger(VectorDirectoryProcessor.class);
	
	IFileProcessor fileProcessor;

	private Set<String> alreadyProcessedFiles = new HashSet<String>();
	private String archiveDir;
	private int dateLoc;
	private String dateLocSeparator;
	
	private LookUp cache;
	private TransposeDirectoryProcessor transposeDirectoryProcessor;
	
	public VectorDirectoryProcessor(Properties props, LookUp cache,TransposeDirectoryProcessor transposeDirectoryProcessor  ) {
		    super(props.getProperty("etl.csvToTable.src.directory"),
			props.getProperty("etl.csvToTable.src.fileName.pattern"));
		    archiveDir = props.getProperty("etl.csvToTable.src.fileName.archive");
		   this.cache = cache;
		   this.transposeDirectoryProcessor = transposeDirectoryProcessor;
		   logger.info("VectorDirectoryProcessor Cache ID:" + cache.hashCode());
		   
	}

	public VectorDirectoryProcessor(String path, String filter) {
		super(path, filter);
		
	}

	public VectorDirectoryProcessor(String path) {
		super(path);
	}

	public void setFileProcessor(IFileProcessor fileProcessor) {
		this.fileProcessor = fileProcessor;
	}
	
	
	@Override
	public void onChange(File file, String action) {
	
		if(!action.equals("delete")){
			
		   logger.info("Processing:" + this.hashCode() + ":" + file.getName() );
		   String archiveLocation = getArchiveSubDir(archiveDir,file,dateLoc, dateLocSeparator);
		   
		   if(alreadyProcessedFiles.contains(file.getName())){
				//logger.info( String.format("File %s already put in queue:", file.getName()));
			 } else {
				 if(checkDateIfAlreadyInCache(file)){
				    logger.info("Processing:" + file.getName());
						 
					alreadyProcessedFiles.add(file.getName());
					
					if( fileProcessor.validate(file)) {  
						if(fileProcessor.preProcess()) {
						  if(fileProcessor.doProcess()) {
							if( fileProcessor.postProcess()) {
								logger.info("File will be move archive:" + archiveLocation + File.separator + file.getName());
								FileUtils.archiveFile( file.getAbsolutePath(),archiveLocation + File.separator);
							} else {
								logger.info("Post Process failed....");
								logger.info("File will be move archive:" + file.getName());
								FileUtils.archiveFile( file.getAbsolutePath(),archiveLocation + File.separator);
							}
						  } else {
								FileUtils.archiveFile( file.getAbsolutePath(),archiveLocation + File.separator);
								logger.info("File will be move archive:" + file.getName());
						  }
						} else {
							logger.info("Pre Process failed....");
							FileUtils.archiveFile( file.getAbsolutePath(),archiveLocation + File.separator);
							logger.info("File will be move archive:" + file.getName());
						}
					
					} else {
						logger.info("File validation failed....");
						alreadyProcessedFiles.remove(file.getName());
						getCheckedFiles().remove(file); 
					}
				 } else { 
					 logger.info("cache is not yet build"); 
					 transposeDirectoryProcessor.initializedCache(file);
				 }
			}
		} else {
			//logger.info("File was deleted or move to archive:" + file.getName());
		} 
	}
 
	
	private String getArchiveSubDir(String archiveDir,File file,int dateLoc, String dateLocSep ){
	    String fileName = file.getName().toString();
    	
		if(dateLoc > 0){
			String date = fileName.split(dateLocSep)[dateLoc];
			archiveDir += "//" + date.substring(6) + date.substring(4,6) + date.substring(0,4);   
	    }
		return archiveDir;
	}

	public int getDateLoc() {
		return dateLoc;
	}

	public void setDateLoc(int dateLoc) {
		this.dateLoc = dateLoc;
	}

	public String getDateLocSeparator() {
		return dateLocSeparator;
	}

	public void setDateLocSeparator(String dateLocSeparator) {
		this.dateLocSeparator = dateLocSeparator;
	}
	
     private boolean checkDateIfAlreadyInCache(File file){
		
    		if(cache!=null){
    			String cob = getCobDate(file.getName());
    			logger.info("Cached COB Date:" + cob);
    			if(cache.getCachedDate().containsKey(Integer.valueOf(cob))) { 
    				logger.info("Cached COB Date:" + cob + ",NOW contain..............................................");
    				
    				Boolean cachedDone = cache.getCachedDone().get(Integer.valueOf(cob)); 
    				logger.info("Cached COB Date IS DONE:" + cachedDone + "..............................................");
    				
    				if( cachedDone!=null && cachedDone.equals(Boolean.TRUE) ){
    					return true;
    				}else{
    					logger.info("Cached COB Date:" + cob + ",Is not yet DONE");
    				} 
    			}else {
    				logger.info("Cached COB Date:" + cob + ",Doesnt contain");
    				cache.getCachedDate().put(Integer.valueOf(cob), true);
    				logger.info("Cached COB Date:" + cob + ",Now PUT");
    				
    			}
    		} 
    		   logger.info("cache object is still false");
    			return false;
		
	}
	
	private  String getCobDate( String fileName   ){
	String[] name = fileName.split("#",-1);
		
		String date   = name[4]; 
		 return date;
	}

}
