package com.quartetfs.pivot.anz.limits.service;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.fwk.util.impl.MappedTuple;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.limits.bo.LimitConfigInfo;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;

import com.quartetfs.pivot.anz.utils.ANZUtils;
import com.quartetfs.pivot.anz.utils.QueryHelper;

public class LimitResolveTask implements Callable<LimitDetail> {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private MappedTuple tuple;
	private QueryHelper queryHelper;
	private LimitConfigInfo configInfo;
	private IActivePivot pivot;	
	private DateParser parser;
	private ConcurrentMap<Object, Object> cache;
		
	
	public LimitResolveTask(MappedTuple tuple,QueryHelper queryHelper,LimitConfigInfo debug,IActivePivot pivot,DateParser parser,ConcurrentMap<Object, Object> cache) 
	{
		super();
		this.tuple = tuple;
		this.queryHelper = queryHelper;
		this.configInfo=debug;
		this.pivot=pivot;
		this.parser=parser;
		this.cache = cache;		
		Validate.notNull(cache, "Cache is null");
	}

	@Override
	public LimitDetail call() throws Exception {
		
		
		
		
		Date limitDateTmp = (Date)getCacheValue((Date)tuple.get("Date"));	
		IDate limitDate  = Registry.create(IDate.class, limitDateTmp.getTime());
		limitDate.applyTime(0, 0, 0, 0);
		
		
		String measureName = (String)getCacheValue(tuple.get("Measure_Name"));
		
		String limitId = (String)tuple.get("Limit_ID");
		String attributes = (String)tuple.get("Attributes");				
		String weight = (String)tuple.get("Weight");
		String calculationType = (String)tuple.get("Calculation_Type");
		
		info(String.format("Parsing Started for Limt id %s", limitId));
		LimitDetail limit = new LimitDetail(limitId,limitDate,weight,measureName,attributes,calculationType);
		List<Map<String, Object>> resolvedLocation = null;
	
		try{
			resolvedLocation = resolveLimitLocation(limit);	
		}catch(Exception  e){
			return null;
		}
		info(String.format("Limit details %s", limit));			
		identifyLocationPartsIndex(limit, resolvedLocation);
		limit.setLocationAttributes(null);
		return limit;		
	}
	
	private List<Map<String, Object>> resolveLimitLocation(LimitDetail limit) throws Exception
	{
	
		Map<String, Map<String, Set<LevelValue>>> reslovedLimitDimension =null;
	   try{
			reslovedLimitDimension = parseLimitLocation(limit);		
	   }catch(Exception e){
		   throw e;
	   }
		List<List<Map<String, Object>>> dimLocations = crossJoinLevels(reslovedLimitDimension);		
		List<Map<String, Object>> finalLocation = crossJoinDimension(dimLocations);	
		
		return finalLocation;  
	}
	
	private void info(String message)
	{
		if(configInfo.isDebugLimits())
		{
			LOGGER.info(message);
		}
	}
	
	private void identifyLocationPartsIndex(LimitDetail limit,List<Map<String, Object>> resolvedLocation) 
	{
		List<TIntObjectMap<TIntObjectMap<Object>>> locationValues = new ArrayList<TIntObjectMap<TIntObjectMap<Object>>>();			
		for(final Map<String, Object> params : resolvedLocation)
		{
			TIntObjectMap<TIntObjectMap<Object>> dimensionValuesMap = new TIntObjectHashMap<TIntObjectMap<Object>>();
			for(Map.Entry<String, Object> entry : params.entrySet())
			{
				int[] ordinal = queryHelper.retrieveLevelOrdinals(entry.getKey());
				TIntObjectMap<Object> levelValues = new TIntObjectHashMap<Object>();
				TIntObjectMap<Object> existingValues = dimensionValuesMap.putIfAbsent(ordinal[0]-1, levelValues);
				if(existingValues!=null)
				{
					levelValues=existingValues;
				}
				levelValues.put(ordinal[1], getCacheValue(entry.getValue()));					
			}				
			locationValues.add(dimensionValuesMap);
		}
		info(String.format("Limit %s locations %s", limit.getId(),locationValues));
		limit.setLocationValues(locationValues);
	}
	
	private List<Map<String, Object>> crossJoinDimension(List<List<Map<String, Object>>> dimLocations) 
	{
		List<Map<String,Object>> crossJoinLocation = new ArrayList<Map<String,Object>>();
		Iterator<List<Map<String,Object>>> dimItr = dimLocations.iterator();
		crossJoinLocation.addAll(dimItr.next());		
		for(;dimItr.hasNext();)
		{
			List<Map<String,Object>> nextDimension = dimItr.next();
			List<Map<String,Object>> tmpCrossJoinLocations = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> currentDimensionMap : crossJoinLocation)
			{
				for(Map<String,Object> nextDimensionMap : nextDimension)
				{
					Map<String,Object> tmpValues = new HashMap<String,Object>();
					tmpValues.putAll(nextDimensionMap);
					tmpValues.putAll(currentDimensionMap);
					tmpCrossJoinLocations.add(tmpValues);
				}
			}
			crossJoinLocation=tmpCrossJoinLocations;
		}
		return crossJoinLocation;
	}


	private List<List<Map<String, Object>>> crossJoinLevels(Map<String, Map<String, Set<LevelValue>>> reslovedLimitDimension) {
		
		List<List<Map<String,Object>>> dimLocations = new ArrayList<List<Map<String,Object>>>();		
		for(Map.Entry<String,Map<String,Set<LevelValue>>> entry:reslovedLimitDimension.entrySet())
		{
			List<Map<String,Object>> dimLevelCrossJoin = crossJoinDimensionLevels(entry.getValue());
			dimLocations.add(dimLevelCrossJoin);
		}
		return dimLocations;
	}  
	
	private Map<String, Map<String, Set<LevelValue>>> parseLimitLocation(LimitDetail limit) throws Exception 
	{
		
		StringTokenizer dimValues = new StringTokenizer(limit.getLocationAttributes(), configInfo.getDimensionSeparator());
		Map<String,Map<String,Set<LevelValue>>> dimensionMap = new HashMap<String,Map<String,Set<LevelValue>>>();
		Set<String> combineLimitLevels = new HashSet<String>();
		
		for(;dimValues.hasMoreTokens();)
		{
			String value = dimValues.nextToken();			
			String[] dimLocationValues =  value.split(configInfo.getLocationSeparator());
		
			if(dimLocationValues.length > 1)
			{
				try{
					parseDimensionLocationPart(limit, dimensionMap,combineLimitLevels, dimLocationValues);			
				}catch(Exception e){
					throw e;
				}
			}  
		}			
		limit.setCombileLimitLevels(combineLimitLevels);   
				
		info(String.format("Dimension Values Details %s", dimensionMap));	
		return dimensionMap;
	}

	private List<Map<String,Object>> crossJoinDimensionLevels(Map<String,Set<LevelValue>> dimensionLevels)
	{
		List<Map<String,Object>> dimensionLocs = new ArrayList<Map<String,Object>>();
			
		Iterator<Map.Entry<String,Set<LevelValue>>> levelsItr = dimensionLevels.entrySet().iterator();		
		Entry<String,Set<LevelValue>> first = levelsItr.next();		
		
		for(LevelValue firstLevelValue : first.getValue())
		{
			Map<String,Object> levelValueMap = new HashMap<String,Object>();
			levelValueMap.put(firstLevelValue.getLevelName(), firstLevelValue.getLevelValue());
			dimensionLocs.add(levelValueMap);			
		}
		
		while(levelsItr.hasNext())
		{
			Entry<String,Set<LevelValue>> next = levelsItr.next();
			List<Map<String,Object>> tmpDimensionLocs = new ArrayList<Map<String,Object>>();			
			for(Map<String,Object> value : dimensionLocs)
			{
				for(LevelValue firstLevelValue : next.getValue())
				{
					Map<String,Object> levelValueMap = new HashMap<String,Object>();					
					levelValueMap.put(firstLevelValue.getLevelName(), firstLevelValue.getLevelValue());
					levelValueMap.putAll(value);					
					tmpDimensionLocs.add(levelValueMap);	
				}
			}			
			dimensionLocs=tmpDimensionLocs;		
		}
		return dimensionLocs;		
	}	
	
	private void parseDimensionLocationPart(LimitDetail limit,Map<String, Map<String, Set<LevelValue>>> dimensionMap,
			Set<String> combineLimitLevels, String[] dimLocationValues) throws Exception
	{   
		  
		String dimensionName = dimLocationValues[0];
		IDimension dimensionObj = ANZUtils.findDimensionByName(pivot, dimensionName);
		try{
			Validate.notNull(dimensionObj, String.format("Unable to find dimension %s ", dimensionName));
		}catch(Exception e){
			LOGGER.log(Level.WARNING, String.format("Unable to find dimension %s ", dimensionName)); 
			throw e;
			
		}
		Map<String, Set<LevelValue>> levelToValueMap = createDimensionMap(dimensionMap, dimensionName);
		
		String dimensionValue = dimLocationValues[1];	
		String[] paths = dimensionValue.split(Pattern.quote(configInfo.getLevelValueSeparator()));
		
		for(int pathCnt=0;pathCnt<paths.length;pathCnt++)
		{
			String loc = paths[pathCnt];
			String[] locationParts = loc.split(Pattern.quote(ILocation.LEVEL_SEPARATOR));
			Validate.isTrue(dimensionObj.getLevels().size()>=locationParts.length,String.format("Limit Id %s , Location Value %s is invalid",limit.getId(),dimensionValue));
			for(int cnt=0;cnt<locationParts.length;cnt++)
			{				
				String levelValue = locationParts[cnt];
				if(levelValue.equals(ILevel.ALLMEMBER))
				{
					continue;
				}
				//if not all member 
				ILevel level  = dimensionObj.getLevels().get(cnt);			
				String levelName = getLevelFQN(level.getName(),dimensionObj.getName());		
			
				Set<LevelValue> levelValues = levelToValueMap.get(levelName);
				if(levelValues==null)
				{
					levelValues =new LinkedHashSet<LevelValue>();
					levelToValueMap.put(levelName, levelValues);
				}					
				//Dimension value can contain multiple values - this will be combined limit
				parseLevelValues(levelValue,levelName, levelValues);					
				checkForCombineLimit(limit, combineLimitLevels, levelName,levelValues);
			}
		}
	}
	

	private Map<String, Set<LevelValue>> createDimensionMap(Map<String, Map<String, Set<LevelValue>>> dimensionMap,String dimensionName) 
	{
		Map<String,Set<LevelValue>> levelToValueMap = dimensionMap.get(dimensionName);
		if(levelToValueMap==null)
		{
			levelToValueMap = new HashMap<String,Set<LevelValue>>();
			dimensionMap.put(dimensionName, levelToValueMap);
		}
		return levelToValueMap;
	}
	
	
	private String getLevelFQN(String levelName,String dimensionName)
	{
		return String.format("%s@%s",levelName,dimensionName);
	}
	
	private void parseLevelValues(String levelValue,String levelName, Set<LevelValue> levelValues) 
	{
		LimitConfigInfo.LevelDataType dataType = configInfo.identifyLevelDataType(levelName);
		Object tokenValue = getValue(dataType,levelValue);		
		levelValues.add(new LevelValue(levelName,tokenValue));
		
	}
	
	private Object getValue(LimitConfigInfo.LevelDataType dataType, Object value)
	{
		return dataType.getValue(parser, value.toString());		
	}
	
	private void checkForCombineLimit(LimitDetail limit,Set<String> combineLimitLevels, String levelName,Set<LevelValue> levelValues) {
		if(levelValues.size() > 1)
		{
			//Combine limit level.
			combineLimitLevels.add(levelName);
			limit.setCombineLimit(true);
		}
	}
	 
	private Object getCacheValue(Object value)
	{
		Object oldValue = cache.putIfAbsent(value, value);
		if(oldValue!=null)
			return oldValue;			
		return value;	
	}
	
	private static class LevelValue
	{
		private String levelName;
		private Object levelValue;
		
		public LevelValue(String levelName, Object levelValue) {
			super();
			this.levelName = levelName;
			this.levelValue = levelValue;
		}
		
		public String getLevelName() {
			return levelName;
		}
		
		public Object getLevelValue() {
			return levelValue;
		}
		
		@Override
		public String toString()
		{
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("levelName",levelName)
			.append("levelValue",levelValue)
			.toString();	
		}
		
		@Override
		public int hashCode()
		{
			return new HashCodeBuilder().append(levelName).append(levelValue).toHashCode();
		}
		
		@Override
		public boolean equals(Object o)
		{
			if(o == null || !LevelValue.class.isInstance(o))
			{
				return false;
			}
			LevelValue rhs = (LevelValue)o;
			return new EqualsBuilder().append(levelName, rhs.levelName).append(levelValue, rhs.levelValue).isEquals();			
		}
		
	}	
}
