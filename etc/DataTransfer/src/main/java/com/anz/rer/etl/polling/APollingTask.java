package com.anz.rer.etl.polling;

import java.util.TimerTask;
 
public abstract class APollingTask extends TimerTask implements IPollingTask {
	
	private int interval;
	private long startDelay;
	
	public APollingTask(int interval, long startDelay) {
		this.interval = interval;
		this.startDelay = startDelay;
	}
	
	
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public long getStartDelay() {
		return startDelay;
	}
	public void setStartDelay(long startDelay) {
		this.startDelay = startDelay;
	}
	 
		
	

}
