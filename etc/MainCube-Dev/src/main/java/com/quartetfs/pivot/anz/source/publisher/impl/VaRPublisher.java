/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.source.publisher.impl;




import static com.quartetfs.pivot.anz.utils.ANZUtils.getFactTemplate;
import static com.quartetfs.pivot.anz.utils.TransactionUtils.rollback;

import java.util.HashMap;
import java.util.HashSet;
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
import com.quartetfs.fwk.transaction.TransactionException;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.model.impl.Deal;
import com.quartetfs.pivot.anz.service.impl.PSRDetail;
import com.quartetfs.pivot.anz.service.impl.PSRDetail.PublisherType;
import com.quartetfs.pivot.anz.source.IRunParseData;
import com.quartetfs.pivot.anz.source.publisher.IRunParseDataPublisher;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.ANZUtils;
import com.quartetfs.tech.dictionary.IDictionary;
import com.quartetfs.tech.point.impl.Point;

public class VaRPublisher implements IRunParseDataPublisher {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private IActivePivotManager activePivotManager;
	private String schemaName;
	IActivePivotSchema activePivotSchema;
	final Map<String,Integer> psrToIndex;
	final String[] readerPattern;
	private IKeyExtractor<Point> keyExtractor;
	private IDictionary<Point> pointDictionary;
	private Set<String> fieldsToGroup=new HashSet<String>();
	private PSRDetail psrDetail;

	public VaRPublisher(PSRDetail psrDetail) {
		this.psrToIndex = psrDetail.retrievePsr2Idx();
		this.readerPattern = psrDetail.retrieveIndexerFields();
		fieldsToGroup.add(ANZConstants.VAR_COL);
		fieldsToGroup.add("mResultDealccy");
		fieldsToGroup.add("mResultVDealccy");
		this.psrDetail = psrDetail;
	}

	public void setActivePivotManager( final IActivePivotManager activePivotManager) {
		this.activePivotManager = activePivotManager;		
	}

	public void setSchemaName(final String schemaName) {
		this.schemaName = schemaName;
		activePivotSchema = activePivotManager.getSchemas().get(schemaName);		
	}

	public String getName() {
		return getClass().getSimpleName() + " - " + schemaName;
	}

	public boolean publish(IRunParseData runParseData) throws Throwable {
		return _publish(runParseData);		
	}


	private boolean _publish(final IRunParseData runParseData) throws Throwable {
		// The overall is synch for the moment
		try {
			 
			activePivotSchema.getTransactionManager().startTransaction();
		
			StopWatch stop = new StopWatch(runParseData.mergedPSR().toString());
			
			publish(runParseData, stop);		
			commit(runParseData, stop);
			
			LOGGER.info(String.format("Publish Result %s", stop.toString()));
			
		}  catch (Throwable e) {
			LOGGER.log(Level.SEVERE, MessagesANZ.ROLLBACK_PROBLEM, e);
			rollback(activePivotSchema.getTransactionManager());
			throw e;
		}
		return true;
	}

	private void commit(final IRunParseData runParseData, StopWatch stop) throws TransactionException 
	{
		LOGGER.log(Level.INFO,MessagesANZ.START_COMMIT, runParseData.mergedPSR());			
		stop.start("Commit");			
		activePivotSchema.getTransactionManager().commit();
		stop.stop();
	}

	private void publish(final IRunParseData runParseData, StopWatch stop) throws Exception 
	{
		stop.start("Publish");
		LOGGER.log(Level.INFO,MessagesANZ.START_PUBLISH, runParseData.mergedPSR());
		
		final Map<Long, Deal> pointKeyMap=new HashMap<Long,Deal>(runParseData.getValueCount());
		
		for (Deal deal:runParseData.deals()){
		    Object[] template = null; //VAR_10D
		    String containerName = psrDetail.getVarPsrToContainerNameMapping().get( String.valueOf(deal.getAttributes().get("containerName"))) ;
		
		    LOGGER.info("containerName=" +   String.valueOf(deal.getAttributes().get("containerName")) + "," + containerName + ",psrName:" + deal.getPsrName());
		    
		    deal.setPsrName("V1AL0");
		    
		    template = getFactTemplate(4,containerName ) ;
			
			final Object[] fact = template.clone();
			fact[ANZConstants.FACT_DEAL_INDEX] = deal;
			Point p=keyExtractor.extractKey(fact);
			
			deal.setContainerName( containerName ) ;//(String) deal.getAttributes().get("containerName") );
	
			
			
			Long key=new Long(pointDictionary.map(p));
			Deal n=pointKeyMap.get(key);
			if(n==null)
				pointKeyMap.put(key, deal);
			else 
				aggregateValues(n,deal); 
		}
		 
		// create the publish task and do the processing
		createPublishTask(pointKeyMap).call();
		stop.stop();
	}

	private void aggregateValues(Deal o, Deal n){
		Object result=null;;
		if (o.getAttributes().get(ANZConstants.VAR_COL) instanceof double[]){
			Object oldValueObj=o.getAttributes().get(ANZConstants.VAR_COL);
			Object newValueObj=n.getAttributes().get(ANZConstants.VAR_COL);
			if (oldValueObj==null && newValueObj==null) return;
			o.getAttributes().put(ANZConstants.VAR_COL,ANZUtils.addTwoArrays(oldValueObj, newValueObj));
		}else if (o.getAttributes().get(ANZConstants.VAR_COL) instanceof Double){
			for (String field:fieldsToGroup){
				Double oldValue=(Double)(o.getAttributes().get(field)==null?0:o.getAttributes().get(field));
				Double newValue=(Double)(n.getAttributes().get(field)==null?0:n.getAttributes().get(field));
				oldValue+=newValue;
				result=oldValue;
				o.getAttributes().put(field,result);
			}
			
		}
		
	}

	private Callable<Void> createPublishTask(final Map<Long,Deal> dealPointMap) {		
		return new VectorPublishTask(dealPointMap,activePivotSchema,readerPattern,psrToIndex, true);
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
		return PublisherType.Var;
	}
}
