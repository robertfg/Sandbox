package com.quartetfs.pivot.anz.datasource.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;

import com.quartetfs.pivot.anz.concurrent.ResizeableSemaphore;
import com.quartetfs.pivot.anz.datasource.factory.DataSourceServiceFactory;
import com.quartetfs.pivot.anz.datasource.factory.DataSourceServiceFactory.BeanName;
import com.quartetfs.pivot.anz.datasource.factory.NamedThreadFactory;
import com.quartetfs.pivot.anz.datasource.task.TransactionPublisherTask;
import com.quartetfs.pivot.anz.service.impl.PSRDetail;
import com.quartetfs.pivot.anz.service.impl.PSRDetail.PublisherType;
import com.quartetfs.pivot.anz.source.IRunParseData;
import com.quartetfs.pivot.anz.source.impl.BatchPSRData;
import com.quartetfs.pivot.anz.source.impl.PSRData;

public class PSRPublisherService {
	
	private static final Logger LOGGER = Logger.getLogger(PSRPublisherService.class.getSimpleName());
	
	
	private PSRPublisherConfigInfo configInfo;
	private PSRDetail psrDetails;
	private DataSourceServiceFactory factory;
	
	private ThreadPoolExecutor transactionExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(1, new NamedThreadFactory("Transaction"));	
	private ResizeableSemaphore transactionSemaphore;
	 
	private ExecutorService batchTranExecutor = Executors.newFixedThreadPool(1,new NamedThreadFactory("Transaction-Batch"));	
	private Lock batchLock = new ReentrantLock();
	private DelayQueue<BatchPSRData> delayQueue= new DelayQueue<BatchPSRData>();
	private ResizeableSemaphore batchSemaphore;
	private List<String> excludedPsrFromBatch = new ArrayList<String>();
	
	
	public PSRPublisherService(DataSourceServiceFactory factory) {
		super();
		this.factory=factory;
		this.configInfo = factory.getPublisherConfig(BeanName.PSRPublisherConfigInfo.name());
		this.psrDetails = factory.getPSRDetails(BeanName.psrDetail.name());
	}
	
	public void start()
	{
		initSemaphore();
		startBatchTransactionMonitor();
	}
	
	private void initSemaphore()
	{
		this.transactionSemaphore = new ResizeableSemaphore(configInfo.getTransactionQueueSize());
		this.batchSemaphore = new ResizeableSemaphore(configInfo.getBatchQueueSize());
	}
	
	private void startBatchTransactionMonitor()
	{
		batchTranExecutor.submit( new Callable<Void>() {
			@Override
			public Void call() throws Exception {
			while(true)
				{
					BatchPSRData data=delayQueue.take();
					if(data!=null)
					{
						LOGGER.info(String.format("Moving %s from batch Q to Transaction Q", data));
						batchSemaphore.release();
						data.lockForTran();
						submitForTransaction(data);
						data.unLockTran();					
					}
				}				
			}
		});
	}
	
	public void enqueue(PSRData data) throws InterruptedException
	{
		if(canBeBatched(data))
		{			
			createBatch(data);
		}
		else
		{
			submitForTransaction(data);
		}
	}

	private void submitForTransaction(IRunParseData data) throws InterruptedException {
		waitForSpace("transaction",transactionSemaphore,configInfo.getTransactionQueueSize());
		transactionExecutor.submit(new TransactionPublisherTask(data,transactionSemaphore,factory));
	}

	public void waitForSpace(String name,ResizeableSemaphore semaphore,int newSize) throws InterruptedException
	{
		LOGGER.info(String.format("Available space in %s Queue %s" , name,semaphore.availablePermits()));
		semaphore.acquire();
		semaphore.resizeIfRequired(newSize);
	}
	
	private void createBatch(PSRData data) throws InterruptedException 
	{
		
		PublisherType publisher = psrDetails.identifyPublisherType(data.getPSRName());
		Validate.notNull(publisher, String.format("Unable to find publisher for psr %s", data.getPSRName()));
		boolean found = batchTransaction(data);
		
		if(!found)
		{
			waitForSpace("batch",batchSemaphore,configInfo.getBatchQueueSize());			
			delayQueue.put( new BatchPSRData(configInfo,data.getFileName(),data,publisher));
		}
		
	}

	private boolean batchTransaction(PSRData data) {
		boolean found=false;
		try
		{
			this.batchLock.lock();			
			
			for(BatchPSRData psrData : delayQueue)
			{
				if(psrData.getPSRName().equals(data.getPSRName()))
				{
					if(psrData.appendDeals(data))
					{
						found=true;
						LOGGER.info(String.format("Rows %s added for batch transaction from %s file", data.getValueCount(),data.getFileName()));
						break;
					}					
				}
			}	
			
		}
		finally
		{
			batchLock.unlock();
		}
		return found;
	}
	
	private boolean canBeBatched(PSRData data)
	{
		for(String excludes: excludedPsrFromBatch ){
			 if(data.getFileName().contains(excludes)){
				 return false;
			 }
		}
		
		return data.getValueCount() <= configInfo.getBatchRowSize();
	}
	public int getTransactionQueueSize()
	{
		return  transactionExecutor.getQueue().size();		
	}
	
	public int getBatchTransactionQueueSize()
	{
		return  delayQueue.size();		
	}
	
	public DelayQueue<BatchPSRData> getDelayQueue() {
		return delayQueue;
	}
	
	public List<String> getExcludedPsrFromBatch() {
		return excludedPsrFromBatch;
	}

	public void setExcludedPsrFromBatch(List<String> excludedPsrFromBatch) {
		this.excludedPsrFromBatch = excludedPsrFromBatch;
	}
}
