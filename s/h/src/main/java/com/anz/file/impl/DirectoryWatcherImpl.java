package com.anz.file.impl;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.anz.file.ADirWatcher;
import com.anz.parser.IFileDispatcher;
import com.anz.task.Tasklet;

public class DirectoryWatcherImpl implements Tasklet{
	
	IFileDispatcher fileDispatcher;
	String strDir = "C:/devs/murex_files";
	String strFilePattern = "csv";

	public IFileDispatcher getFileDispatcher() {
		return fileDispatcher;  
	}

	public void setFileDispatcher(IFileDispatcher fileDispatcher) {
		this.fileDispatcher = fileDispatcher;
	}


	@Override
	public void run() {
		TimerTask task = new ADirWatcher(strDir, strFilePattern) {
			protected void onChange(File file, String action) {
				fileDispatcher.dispatchFile(file.getName());
			}
		};
		Timer timer = new Timer();
		timer.schedule(task, new Date(), 1000);
		
	}

	@Override
	public String taskName() {
		// TODO Auto-generated method stub
		return null;
	}


	
	
}