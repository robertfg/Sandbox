package com.quartetfs.pivot.anz.limits.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

import org.apache.log4j.MDC;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.pivot.anz.limits.bo.LimitConfigInfo;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;
import com.quartetfs.pivot.anz.limits.bo.LocationLimitDetails;
import com.quartetfs.pivot.anz.utils.ANZConstants;

public class LimitMatcherSubTask implements Callable<LocationLimitDetails> {

	private static final Logger LOGGER = Logger.getLogger(LimitMatcherSubTask.class.getSimpleName());
	
	private List<LimitDetail> limitToSearch;	
	private List<LimitDetail> searchResult = new ArrayList<LimitDetail>();
	private ILocation location;
	private String measureName;
	private LimitUtil util;
	private LimitConfigInfo debug;
	
		
	public LimitMatcherSubTask(ILocation location,String measureName,List<LimitDetail> limitToSearch,LimitConfigInfo debug) {
		super();
		this.limitToSearch = limitToSearch;		
		this.measureName = measureName;
		this.location=location;
		this.debug = debug;
		this.util = new LimitUtil();
			
	}


	@Override
	public LocationLimitDetails call() throws Exception 
	{
		LocationLimitDetails details = new LocationLimitDetails(location, searchResult);
		MDC.put(ANZConstants.CONTEXT, location.hashCode());	
		for(LimitDetail limit :limitToSearch)
		{
			isCancelled();
			if(limit.getMeasureName().equals(measureName))
			{
				if(util.matchLimitLocations(location, limit))
				{
					info(String.format("Limit %s matched for %s location", limit.getId(),location));
					searchResult.add(limit);
				}
			}				
		}
		
		MDC.remove(ANZConstants.CONTEXT);		
		return details;
	}


	private void isCancelled() {
		if(Thread.currentThread().isInterrupted()) throw new CancellationException();
	}
	
	private void info(String message)
	{
		if(debug.isDebugLimits())
		{
			LOGGER.info(message);
		}
	}
	
}
