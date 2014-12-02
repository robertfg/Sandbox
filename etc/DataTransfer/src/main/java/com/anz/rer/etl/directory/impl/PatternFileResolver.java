package com.anz.rer.etl.directory.impl;

import java.io.File;
import java.util.List; 
import java.util.Properties;

import com.anz.rer.etl.directory.IFileResolver;

public class PatternFileResolver  implements IFileResolver{

	private String srcFileName;
	private String srcConfigFileName;
	private Properties properties;
	private File file;
	
	public PatternFileResolver(Properties properties) {
		this.setProperties(properties);
	}
	
	@Override
	public void resolveSrcFileName() {
		
		
		
	}

	@Override
	public String getSrcFileName() {
		return srcFileName;
	}

	@Override
	public void setSrcFileName(String fileName) {
		// TODO Auto-generated method stub
		srcFileName = fileName;
	}

	@Override
	public String getSrcConfigFileName() {
		
		return srcConfigFileName;
	}

	@Override
	public void setSrcConfigFileName(String fileName) {
		srcConfigFileName = fileName;
	}

	@Override
	public boolean validateFileName(File fileName) {
		return false;
	}

	@Override
	public void setFile(File file) {
		this.file = file;
		
	}

	@Override
	public File getFile() {
		// TODO Auto-generated method stub
		return file;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getProperties() {
		return properties;
	}



}
