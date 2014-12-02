package com.quartetfs.pivot.anz.postprocessing.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;
import com.quartetfs.pivot.anz.limits.bo.LimitMasterData;
import com.quartetfs.pivot.anz.limits.bo.LimitMasterData.LimitDataHolder;
import com.quartetfs.pivot.anz.limits.bo.LocationLimitDetails;
import com.quartetfs.pivot.anz.limits.service.LimitLocationResolver;
import com.quartetfs.pivot.anz.utils.ANZUtils;


@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = LimitMeasurePostProcessor.PLUGIN_KEY)
public class LimitMeasurePostProcessor extends APostProcessor<Object> implements IPrefetcher{

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private static final long serialVersionUID = -2533059314746154714L;	
	public static final String PLUGIN_KEY="MEASURE_LIMIT";	
	private int dateDimOridinal;
	private LimitMasterData limitMasterData;
	private LimitLocationResolver limitResolver;
	private LimitMode limitMode;
	
	public enum LimitMode
	{
		LIMIT,UTLIZATION
	}
	
	public LimitMeasurePostProcessor(String name, IActivePivot pivot) {
		super(name, pivot);
	}


	@Override
	public void init(Properties properties) throws QuartetException {
		super.init(properties);
		
		String cobDate = properties.getProperty("cobDate");
		IDimension cobDateDim = ANZUtils.findDimensionByName(pivot, cobDate);
		Validate.notNull(cobDateDim, String.format("Unable to find dimension with name %s",cobDate));
		dateDimOridinal = cobDateDim.getOrdinal()-1;		
		limitMode=LimitMode.valueOf(properties.getProperty("mode"));		
		getPrefetchers().addAll( Collections.<IPrefetcher> singletonList(this) );
		
	}	


	@Override
	public void evaluate(ILocation location,final IAggregatesRetriever retriever) throws QuartetException 
	{		
		calculate(location, retriever);			
	}
	
	private void calculate(ILocation location,final IAggregatesRetriever retriever) throws QueryException
	{
		ExecutorService matcherExecutors = createThreadPool();
		Timer t = scheduleTimerForCancel(matcherExecutors);	
		try
		{		
			
			final ICellSet cellSet = retriever.retrieveAggregates(Collections.singletonList(location),Collections.singletonList(underlyingMeasures[0]));
			long start = System.currentTimeMillis();			
			
			List<Future<LocationLimitDetails>> ftList = submitForMatch(matcherExecutors, cellSet);			
			writeLimitValue(retriever, cellSet, ftList);
			
			long end= System.currentTimeMillis();
			
			LOGGER.info(String.format("Mode : %s Total %s location processed in %sms ",limitMode,cellSet.getLocations().size(),(end-start)) );
		}
		catch(Exception e)
		{
			throw new QuartetRuntimeException(e);
		}
		finally
		{
			t.cancel();
			matcherExecutors.shutdownNow();			
		}
		
	}


	private void writeLimitValue(IAggregatesRetriever retriever, ICellSet cellSet,List<Future<LocationLimitDetails>> ftList)	
			throws InterruptedException, ExecutionException 
	{
		
		for(Future<LocationLimitDetails> future: ftList)
		{
			LocationLimitDetails details = future.get();	
			LimitDetail limit;
			
			if(CollectionUtils.isEmpty(details.getSearchResult())) continue;				
			if(details.getSearchResult().size() > 1)
			{
				//More than one limit matched
				LOGGER.warning(String.format("More than one limit matched for location %s, limit details %s",details.getLocation(),details.getSearchResult()));
				continue;
			}
			else
			{
			
				limit = details.getSearchResult().get(0);
				if(limit.isCombineLimit()) continue;				
			}
			
			double limitBeginValue=0;//limit.getBeginValue();
			double limitEndValue=0;//limit.getEndValue();
			String result = "";
			
			if(limitMode.equals(LimitMode.UTLIZATION))
			{
				
				double measureValue = (Double)cellSet.getCellValue(details.getLocation(), underlyingMeasures[0]);
				
				if(measureValue>=0){
				    if(limitEndValue == 0){
				    	result = "100";
				    }else {
				    	result = String.valueOf( (measureValue / limitEndValue) * 100 );    
				    }
				 } else {
					  if(limitBeginValue == 0){
						  result = "100";
					    }else {
					    	result =  String.valueOf((measureValue / limitBeginValue) * 100);
					    }
				}
			
			} else {
			  result = 	limitBeginValue + ":" + limitEndValue;
			}
			
			retriever.write(details.getLocation(),result);							
		}
	}


	private List<Future<LocationLimitDetails>> submitForMatch(ExecutorService matcherExecutors,ICellSet cellSet) 
	{
		Set<ILocation> locs = cellSet.getLocations();
		List<Future<LocationLimitDetails>> locationFutures = new ArrayList<Future<LocationLimitDetails>>(locs.size());
		
		for(ILocation singleLocation : locs)
		{
			isCancelled();				
			IDate cobDate = (IDate) singleLocation.getCoordinate(dateDimOridinal, 0);
			if(cobDate==null) continue;
			
			LimitDataHolder limitDetails = limitMasterData.getLimits().get(cobDate);
			if(limitDetails==null || CollectionUtils.isEmpty(limitDetails.getLimitDetails())) continue;			
			locationFutures.add(limitResolver.enqueForMatch(limitDetails.getLimitDetails(),singleLocation,underlyingMeasures[0],matcherExecutors));		
		}
		return locationFutures;
	}


	private ExecutorService createThreadPool()
	{
		final Thread queryThread  = Thread.currentThread();
		ExecutorService matcherExecutors = Executors.newFixedThreadPool(limitResolver.getLimitConfigInfo().getLimitMatcherThreads(), new ThreadFactory() {
			AtomicInteger threadCtr = new AtomicInteger();
			String namePrefix = queryThread.getName() + "-limitmatch-worker-";
			
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, namePrefix + threadCtr.incrementAndGet());
			}
		});		
		return matcherExecutors;
	}


	private Timer scheduleTimerForCancel(final ExecutorService resolveExecutors) 
	{
		final Thread queryThread  = Thread.currentThread();
		Timer t = new Timer(queryThread.getName() + "-limitmatch-timer");
		t.schedule( new TimerTask() {
			
			@Override
			public void run() {
				if(!resolveExecutors.isTerminated())
				{
					resolveExecutors.shutdownNow();	
					queryThread.interrupt();
				}
			}
		}, TimeUnit.SECONDS.toMillis(limitResolver.getLimitConfigInfo().getMatchTimeOutInSeconds()));		
		return t;
	}


	private void isCancelled() {
		if(Thread.currentThread().isInterrupted()) throw new CancellationException();
	}
	 

	@Override
	public String getType() {
		return PLUGIN_KEY;
	}


	@Override
	public Collection<ILocation> computeLocations(Collection<ILocation> locations) {
		return locations;
	}

	@Override
	public Collection<String> computeMeasures(Collection<ILocation> locations) {
		return Arrays.asList(underlyingMeasures[0]);
	}
	
	public void setLimitMasterData(LimitMasterData limitMasterData) {
		this.limitMasterData = limitMasterData;
	}
	public void setLimitResolver(LimitLocationResolver limitResolver) {
		this.limitResolver = limitResolver;
	}
	

}
