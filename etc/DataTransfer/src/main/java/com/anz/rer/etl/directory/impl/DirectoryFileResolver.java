package com.anz.rer.etl.directory.impl;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.anz.rer.etl.directory.IFileResolver;
import com.anz.rer.etl.services.COBDateService;

public class DirectoryFileResolver implements IFileResolver{

	private final static Logger logger = Logger.getLogger(DirectoryFileResolver.class);
	
	private COBDateService cobDateService;
	private String srcFileName;
	private String srcConfigFileName;
	private Properties properties;
	private String inPattern;
	private String srcPattern; 
	private	String outPattern;
	private File file;
	
	//private boolean loadPreviousFile;
	
	
	
	public DirectoryFileResolver(Properties properties) {
		this.properties = properties;
	}

	@Override
	public void resolveSrcFileName() {
		 try{
		        cobDateService.loadDates( inPattern,srcPattern,outPattern );	
		        
		        String srcDir =  properties.getProperty("etl.csvToTable.src.directory");
		        String appendDate = properties.getProperty("etl.csvToTable.src.fileName.appendDate");
		        
		        if(appendDate.toLowerCase().equals("true")){
		        	srcFileName = srcDir + File.separator + properties.getProperty("etl.csvToTable.src.fileName") 
		        	+ cobDateService.deriveDate(cobDateService.getCobDate(),
		        	   properties.getProperty("etl.csvToTable.src.fileName.appendDate.format") )
		        	+ properties.getProperty("etl.csvToTable.src.fileName.pattern");
		        	 
		        } else {
		        
		        	srcFileName = srcDir + File.separator + properties.getProperty("etl.csvToTable.src.fileName")   
		        	+  properties.getProperty("etl.csvToTable.src.fileName.pattern");
		        
		        } 
	  	     
		        	srcConfigFileName = srcDir + File.separator +  properties.getProperty("etl.csvToTable.src.fileName.config");
		 	
		 } catch(ParseException pe){
			 pe.printStackTrace();
		 }
		 
		 
		
	}
	
	
	
	
	@Override
	public String getSrcFileName() {
		return srcFileName;
	}

	@Override
	public String getSrcConfigFileName() {
		return srcConfigFileName;
	}

	@Override
	public boolean validateFileName(File file) {
		logger.info("Processing file:" + file.getAbsolutePath());
		if(!file.getAbsolutePath().equals(srcFileName)){
			logger.info("Invalid filename, filename should be:" + srcFileName );
			return false;
		}
		this.file = file;
		return true;
	}

	private  String getFileName(String filePath, String fileName, String fileExt, String inPattern, String outPattern, Date date , boolean loadPreviousDateData){
   	 String retFileName = null;
   	 int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
   	
   	 retFileName =  "";//cobDateService.deriveDate(date, outPattern);
		
		
		File f1 = new File( filePath + File.separator + fileName + retFileName + fileExt );
		
	    if(f1.isFile()&&f1.exists()){
			return f1.getPath();
		} else {
			
			logger.warn("Daily Murex Postion extract files from Murex is not presented loading previous file: " + f1.getPath());
			if(loadPreviousDateData){
				retFileName = getFileName(filePath, fileName, fileExt, inPattern, outPattern,  new Date( date.getTime() - MILLIS_IN_DAY ),loadPreviousDateData );
			} else {
				retFileName = null;	
			}
		}
	  return  retFileName;
   }
	
	public COBDateService getCobDateService() {
		return cobDateService;
	}

	public void setCobDateService(COBDateService cobDateService) {
		this.cobDateService = cobDateService;
	}

	public String getInPattern() {
		return inPattern;
	}

	public void setInPattern(String inPattern) {
		this.inPattern = inPattern;
	}

	public String getSrcPattern() {
		return srcPattern;
	}

	public void setSrcPattern(String srcPattern) {
		this.srcPattern = srcPattern;
	}

	public String getOutPattern() {
		return outPattern;
	}

	public void setOutPattern(String outPattern) {
		this.outPattern = outPattern;
	}

	@Override
	public void setSrcFileName(String fileName) {
		this.srcFileName = fileName;
		
	}

	@Override
	public void setSrcConfigFileName(String fileName) {
		srcConfigFileName = fileName;
		
	}

	@Override
	public void setFile(File file) {
		this.file =file;
		
	}

	@Override
	public File getFile() {
		// TODO Auto-generated method stub
		return  file;
	}


}
