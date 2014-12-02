package com.anz.rer.etl.directory;

import java.io.File;

public interface IFileResolver {

	public void resolveSrcFileName();
	public String getSrcFileName();

	public void setSrcFileName(String fileName);
	public void setSrcConfigFileName(String fileName);

	public String getSrcConfigFileName();
	public boolean validateFileName(File fileName);
	
	public void setFile(File file);
	public File getFile();
	
	
	
}
