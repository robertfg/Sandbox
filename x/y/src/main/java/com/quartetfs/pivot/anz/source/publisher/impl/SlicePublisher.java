/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.source.publisher.impl;


import static com.quartetfs.pivot.anz.utils.TransactionUtils.rollback;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.util.StopWatch;

import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.IActivePivotSchema;
import com.quartetfs.fwk.transaction.TransactionException;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.model.impl.Deal;
import com.quartetfs.pivot.anz.service.impl.PSRDetail;
import com.quartetfs.pivot.anz.service.impl.PSRDetail.PublisherType;
import com.quartetfs.pivot.anz.source.IRunParseData;
import com.quartetfs.pivot.anz.source.publisher.IRunParseDataPublisher;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.KeyGenerator;


/**
 * <b>SlicePublisher<b/><br/>
 * Publish the data into a specific cube using a transaction wrapper.<br/>
 * The <i>keyExtractor</i> attribute will create the key used to link the object
 * put into the cube.
 * 
 * @author Quartet Financial Systems
 */
public class SlicePublisher implements IRunParseDataPublisher {
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);

	private String name;
	//private AtomicLong counter=new AtomicLong(Integer.MAX_VALUE+1);
	
	private KeyGenerator counter;
	
	private IActivePivotSchema activePivotSchema;
	private PSRDetail psrDetail;
	

	public void setName(final String name) {
		this.name = name;
	}


	@Override
	public String getName() {
		return getClass().getSimpleName() + " - " + name;
	}

	/**
	 * Publish the data from the <i>runParseData</i> object into the cube
	 * 
	 * @param runParseData
	 * @return true if the data should be published or false otherwise
	 * @throws Throwable 
	 */
	@Override
	public boolean publish(final IRunParseData runParseData) throws Throwable{
  
		try {
			//publish phase
			// start transaction should be done first because of a transactionManager limitation 
			activePivotSchema.getTransactionManager().startTransaction();
			StopWatch stop = new StopWatch(runParseData.mergedPSR().toString());
			
			publish(runParseData, stop);
			commit(runParseData, stop);
			
			LOGGER.info(String.format("Publish Result %s", stop.toString()));
		} 
		catch (Throwable th) 
		{
			LOGGER.log(Level.SEVERE,MessagesANZ.ERROR_ADDING_DATA,th);
			rollback(activePivotSchema.getTransactionManager());
			throw th;
		}
		return true;
	}


	private void commit(final IRunParseData runParseData, StopWatch stop) throws TransactionException 
	{
		stop.start("Commit");
		LOGGER.log(Level.INFO,MessagesANZ.START_COMMIT, runParseData.getPSRName());
		activePivotSchema.getTransactionManager().commit();
		stop.stop();
	}


	private void publish(final IRunParseData runParseData, StopWatch stop) throws Exception 
	{
		stop.start("Publish");
		LOGGER.log(Level.INFO,MessagesANZ.START_PUBLISH, runParseData.getPSRName());
		for (Deal deal:runParseData.deals()){
			Object factToInsert[]=getTemplate(deal);
			factToInsert[ANZConstants.FACT_DEAL_INDEX] = deal;
			
			submitToSchema(counter.getNextId(),factToInsert,deal.getPsrName());
		}
		stop.stop();
	}

	
	private Object[] getTemplate(Deal deal)
	{
		final Object[] template = new Object[ANZConstants.FACT_SIZE];
		template[ANZConstants.FACT_SCENARIO_NAME_INDEX]=deal.getContainerName();
		template[ANZConstants.FACT_SCENARIO_PSR_NAME_INDEX]=deal.getPsrName();
		return template;
	}



	private void submitToSchema(final Object key,final Object[] fact,String psrName) throws Exception{
		try {
			activePivotSchema.put(key, fact);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, MessagesANZ.ROLLBACK_FOR_PSR, psrName);
			LOGGER.log(Level.SEVERE,MessagesANZ.ERROR_ADDING_DATA,e);
			rollback(activePivotSchema.getTransactionManager());
			throw e;
		}
	}


	Set<String> getPsrNames(){
	  // code changed to get only NonVarPSRs
		return psrDetail.getNonVarPsr();
		//return psrDetail.getAllPSRs();
		
	}

	public void setActivePivotManager(IActivePivotManager activePivotManager) {
		activePivotSchema= activePivotManager.getSchemas().get(name);		
	}

	public void setPsrDetail(PSRDetail psrDetail) {
		this.psrDetail = psrDetail;
	}


	@Override
	public PublisherType getType() {		
		return PublisherType.Sensitivities;
	}   


	public KeyGenerator getCounter() {
		return counter;
	}


	public void setCounter(KeyGenerator counter) {
		this.counter = counter;
	}
}
