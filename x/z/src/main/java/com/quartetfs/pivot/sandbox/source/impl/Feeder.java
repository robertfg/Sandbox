/*
 * (C) Quartet FS 2007-2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.source.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.quartetfs.biz.pivot.IContribution;
import com.quartetfs.biz.pivot.impl.Contribution;
import com.quartetfs.biz.pivot.transaction.IKeyExtractor;
import com.quartetfs.fwk.AgentException;
import com.quartetfs.fwk.messaging.ISource;
import com.quartetfs.fwk.messaging.ISourceListener;
import com.quartetfs.fwk.messaging.ITransaction;
import com.quartetfs.fwk.transaction.ITransactionWrapper;
import com.quartetfs.pivot.sandbox.model.impl.Trade;

/**
 * <b>Feeder</b>
 * This is a sample class that receives objects from the source
 * and submit them to the schema thanks to the TransactionWrapper
 * 
 * @author Quartet Financial Systems
 */
public class Feeder implements ISourceListener{

	/** TransactionWrapper is in charge of transaction commit */
	protected ITransactionWrapper<IContribution, Object> transactionWrapper;

	/** Key extractor */
	protected IKeyExtractor<Object> keyExtractor;

	@Override
	@SuppressWarnings("unchecked")
	public void receive(String subject, Object object, Properties props, ITransaction trans) {
		// In our case received  objects are Lists
		if (object instanceof List){
			List<Trade> trades = (List<Trade>) object;

			List<IContribution> transactionContent = new ArrayList<IContribution>(trades.size());
			// build the batched transaction
			for (Trade trade: trades ){
				Object key = keyExtractor.extractKey(trade);
				transactionContent.add(new Contribution(key, trade));
			}

			// do the transaction
			if(props != null && "true".equalsIgnoreCase(props.getProperty(TradeSource.SYNCHRONOUS_UPDATE_PROPERTY))) {
				transactionWrapper.doSynchronousTransaction(transactionContent, null);
			} else {
				transactionWrapper.addAll(transactionContent);
			}

		}
	}

	//not implemented
	@Override
	public void sourcePaused(ISource source) {}

	@Override
	public void sourceResumed(ISource source) {}

	@Override
	public void sourceStarted(ISource source) {}

	@Override
	public void sourceStopped(ISource source) {
		try {
			transactionWrapper.stop();
		} catch (AgentException e) {
			e.printStackTrace();
		}
	}

	public void setTransactionWrapper(ITransactionWrapper<IContribution, Object> transactionWrapper) {
		this.transactionWrapper = transactionWrapper;
	}

	public void setKeyExtractor(IKeyExtractor<Object> keyExtractor) {
		this.keyExtractor = keyExtractor;
	}

}