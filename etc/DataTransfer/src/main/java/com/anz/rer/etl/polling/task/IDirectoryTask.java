package com.anz.rer.etl.polling.task;

import java.io.File;
import java.util.concurrent.Callable;

public interface IDirectoryTask<T>  {
   
	public boolean execute(File file, String action);
   
}
