package com.quartetfs.pivot.anz.service.export;

public class ExportTaskThreadInfo {

	private long totalTime;
	private long threadId;
	
	
	public ExportTaskThreadInfo(long totalTime, long threadId) {
		super();
		this.totalTime = totalTime;
		this.setThreadId(threadId);
	}
	
	public long getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}


	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}
	
	
}
