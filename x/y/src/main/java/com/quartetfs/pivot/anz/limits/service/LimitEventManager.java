package com.quartetfs.pivot.anz.limits.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;
import com.quartetfs.pivot.anz.limits.bo.LimitMasterData;
import com.quartetfs.pivot.anz.limits.bo.LimitMasterData.LimitDataHolder;

@ManagedResource
public class LimitEventManager {

	private static final Logger LOGGER = Logger.getLogger(LimitEventManager.class.getSimpleName());
	private LimitMasterData limitMasterData;
	private DateParser parser;
	private ConcurrentHashMap<String,List<Object[]>> events = new ConcurrentHashMap<String,List<Object[]>>(); 
	private FastDateFormat dateFormat;
	private final String lineSeperator = System.getProperty("line.separator");
	
	public LimitEventManager(LimitMasterData limitMasterData) {
		this.parser =(DateParser) Registry.getPlugin(IParser.class).valueOf("date[yyyyMMdd]");
		this.limitMasterData=limitMasterData;
		this.dateFormat = FastDateFormat.getInstance("yyyyMMdd");
	}
	
	@ManagedOperation(description="Remove/Delete limit for specified date (yyyyMMdd) ")
	public void removeLimitDetails(String date)
	{
		Date currentDate =  new Date(System.currentTimeMillis());		
		addEvent(dateFormat.format(currentDate), new Object[]{"REMOVE_REQUESTED",new Date(System.currentTimeMillis()),date});
		
		IDate cobDate = Registry.create(IDate.class, parser.parse(date).getTime());
		LOGGER.info("Removing data for " + cobDate);
		LimitDataHolder limits = limitMasterData.getLimits().remove(cobDate);
		Validate.notNull(limits, String.format("Limit with %s date not found",date));
		LOGGER.info(String.format("%s limits removed for %s date " , limits.getLimitDetails().size(), cobDate));
			
		addEvent(dateFormat.format(currentDate), new Object[]{"REMOVE_DONE",new Date(System.currentTimeMillis()),date});
	}
	
	
	public void removeAllLimitDetails()
	{
		Date currentDate =  new Date(System.currentTimeMillis());		
		addEvent(dateFormat.format(currentDate), new Object[]{"ALL_LIMITS_REMOVE_REQUESTED",new Date(System.currentTimeMillis())});
		
		LOGGER.info("Removing all limit details.");
		dumpLimitDates();
		limitMasterData.getLimits().clear();		
		LOGGER.info("All limits removed");
		
		
		addEvent(dateFormat.format(currentDate), new Object[]{"ALL_LIMITS_REMOVE_DONE",new Date(System.currentTimeMillis())});
	}
	
	@ManagedOperation(description="Dump limit details for specified date (yyyyMMdd) ")
	public void dumpLimitDetails(String date)
	{
		IDate cobDate = Registry.create(IDate.class, parser.parse(date).getTime());
		LimitDataHolder limits = limitMasterData.getLimits().get(cobDate);		
		if(limits==null || CollectionUtils.isEmpty(limits.getLimitDetails())) 
		{
			LOGGER.info(String.format("No limit found for %s",date));
			return;
		}
		StringBuilder sb = new StringBuilder(1000).append(lineSeperator);
		sb.append("Details for date:").append(date).append(lineSeperator);
		sb.append("LimitId,MeasureName,limitValue,combineLimit,locationAttributes,combileLimitLevels,locationValues").append(lineSeperator); // Header
		int cnt=0;
		for(LimitDetail detail : limits.getLimitDetails())
		{
			sb.append(detail.getId()).append(",")
			.append(detail.getMeasureName()).append(",")
			.append(detail.isCombineLimit()).append(",")
			.append(detail.getLocationAttributes()).append(",")
			.append(detail.getCombileLimitLevels()).append(",")
			.append(detail.getLocationValues().toString().replaceAll(",", "#")).append(lineSeperator);
			cnt++;
			
			if(cnt%100==0)
			{
				LOGGER.info(sb.toString());
				cnt=0;
				sb.setLength(0);
			}
		}
		LOGGER.info(sb.toString());		
	}
	
	@ManagedOperation(description="Dump limit dates (yyyyMMdd) ")
	public void dumpLimitDates()
	{
		if(limitMasterData.getLimits().isEmpty()) 
		{
			LOGGER.info(String.format("Limits not loaded"));
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Limit Data details. Dates in (yyyyMMdd)").append(lineSeperator);
		for(Map.Entry<IDate, LimitDataHolder> entry : limitMasterData.getLimits().entrySet())
		{
			sb.append("Limit Date:").append(dateFormat.format(entry.getKey().javaDate()))
			   .append(", Count:").append(entry.getValue().getLimitDetails().size())
			   .append(lineSeperator);
		}
		LOGGER.info(sb.toString());	
	}
	
	@ManagedOperation(description="Dump limit events.")
	public void dumpEvents()
	{
		for(Map.Entry<String,List<Object[]>> entry : events.entrySet())
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Events for ").append(entry.getKey()).append(lineSeperator);
					
			for(Object[] value : entry.getValue())
			{
				sb.append(Arrays.toString(value)).append(lineSeperator);
			}
			LOGGER.info(sb.toString());			
		}
	}
	
	public void addEvent(String date,Object[] values)
	{
		List<Object[]> event = new ArrayList<Object[]>();
		List<Object[]> existingEvent = events.putIfAbsent(date, event);
		if(existingEvent!=null)
		{
			event=existingEvent;
		}
		event.add(values);
		
	}
	
}
