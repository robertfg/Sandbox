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
class VectorPublishTask implements Callable<Void>,	IProcedure<IProjection> {

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
	
	public VectorPublishTask(Map<Long,Deal> pointDealMap,IActivePivotSchema schema,final String[]readerPattern,Map<String,Integer> psrToIndex, boolean createMapping)
	{
		this.pointDealMap=pointDealMap;
		this.activePivotSchema=schema;
		this.readerPattern=readerPattern;		
		this.psrToIndex=psrToIndex;
		this.createMapping = createMapping;
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

	private Map<String,Integer> createPSRIndex(Deal deal)
	{
		if(createMapping)
		{  
			String psrName = (String)deal.getAttributes().get(ANZConstants.PSR_NAME);
			System.out.println( "Task PsrName:" + psrName );
			
			Map<String,Integer>fieldsToIndexPosition = psrIndexFieldMap.get(psrName);
			
	
			
			
			
			System.out.println( "Task PsrName fieldsToIndexPosition:" + fieldsToIndexPosition );
			if(fieldsToIndexPosition!=null) return fieldsToIndexPosition;
			int indexPosition=psrToIndex.get(psrName);
			fieldsToIndexPosition=new HashMap<String, Integer>();
			fieldsToIndexPosition.put(ANZConstants.VAR_COL, indexPosition);
			psrIndexFieldMap.put(psrName, fieldsToIndexPosition);
			
			return fieldsToIndexPosition;
		}
		else
		{
			return psrToIndex;
		}
	}
	
	//retrieve the fact already inserted and complete this fact with the new vector
	//e.g. the fact was stored with  1DVaRPL vector, we receive 10DVaRPL that we insert in the fact we add then the same fact with the new vector inserted
	@Override
	public boolean execute(final IReader<IProjection> reader) {
		reader.setTuplePattern(readerPattern);
		while (reader.hasNext()) {
			reader.next();
			final Object[] tuple = reader.readTuple();

			//get the deal
			final Long key = (Long) tuple[0];
			final Deal deal=pointDealMap.get(key);
			this.setPsrExtension(deal, tuple);
			initializeTuple(tuple,deal.getContainerName());
			tuple[ANZConstants.FACT_DEAL_INDEX] = deal;
	
					  	
			for (Entry<String, Integer> entry : createPSRIndex(deal).entrySet()) {
				String columnName = entry.getKey();
				int valueIndex = entry.getValue().intValue();
				// get the new value
				Object value = getValue(deal, columnName);
				// get the previous value if exists
				final Object prevValue = tuple[valueIndex];
				// previous does not exists
				if (prevValue == null) {
					tuple[valueIndex] = value;
				} else {// exists
					if (value instanceof double[]) {
						final double[] prevValueD = (double[]) prevValue;
						final double[] valueD = (double[]) value;
						for (int i = 0; i < prevValueD.length; i++) {
							valueD[i] += prevValueD[i];
						}
					} else if (value instanceof Number) {
						value = ((Number) value).doubleValue()
								+ ((Number) prevValue).doubleValue();
					} else {
						throw new IllegalStateException();
					}
					// consolidated value set here
					tuple[valueIndex] = value;
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
			
			for (Entry<String, Integer> fieldsEntry : createPSRIndex(deal).entrySet()) {
				Object value=getValue(deal,fieldsEntry.getKey());
				fact[fieldsEntry.getValue()] = value;
			}
			
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
	
	private void setPsrExtension(Deal deal,Object tuple[])
	{
		String psrExtA = String.valueOf(tuple[25]);
		String psrExtB = String.valueOf(tuple[26]);
		String psrExtH = String.valueOf(tuple[27]);
		
		if( psrExtA !=null && psrExtA.length()> 0 ){
			deal.setPsrExtA( "1D" );
		}
		
		if( psrExtB !=null && psrExtB.length()> 0){
			  deal.setPsrExtB( "10D" );
		}

		if( psrExtH !=null && psrExtH.length()> 0 ){
			deal.setPsrExtH( "B1" );
		}
		
		
		
	}
}