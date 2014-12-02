package com.quartetfs.pivot.anz.datasource.monitor;

import com.quartetfs.pivot.anz.datasource.factory.DataSourceServiceFactory;
import com.quartetfs.pivot.anz.datasource.factory.DataSourceServiceFactory.BeanName;
import com.quartetfs.pivot.anz.datasource.impl.PSRPublisherService;
import com.quartetfs.pivot.anz.source.impl.BatchPSRData;

public class TransactionMonitor {
	
	public DataSourceServiceFactory factory;
	
	public TransactionMonitor(DataSourceServiceFactory factory)
	{
		this.factory = factory;
	}
	
	public int getTransactionQueueSize()
	{
		PSRPublisherService service = factory.getPublisherService(BeanName.PSRPublisherService.name());
		return service.getTransactionQueueSize();		
	}
	
	public int getBatchTransactionQueueSize()
	{
		PSRPublisherService service = factory.getPublisherService(BeanName.PSRPublisherService.name());
		return  service.getBatchTransactionQueueSize();		
	}
	
	public String getEnqueuedPSR()
	{
		PSRPublisherService service = factory.getPublisherService(BeanName.PSRPublisherService.name());
		StringBuilder sb = new StringBuilder();
		sb.append("Publisher,PSR,Rows,Files").append("\n");
		for(BatchPSRData data : service.getDelayQueue())
		{
			sb.append(data.getPublisherType()).append(",")
			.append(data.mergedPSR()).append(",")
			.append(data.getRecordCount()).append(",")
			.append(data.getFileNames()).append("\n");			
		}
		return sb.toString();		
	}
}
