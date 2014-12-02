package com.anz.rer.etl.polling;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.anz.rer.etl.polling.task.IDirectoryTask;

public  class DirectoryFilePolling  extends ADirectoryPolling{

	private final static Logger logger = Logger.getLogger(DirectoryFilePolling.class);
	private IDirectoryTask<Long> task;
    	
	public DirectoryFilePolling(int interval, long startDelay, String path,
			String filter,IDirectoryTask<Long>task) {
		super(interval, startDelay, path, filter);
		this.task = task;
	}
	
  
	@Override
	public void onChange(File file, String action) 
	{
	   if(action.equals("add")){
		task.execute(file, action);
	   }
	}

	public IDirectoryTask<Long> getTask() {
		return task;
	}  

	public void setTask(IDirectoryTask<Long> task) {
		this.task = task;
	}


	@Override
	public void setPath(String path) {
		// TODO Auto-generated method stub
		
	}
	
	
}
