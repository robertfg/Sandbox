package com.quartetfs.pivot.anz.datasource.task;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import com.quartetfs.pivot.anz.datasource.factory.DataSourceServiceFactory;
import com.quartetfs.pivot.anz.datasource.factory.DataSourceServiceFactory.BeanName;
import com.quartetfs.pivot.anz.datasource.factory.PublisherFactory;
import com.quartetfs.pivot.anz.service.impl.PSRDetail;
import com.quartetfs.pivot.anz.service.impl.PSRDetail.PublisherType;
import com.quartetfs.pivot.anz.source.IRunParseData;
import com.quartetfs.pivot.anz.source.impl.BatchPSRData;
import com.quartetfs.pivot.anz.source.impl.PSRData;
import com.quartetfs.pivot.anz.source.publisher.IRunParseDataPublisher;
import com.quartetfs.pivot.anz.utils.ANZUtils;
import com.quartetfs.pivot.anz.utils.CubeEventKeeper;

public class TransactionPublisherTask  implements Callable<Void>  {

	private static final Logger LOGGER = Logger.getLogger(TransactionPublisherTask.class.getSimpleName());
	
	private CubeEventKeeper eventKeeper;
	private String statusFilePath;
	private PSRDetail psrDetails;
	private PublisherFactory publisher;
	
	protected IRunParseData runParseData;
	private Set<String> fileNames;
	private Semaphore transactionSemaphore;
	private String statusDirPath;
	
	
	public TransactionPublisherTask(IRunParseData runParseData,Semaphore transactionSemaphore,DataSourceServiceFactory factory) {
		
		init(factory);	
		this.transactionSemaphore=transactionSemaphore;
		this.runParseData = runParseData;
		extractFileNames();
	}

	private void extractFileNames() 
	{
		if(BatchPSRData.class.isInstance(runParseData))
		{
			fileNames = ((BatchPSRData)runParseData).getFileNames();
		}
		else
		{
			fileNames = new HashSet<String>();
			fileNames.add(((PSRData)runParseData).getFileName());
		}
	}

	private void init(DataSourceServiceFactory factory) {
		this.eventKeeper = factory.getCubeEventKeeper(BeanName.cubeEventKeeper.name());
		Properties prop=  factory.getFileTriggerProperties(BeanName.psrFileTriggerProperties.name());
		this.statusFilePath = new File(prop.getProperty("directoryToWatch")).getParent();
		this.psrDetails = factory.getPSRDetails(BeanName.psrDetail.name());
		this.publisher = factory.getPublisherFactory(BeanName.PublisherFactory.name());
		this.statusDirPath = prop.getProperty("statusDirectory");
	}

	private void commitEvent(boolean start)
	{
		for(String file : toFileNames())
		{
			 if(start)
			 { 
				 eventKeeper.fileComittStarted( new File(file).getName()); 
			 }
			 else 
			 { 
				 eventKeeper.fileComittCompleted(new File(file).getName()); 
			 }
		}
	}
	
	private void updateStatus(boolean error)
	{
		if(error && fileNames.size() > 1)
		{
			LOGGER.info(String.format("Multiple files will be marked for error, file details %s",toFileNames()));	
		}
		for(String file : fileNames)
		{
			ANZUtils.updateFileName( new File(statusFilePath) ,new File(file), error,statusDirPath);
		}
	}
	
	private Set<String> toFileNames()
	{
		Set<String> names = new HashSet<String>();
		for(String f : fileNames)
		{
			names.add( new File(f).getName());
		}
		return names;
	}
	
	
	@Override
	public Void call() throws Exception {
		
		try
		{
			commitEvent(true);
			try
			{
				PublisherType publisherType = psrDetails.identifyPublisherType(runParseData.getPSRName());
				long start = System.currentTimeMillis();
				IRunParseDataPublisher dataPublisher = publisher.create(publisherType);
				dataPublisher.publish(runParseData);
				LOGGER.info(String.format("Publish %s of PSR %s from file %s took %s ms", dataPublisher.getName(), runParseData.mergedPSR(),toFileNames(),(System.currentTimeMillis() - start)));
			}
			catch (Throwable e) 
			{
				updateStatus(true);
				throw new Exception(e);
			}				
			
			commitEvent(false);
			updateStatus(false);
		}
		finally
		{
			transactionSemaphore.release();
			LOGGER.info(String.format("Available permit for transaction Queue %s" , transactionSemaphore.availablePermits()));
		}
		return null;
	}
}
