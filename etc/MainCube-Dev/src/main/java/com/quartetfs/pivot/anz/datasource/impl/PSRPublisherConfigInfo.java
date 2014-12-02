package com.quartetfs.pivot.anz.datasource.impl;

public class PSRPublisherConfigInfo {
	
	private volatile int transactionQueueSize;
	private volatile int batchQueueSize;
	private volatile long batchExipreTimeInSecond;
	private volatile long batchRowSize;
	private volatile long maxBatchRowSize;
	
	public PSRPublisherConfigInfo(int transactionQueueSize, long batchExipreTimeInSecond,long batchRowSize,int batchQueueSize,long maxBatchRowSize) {
		super();
		this.transactionQueueSize = transactionQueueSize;
		this.batchExipreTimeInSecond = batchExipreTimeInSecond;
		this.batchRowSize = batchRowSize;
		this.batchQueueSize = batchQueueSize;
		this.maxBatchRowSize=maxBatchRowSize;
	}
	
	public long getBatchExipreTimeInSecond() {
		return batchExipreTimeInSecond;
	}
	
	public void setBatchExipreTimeInSecond(long batchExipreTimeInSecond) {
		this.batchExipreTimeInSecond = batchExipreTimeInSecond;
	}
	
	
	public int getTransactionQueueSize() {
		return transactionQueueSize;
	}
	
	public void setTransactionQueueSize(int transactionQueueSize) {
		this.transactionQueueSize = transactionQueueSize;
	}
	
	public void setBatchRowSize(long batchRowSize) {
		this.batchRowSize = batchRowSize;
	}
	
	public long getBatchRowSize() {
		return batchRowSize;
	}
	
	public int getBatchQueueSize() {
		return batchQueueSize;
	}
	
	public void setBatchQueueSize(int batchQueueSize) {
		this.batchQueueSize = batchQueueSize;
	}
	
	public long getMaxBatchRowSize() {
		return maxBatchRowSize;
	}
	
	public void setMaxBatchRowSize(long maxBatchRowSize) {
		this.maxBatchRowSize = maxBatchRowSize;
	}
	
}
