package com.anz.rer.etl.directory.impl;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.anz.rer.etl.directory.IFileProcessor;
import com.anz.rer.etl.utils.FileUtils;


public class DirectoryFileProcessor extends ADirectoryWatcher {
	
	private final static Logger logger = Logger.getLogger(DirectoryFileProcessor.class);
	
	IFileProcessor fileProcessor;

	private Set<String> alreadyProcessedFiles = new HashSet<String>();
	private String archiveDir;
	
	public DirectoryFileProcessor(Properties props) {
		super(props.getProperty("etl.csvToTable.src.directory"),
			  props.getProperty("etl.csvToTable.src.fileName.pattern"));
		    archiveDir =  props.getProperty("etl.csvToTable.src.fileName.archive",null);
	}

	public DirectoryFileProcessor(String path, String filter) {
		super(path, filter);
		
	}

	public DirectoryFileProcessor(String path) {
		super(path);
	}

	public void setFileProcessor(IFileProcessor fileProcessor) {
		this.fileProcessor = fileProcessor;
	}
	
	
	@Override
	public void onChange(File file, String action) {
	    
		
		if(!action.equals("delete")){
		    if(archiveDir==null){
		    	archiveDir = file.getParent()  + File.separator + "archive" + File.separator;
		    }
			logger.info("Processing:" + this.hashCode() + ":" + file.getName());
			 
			if(alreadyProcessedFiles.contains(file.getName())){
				logger.info( String.format("File %s already processed", file.getName()));
				FileUtils.archiveFile(file.getAbsolutePath(), archiveDir);
			 } else {
					alreadyProcessedFiles.add(file.getName());
					if( fileProcessor.validate(file)) {  
					
						if(fileProcessor.preProcess()) {
						  if(fileProcessor.doProcess()) {
							if( fileProcessor.postProcess()) {
								logger.info("File will be move archive:" + file.getName());
								FileUtils.archiveFile(file.getAbsolutePath(), archiveDir);
							} else {
								logger.info("Post Process failed....");
								logger.info("File will be move archive:" + file.getName());
								FileUtils.archiveFile(file.getAbsolutePath(), archiveDir);
							}
						  } else {
								FileUtils.archiveFile(file.getAbsolutePath(), archiveDir);
								logger.info("File will be move archive:" + file.getName());
						  }
						} else {
							logger.info("Pre Process failed....");
							FileUtils.archiveFile(file.getAbsolutePath(), archiveDir);
							logger.info("File will be move to archive:" + file.getName());
						}
					} else {
						logger.info("File validation failed....");
						alreadyProcessedFiles.remove(file.getName());
						getCheckedFiles().remove(file);
						//FileUtils.archiveFile(file.getAbsolutePath(), archiveDir);
					}
			}
		} else {
			//logger.info("File was deleted or move to archive:" + file.getName());
		}
	}
}
