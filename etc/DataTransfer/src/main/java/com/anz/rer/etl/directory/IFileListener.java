package com.anz.rer.etl.directory;

import java.io.File;

public interface IFileListener {
	public  void onChange(File file, String action);
	public void setFilter(String filter);
	public void setPath(String path);
	
	
}
