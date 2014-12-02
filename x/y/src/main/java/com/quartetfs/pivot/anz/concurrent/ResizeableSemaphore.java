package com.quartetfs.pivot.anz.concurrent;

import java.util.concurrent.Semaphore;

public class ResizeableSemaphore extends Semaphore
{

	private static final long serialVersionUID = 1L;
	private int permit;
	public ResizeableSemaphore(int permit) {
		super(permit);
		this.permit=permit;
	}		
	
	public synchronized void resizeIfRequired(int newPermit)
	{
		int delta = newPermit - permit;
		if(delta==0) return;
		if(delta > permit) this.release(delta); // this will increase capacity
		if(delta < 0) this.reducePermits(Math.abs(delta));
		this.permit=newPermit;
	}
	
}
