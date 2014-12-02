package com.quartetfs.pivot.anz.limits.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.springframework.util.StopWatch;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.IMember;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.fwk.transaction.TransactionException;
import com.quartetfs.fwk.util.impl.MappedTuple;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.limits.bo.LimitConfigInfo;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;
import com.quartetfs.pivot.anz.limits.bo.LimitMasterData.LimitDataHolder;
import com.quartetfs.pivot.anz.limits.bo.LocationLimitDetails;
import com.quartetfs.pivot.anz.utils.QueryHelper;

public class LimitLocationResolver {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private IActivePivot pivot;	
	private QueryHelper queryHelper;
	private DateParser parser;
	private String pivotId;
	private LimitConfigInfo limitConfigInfo;
	
	private ExecutorService resolveExecutors;
	private Set<String> measureNames = new HashSet<String>();
	
	public LimitLocationResolver(String pivotId,LimitConfigInfo config) {
		super();		
		this.pivotId=pivotId;
		this.limitConfigInfo=config;			
		this.parser =(DateParser) Registry.getPlugin(IParser.class).valueOf("date[dd-MM-yyyy]");
		
		
		this.resolveExecutors = Executors.newFixedThreadPool(config.getLimitResolveThreads(), new ThreadFactory() {
			AtomicInteger threadCtr = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r,"limitresolve-worker-" + threadCtr.incrementAndGet());
			}
		});		
	}	


	public void init(IActivePivotManager manager)
	{
		this.pivot = manager.getActivePivots().get(pivotId);
		this.queryHelper = new QueryHelper(pivot);		
		loadMeasureNames();
	}


	private void loadMeasureNames() {
		List<? extends ILevel> levels = pivot.getDimensions().get(0).getLevels();
		for(ILevel level : levels)
		{
			List<? extends IMember> members = level.getMembers();
			for(IMember member : members)
			{
				measureNames.add(member.getName());
			}
		}
	}
	
	public LimitDataHolder resolveLimit(List<MappedTuple> limitData) throws InterruptedException, ExecutionException, TransactionException
	{
		Map<String,LimitDetail> limitMap = new HashMap<String,LimitDetail>();
		StopWatch stopWatch = new StopWatch();
		Set<IDate> limitDates = new HashSet<IDate>();
		Set<String> measures = new HashSet<String>();
		 
		int totalNoOfLimit=limitData.size();
		
		ConcurrentMap<Object, Object> limitCache =   new ConcurrentHashMap<Object,Object>();
		
		LOGGER.info(String.format("Resolve Started. No of limits to resolve %s",totalNoOfLimit));
		stopWatch.start("Resolve");		
		ExecutorCompletionService<LimitDetail> completionService = new ExecutorCompletionService<LimitDetail>(resolveExecutors);
		
		for(MappedTuple tuple : limitData)
		{
			String measureName = (String)tuple.get("Measure_Name");
			       measureName = measureName.replace("[Measures].[","").replace("]","");
			       
			Validate.isTrue(measureNames.contains(measureName), String.format("Measure %s not found", measureName));
			completionService.submit(new LimitResolveTask(tuple, queryHelper, limitConfigInfo, pivot, parser,limitCache));		
		}	
		
		int cnt;
		for(cnt=0;cnt<totalNoOfLimit;cnt++)
		{
			
			LimitDetail limit = completionService.take().get();
			if(limit!=null){
				limitDates.add(limit.getLimitDate());			
				Validate.isTrue(limitDates.size()==1,String.format("More than one dates of limit passed in single dataset. Dates %s", limitDates));
				
				LimitDetail prevDetails = limitMap.put(limit.getId(),limit);
				//Validate.isTrue(prevDetails==null, String.format( "Duplicate record found for limit %s", limit.getId()));
				
				if(cnt%1000==0)
				{
					LOGGER.info(String.format(" Resolved Count %s , pending %s", cnt, totalNoOfLimit-cnt));
				}
				measures.add(limit.getMeasureName());
			}else {
				
			}
			
		}		
		stopWatch.stop();
		LOGGER.info(String.format(" Resolved Count %s , pending %s", cnt, totalNoOfLimit-cnt));
		LOGGER.info("Resolve Finished"); 
		LOGGER.info(String.format("Time Taken for resolve %s", stopWatch));
		
		
		//fakeTransaction();
		
		return new LimitDataHolder(limitCache, new ArrayList<LimitDetail>(limitMap.values()),measures);
		
	}


	private void fakeTransaction() throws TransactionException 
	{
		//Fake transaction to invalid aggregate cache
		pivot.getTransactionManager().startTransaction();
		pivot.getTransactionManager().commit();
	}
	
	public Future<LocationLimitDetails> enqueForMatch(List<LimitDetail> limitsToMatch,ILocation location, String measureName,ExecutorService resolveExecutors) 
	{
		try 
		{
			return resolveExecutors.submit(new LimitMatcherSubTask(location,measureName,limitsToMatch, limitConfigInfo));
		} catch (Exception e) {
			throw new QuartetRuntimeException(e);
		}	 
	}	
	
	public LimitConfigInfo getLimitConfigInfo() {
		return limitConfigInfo;
	}
	
}
