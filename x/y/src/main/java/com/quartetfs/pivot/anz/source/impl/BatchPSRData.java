package com.quartetfs.pivot.anz.source.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.quartetfs.pivot.anz.datasource.impl.PSRPublisherConfigInfo;
import com.quartetfs.pivot.anz.model.IVRParsingEntry;
import com.quartetfs.pivot.anz.model.impl.Deal;
import com.quartetfs.pivot.anz.service.impl.PSRDetail.PublisherType;
import com.quartetfs.pivot.anz.source.IRunParseData;

public class BatchPSRData implements IRunParseData,Delayed
{
	private PSRPublisherConfigInfo configInfo;	
	private PublisherType publisherType;
	
	private PSRData psrData;	
		
	private long startTime;
	private Set<String> fileNames = new HashSet<String>();	
	private volatile int recordCount=0;
	
	private Lock writeLoc = new ReentrantLock();
	
	public BatchPSRData(PSRPublisherConfigInfo configInfo,String fileName,PSRData psrData,PublisherType publisherType)
	{
		this.psrData=psrData;
		this.configInfo=configInfo;
		this.startTime = System.currentTimeMillis();
		this.fileNames.add(fileName);
		this.publisherType=publisherType;
		this.recordCount = psrData.getValueCount();
		
	}
	@Override
	public String getPSRName() {
		return psrData.getPSRName();
	}
	@Override
	public String getContainerName() {
		return psrData.getContainerName();
	}
	@Override
	public void contribute(List<IVRParsingEntry> list, int from, int toExcluded) {
		throw new UnsupportedOperationException();		
	}
	
	@Override
	public int getValueCount() {
		return psrData.getValueCount();
	}
	@Override
	public Collection<Deal> deals() {
		return psrData.deals();
	}
	
	//Batch transaction related stuff
	public boolean appendDeals(PSRData data)
	{
		if(writeLoc.tryLock())
		{
			try
			{
				fileNames.add(data.getFileName());
				psrData.mergedPSR().add(data.getPSRName());
				psrData.deals().addAll(data.deals());
				recordCount = psrData.getValueCount();
			}
			finally
			{
				writeLoc.unlock();				
			}
			return true;
		}
		return false;	
	}
	
	public void lockForTran() throws InterruptedException
	{
		writeLoc.lockInterruptibly();
	}
	
	public void unLockTran() throws InterruptedException
	{
		writeLoc.unlock();
	}
	
	@Override
	public int compareTo(Delayed o) {
		BatchPSRData data = (BatchPSRData)o;
		if ( this.startTime < data.startTime ) return -1;
		else if ( this.startTime > data.startTime ) return 1;
		else if ( this.startTime == data.startTime ) return 0;
		return 0;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		if(recordCount > configInfo.getMaxBatchRowSize()) return -1; // Expire based on size.
		
		long seconds = configInfo.getBatchExipreTimeInSecond() * 1000;
		return unit.convert((startTime +seconds) - System.currentTimeMillis() , TimeUnit.MILLISECONDS);
	}
	
	public Set<String> getFileNames() {
		return fileNames;
	}
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
		.append("PSR",mergedPSR())	
		.append("Size",recordCount)
		.append("Files",getFileNames()).toString();
	}
	@Override
	public Set<String> mergedPSR() {
		return psrData.mergedPSR();
	}
	
	public PublisherType getPublisherType() {
		return publisherType;
	}
	
	public int getRecordCount() {
		return recordCount;
	}
	
	
}
