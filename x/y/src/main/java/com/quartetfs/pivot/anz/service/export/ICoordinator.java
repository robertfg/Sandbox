package com.quartetfs.pivot.anz.service.export;

public interface ICoordinator {

	public Long getId();
	
	public Integer getCount();	
	
	public void incrementCount(int count);
	
	public Integer getTotalCount();
	
	public boolean isDone();
	
}
