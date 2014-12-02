/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.source.publisher.impl;



import static com.quartetfs.pivot.anz.utils.TransactionUtils.rollback;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StopWatch;

import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.IActivePivotSchema;
import com.quartetfs.biz.pivot.transaction.IKeyExtractor;
import com.quartetfs.biz.pivot.transaction.ISchemaTransactionManager;
import com.quartetfs.fwk.transaction.TransactionException;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.model.impl.Deal;
import com.quartetfs.pivot.anz.service.impl.PSRDetail;
import com.quartetfs.pivot.anz.service.impl.PSRDetail.PublisherType;
import com.quartetfs.pivot.anz.source.IRunParseData;
import com.quartetfs.pivot.anz.source.publisher.IRunParseDataPublisher;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.KeyGenerator;
import com.quartetfs.tech.dictionary.IDictionary;
import com.quartetfs.tech.point.impl.Point;


public class PsrInsertUpdatePublisher implements IRunParseDataPublisher {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private IActivePivotManager activePivotManager;
	private String schemaName;
	private IActivePivotSchema activePivotSchema;
	private final String[] readerPattern;
	private final Map<String,Integer> fieldToIndexPos;
	private final Map<String, String> fieldsToClean;
	private IDictionary<Point> pointDictionary;
	private KeyGenerator counter;
	private IKeyExtractor<Point> keyExtractor;
	
	private Set<String> psrToBeProcessList;
	private Set<String> insertUpdatePSRs;
	private Map<String,Integer> msrToAggregate;
	private Map<String,Integer> readerPatternMapping;
	
	
	
	public PsrInsertUpdatePublisher(PSRDetail psrDetail) {
		this.insertUpdatePSRs = psrDetail.getInsertUpdatePSRs();
		this.readerPattern = psrDetail.retrieveIndexerFields();
		this.fieldToIndexPos=psrDetail.getField2IndexPosition();
		this.fieldsToClean=psrDetail.getFieldsToClean();
	}

	public void setActivePivotManager(	final IActivePivotManager activePivotManager) {
		this.activePivotManager = activePivotManager;
	}

	public void setSchemaName(final String schemaName) {
		this.schemaName = schemaName;
		activePivotSchema = activePivotManager.getSchemas().get(schemaName);		
	}

	public String getName() {
		return getClass().getSimpleName() + " - " + schemaName;
	}

	public boolean publish(final IRunParseData runParseData) throws Throwable {
		boolean publish = false;
		
		if ( this.insertUpdatePSRs.contains(runParseData.getPSRName())) {	
			if(this.psrToBeProcessList.contains(runParseData.getPSRName())){
				publish = _publish(runParseData);
			}
		}
		return publish;
		
	}	
	
	private Object[] getFactTemplate(int size, Deal deal)
	{
		Object [] template= new Object[size];
		template[0]= deal.getContainerName();
		template[1]= deal.getContainerName(); 
		return  template; 
	}

	private boolean _publish(final IRunParseData runParseData) throws Throwable {
		ISchemaTransactionManager tm= activePivotSchema.getTransactionManager();
		try 
		{
			tm.startTransaction();
			StopWatch stop = new StopWatch(runParseData.mergedPSR().toString());
						
			publish(runParseData, stop);
			commit(runParseData, stop);			
			LOGGER.info(String.format("Publish Result %s", stop.toString()));
			
		} catch (Throwable th) {
			LOGGER.log(Level.SEVERE,MessagesANZ.ERROR_ADDING_DATA,th);
			rollback(tm);
			throw th;
		}
		return true;
	}

	private void commit(final IRunParseData runParseData, StopWatch stop) throws TransactionException 
	{
		stop.start("Commit");
		LOGGER.log(Level.INFO,MessagesANZ.START_COMMIT, runParseData.mergedPSR());			
		activePivotSchema.getTransactionManager().commit();
		stop.stop();
	}

	private void publish(final IRunParseData runParseData, StopWatch stop) throws Exception 
	{
		stop.start("Publish");
		LOGGER.log(Level.INFO,MessagesANZ.START_PUBLISH, runParseData.mergedPSR());			
	
		final Map<Long, Deal> pointKeyMap=new HashMap<Long,Deal>(runParseData.getValueCount());			
		for (Deal deal:runParseData.deals()){
			final Object[] fact = getFactTemplate( ANZConstants.FACT_SIZE,deal);
			fact[ANZConstants.FACT_DEAL_INDEX] = deal;
			Point p = keyExtractor.extractKey(fact);
			
			Long key = new Long(pointDictionary.map(p));
			Deal oldValue = pointKeyMap.get(key);
			
			if (oldValue != null) {
				aggregateValues(oldValue, deal);
			} else {
				pointKeyMap.put(key, deal);
			}
	
		}

//		//check consistency
//		if (pointKeyMap.size() != runParseData.getValueCount()) {
//			throw new IllegalStateException("inconsistency in deals to publish");
//		}
		
		createPublishTask(pointKeyMap,runParseData).call();
		stop.stop();
	}
	
	private void aggregateValues(Deal oldD, Deal newD){
		
/*		Map<String,Integer> posToAggregate = new HashMap<String,Integer>();
		
	
			posToAggregate.put("result", 28 );
			posToAggregate.put("resultV", 29 );
			*/
		
		
		for ( Map.Entry<String,Integer> entry : msrToAggregate.entrySet()) {
			Double oldValueObj = (Double)oldD.getAttributes().get( entry.getKey());
			Double newValueObj = (Double)newD.getAttributes().get( entry.getKey());
			
			if (oldValueObj==null && newValueObj==null) continue;
			
			
			oldD.getAttributes().put(entry.getKey(),  oldValueObj+=newValueObj );
		}
	}
   
	
		private Callable<Void> createPublishTask(final Map<Long,Deal> dealPointMap,final IRunParseData runParseData) 
		{
			return new InsertUpdatePublishTask(dealPointMap,activePivotSchema,readerPattern,fieldToIndexPos, false,readerPatternMapping);
		}


	
	public void setKeyExtractor(IKeyExtractor<Point> keyExtractor) {
		this.keyExtractor = keyExtractor;
	}
	@Required
	public void setPointDictionary(IDictionary<Point> pointDictionary) {
		this.pointDictionary = pointDictionary;
	}
	
	@Override
	public PublisherType getType() {		
		return PublisherType.InsertUpdate;
	}

	public void setCounter(KeyGenerator counter) {
		this.counter = counter;
	}

	public KeyGenerator getCounter() {
		return counter;
	}

	public Set<String> getPsrToBeProcessList() {
		return psrToBeProcessList;
	}

	public void setPsrToBeProcessList(Set<String> psrToBeProcessList) {
		this.psrToBeProcessList = psrToBeProcessList;
	}

	public Set<String> getInsertUpdatePSRs() {
		return insertUpdatePSRs;
	}

	public void setInsertUpdatePSRs(Set<String> insertUpdatePSRs) {
		this.insertUpdatePSRs = insertUpdatePSRs;
	}

	public void setMsrToAggregate(Map<String,Integer> msrToAggregate) {
		this.msrToAggregate = msrToAggregate;
	}

	public Map<String,Integer> getMsrToAggregate() {
		return msrToAggregate;
	}

	public void setReaderPatternMapping(Map<String,Integer> readerPatternMapping) {
		this.readerPatternMapping = readerPatternMapping;
	}

	public Map<String,Integer> getReaderPatternMapping() {
		return readerPatternMapping;
	}

	
	
}