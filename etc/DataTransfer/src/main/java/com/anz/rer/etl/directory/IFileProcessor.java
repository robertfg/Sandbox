package com.anz.rer.etl.directory;

import java.io.File;

public interface IFileProcessor {

	public boolean validate(File fileName);
	public boolean preProcess();
	public boolean doProcess();
	public boolean postProcess();
	
}
