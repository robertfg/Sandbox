package com.quartetfs.pivot.anz.source.publisher.impl;



import static com.quartetfs.pivot.anz.utils.ANZUtils.getFactTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import com.quartetfs.biz.pivot.IActivePivotSchema;
import com.quartetfs.biz.pivot.IProjection;
import com.quartetfs.fwk.IPair;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.filtering.ICondition;
import com.quartetfs.fwk.filtering.impl.InCondition;
import com.quartetfs.fwk.filtering.impl.SubCondition;
import com.quartetfs.fwk.impl.Pair;
import com.quartetfs.pivot.anz.model.impl.Deal;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.tech.indexer.IProcedure;
import com.quartetfs.tech.indexer.IReader;

class InsertUpdatePublishTask implements Callable<Void>,	IProcedure<IProjection> {

	/**
	 * serialVersionUID
	 */  
	private static final long serialVersionUID = 809838907637308734L;

	private Map<Long,Deal> pointDealMap;
	private IActivePivotSchema activePivotSchema;
	private String[] readerPattern;
	
	private Map<String, Integer> psrToIndex;
	private Map<String,Map<String, Integer>> psrIndexFieldMap = new HashMap<String, Map<String,Integer>>();
	private boolean createMapping;
	
	Map<String,Integer> readerPatternMapping;
	
	public InsertUpdatePublishTask(Map<Long,Deal> pointDealMap,IActivePivotSchema schema,final String[]readerPattern,
			Map<String,Integer> psrToIndex, boolean createMapping,Map<String,Integer> readerPatternMapping)
	{
		this.pointDealMap=pointDealMap;
		this.activePivotSchema=schema;
		this.readerPattern=readerPattern;		
		this.psrToIndex=psrToIndex;
		this.createMapping = createMapping;
		this.readerPatternMapping =  readerPatternMapping;
	}

	@Override
	public Void call() throws Exception {
		activePivotSchema.getIndexer().execute(Collections.<IPair<ICondition, IProcedure<IProjection>>> singleton(new Pair<ICondition, IProcedure<IProjection>>(
				new SubCondition("objectKey",new InCondition(pointDealMap.keySet())),this)));//the objectKey is the deal hash code
		return null;
	}
	
	@Override
	public boolean supportsParallelExecution() {
		return false;
	}
	
	//retrieve the fact already inserted and complete this fact with the new vector
	@Override
	public boolean execute(final IReader<IProjection> reader) {

		reader.setTuplePattern(readerPattern);
		while (reader.hasNext()) {
			reader.next();
			final Object[] tuple = reader.readTuple(); //old value in the indexer
        
			//get the deal
			final Long key  = (Long) tuple[0];
           	final Deal deal = pointDealMap.get(key); // new value to be inserted to indexer
			final String oldPsrName = (String)  tuple[ readerPatternMapping.get("psrName") ];
			
			initializeTuple(tuple,deal.getContainerName());
			tuple[ANZConstants.FACT_DEAL_INDEX] = deal;
		   
			if(deal.getPsrName().equalsIgnoreCase("TAZC1")) { 
			    //aggregate TAZC1 old data and new data
				if( oldPsrName.equals(deal.getPsrName()) ){
					tuple[readerPatternMapping.get("result")] =  (Double)tuple[readerPatternMapping.get("result")] + (Double) deal.getAttributes().get("result");  //M_RESULT
					tuple[readerPatternMapping.get("resultV")] =  (Double)tuple[readerPatternMapping.get("resultV")] + (Double) deal.getAttributes().get("resultV"); //M_RESULTV
				} else {
                   //override old TAZC0 data
					tuple[readerPatternMapping.get("result")] =  (Double) deal.getAttributes().get("result");  //M_RESULT
					tuple[readerPatternMapping.get("resultV")] =  (Double) deal.getAttributes().get("resultV"); //M_RESULTV
					
				}
			} else {
				 
				 if( oldPsrName.equalsIgnoreCase("TAZC0") ){
						tuple[readerPatternMapping.get("result")] =  (Double)tuple[readerPatternMapping.get("result")] + (Double) deal.getAttributes().get("result");  //M_RESULT
						tuple[readerPatternMapping.get("resultV")] =  (Double)tuple[readerPatternMapping.get("resultV")] + (Double) deal.getAttributes().get("resultV"); //M_RESULTV
						deal.getAttributes().put("result",  tuple[readerPatternMapping.get("result")]);
						deal.getAttributes().put("resultV", tuple[readerPatternMapping.get("resultV")]);
				 }
				
			}
			
			try {
				activePivotSchema.put(key, tuple);
			} catch (final Exception e) {
				throw new QuartetRuntimeException(e);
			}
			
			pointDealMap.remove(key);//remove the processed deal from the deals
		}
		return true;
	}

	@Override
	public void complete() {
		
		for (final Entry<Long,Deal> entry : pointDealMap.entrySet()) {
			Deal deal=entry.getValue();
			final Object[] fact = getFactTemplate(ANZConstants.FACT_SIZE,deal.getContainerName());
			initializeTuple(fact,deal.getContainerName());
			fact[ANZConstants.FACT_DEAL_INDEX] = deal;
			
			try {
				activePivotSchema.put(entry.getKey(), fact);//add the object into the transaction
			} catch (final Exception e) {
				throw new QuartetRuntimeException(e);
			}
		}
	}
	
	Object getValue(Deal deal,String column){
		Object value=deal.getAttributes().get(column);
		return value;
	}


	private void initializeTuple(Object fact[],String containerName)
	{
		fact[ANZConstants.FACT_SCENARIO_NAME_INDEX]=containerName;
		fact[ANZConstants.FACT_SCENARIO_PSR_NAME_INDEX]=containerName;
	}
}