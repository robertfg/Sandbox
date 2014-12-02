package com.quartetfs.pivot.anz.limits.service;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.TIntSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.apache.log4j.MDC;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.limits.bo.LimitConfigInfo;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;
import com.quartetfs.pivot.anz.utils.ANZConstants;

public class LimitMatcherTask implements Callable<List<LimitDetail>> {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private List<LimitDetail> limitToSearch;	
	private List<LimitDetail> searchResult = new ArrayList<LimitDetail>();;
	private LimitConfigInfo debug;
	private ILocation location;
	private String measureName;
	
	public LimitMatcherTask(ILocation location,String measureName,List<LimitDetail> taskQueue,LimitConfigInfo debug) {
		super();
		this.limitToSearch = taskQueue;		
		this.measureName = measureName;
		this.location=location;
		this.debug=debug;
	}


	@Override
	public List<LimitDetail> call() throws Exception 
	{
		MDC.put(ANZConstants.CONTEXT, location.hashCode());	
		for(LimitDetail limit :limitToSearch )
		{
			if(limit.getMeasureName().equals(measureName))
			{
				matchLimitLocations(location, limit);
			}				
		}
		
		MDC.remove(ANZConstants.CONTEXT);		
		return searchResult;
	}
	
	private void matchLimitLocations(final ILocation location, LimitDetail limit) 
	{
		
		for(TIntObjectMap<TIntObjectMap<Object>> singleLoc : limit.getLocationValues())
		{
			boolean matchRequired = isMatchRequired(location, singleLoc);			
			boolean result = matchRequired ? singleLoc.forEachEntry( new TIntObjectProcedure<TIntObjectMap<Object>>() {					
				@Override
				public boolean execute(final int dimensionIndex, TIntObjectMap<Object> levelValues) 
				{
					final int depth = location.getLevelDepth(dimensionIndex);	
					LevelMatchProcedure procedure=new LevelMatchProcedure(depth, dimensionIndex, location);					 
					boolean levelSearch = levelValues.forEachEntry(procedure);	
					if(!levelSearch) return false;					
					return (procedure.maxLevel == depth-1 );					
				}
			}):false;
			
			if(result)
			{
				info(String.format("Limit %s matched for %s location", limit.getId(),singleLoc));
				searchResult.add(limit);
				break;
			}
		}
	}


	private boolean isMatchRequired(ILocation location,TIntObjectMap<TIntObjectMap<Object>> singleLoc) 
	{
		boolean matchRequired=true;
		TIntSet dimensionIndex = singleLoc.keySet();
		int dimensionCount = location.getDimensionCount();
		for(int index =0;index<dimensionCount;index++)
		{
			if(!dimensionIndex.contains(index))
			{
				int depth = location.getLevelDepth(index);			
				if(depth>1)
				{
					matchRequired=false;
					break;
				}			
			}
		}
		return matchRequired;
	}
	
	private void info(String message)
	{
		if(debug.isDebugLimits())
		{
			LOGGER.info(message);
		}
	}
	
	private static class LevelMatchProcedure implements TIntObjectProcedure<Object> 
	{
		private final int depth;
		private final int dimensionIndex;
		private final ILocation location;
		int maxLevel=-1;

		public LevelMatchProcedure(int depth, int dimensionIndex,
				ILocation location) {
			this.depth = depth;
			this.dimensionIndex = dimensionIndex;
			this.location = location;
		}

		@Override
		public boolean execute(int levelIndex, Object levelValue) {
			max(levelIndex);
			
			if(depth<=levelIndex)
			{
				return false;
			}							
			Object valueTOCheck = location.getCoordinate(dimensionIndex, levelIndex);
			return valueTOCheck!=null && valueTOCheck.equals(levelValue);
		}

		private void max(int levelIndex) {
			if(maxLevel < levelIndex)
			{
				maxLevel=levelIndex;
			}
		}
	}

}
