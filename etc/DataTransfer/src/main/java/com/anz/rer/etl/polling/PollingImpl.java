package com.anz.rer.etl.polling;

import java.util.Timer;
import java.util.TimerTask;

import com.anz.rer.etl.polling.task.ApSubCubeStatusUpdater;

public class PollingImpl {

	private IPollingTask pollingTask; 

	public void run() {
		TimerTask task = (TimerTask) pollingTask;
		Timer timer = new Timer();
		timer.schedule(task, pollingTask.getStartDelay(), pollingTask.getInterval());
	}

	public IPollingTask getPollingTask() {
		return pollingTask;
	}

	public void setPollingTask(IPollingTask pollingTask) {
		this.pollingTask = pollingTask;
	}

	public static void main(String[] args){
		PollingImpl polling = new PollingImpl();
		String filter = ".*.*.ERR|.*.*.DONE"; 
	
		IPollingTask pollingTask = new DirectoryFilePolling(10000, 1, "C:\\devs\\polling",
				filter,   new ApSubCubeStatusUpdater(null,null,null,null ) );  
		 
		polling.setPollingTask(pollingTask); 
		
		polling.run(); 
		
		  
		
	}
}
