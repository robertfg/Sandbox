package com.anz.rer.etl.polling;

public interface IPollingTask extends Runnable {

	public int getInterval();
	public long getStartDelay();
	
	
}
