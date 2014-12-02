package com.anz.rer.etl.directory.impl;

import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.anz.rer.etl.directory.IFileListener;

public class DirectoryWatcherImpl {
	
	private final static Logger logger = Logger.getLogger( DirectoryWatcherImpl.class );

	/*private IFileDispatcher fileDispatcher;*/
	private IFileListener dirWatcherImpl;
	

    private int interval; 
	
    public DirectoryWatcherImpl() {
	}
    
    public void run() {
		TimerTask task =  (TimerTask) dirWatcherImpl;
		Timer timer = new Timer();
		timer.schedule(task,   new Date(), interval); 
	}
   
	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public IFileListener getDirWatcherImpl() {
		return dirWatcherImpl;
	}

	public void setDirWatcherImpl(IFileListener dirWatcherImpl) {
		this.dirWatcherImpl = dirWatcherImpl;
	}
	
}