package com.quartetfs.pivot.anz.limits.service;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.TIntSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;
import com.quartetfs.pivot.anz.limits.bo.LimitMasterData;
import com.quartetfs.pivot.anz.limits.bo.LimitMasterData.LimitDataHolder;
import com.quartetfs.pivot.anz.utils.ANZUtils;
import com.quartetfs.pivot.anz.utils.QueryHelper;

public class LimitUtil {
	private static final Logger LOGGER = Logger.getLogger(LimitUtil.class.getSimpleName());

	private LimitMasterData limitMasterData;
	private QueryHelper helper;
	private DateParser parser;
	private List<String> defaultMember = null;
	
	public LimitUtil(LimitMasterData limitMasterData, QueryHelper helper) {
		super();
		this.limitMasterData = limitMasterData;
		this.helper = helper;
		this.parser =(DateParser) Registry.getPlugin(IParser.class).valueOf("date[dd/MM/yy]");
		this.setDefaultMember(helper.retrieveDefaultMembers());
		this.printCubeDefaultMember();
	}
	
	public LimitUtil() {
	}
	
	public QueryHelper getHelper() {
		return helper;
	}

	private int varConfidenceLevelIndex=-1;
	
	//To use APCube library to find out VarConfidenceIndex dimension position.
	public int findVarConfidenceLevelIndex()
	{
		for(IDimension dim : helper.getPivot().getDimensions())
		{
			//find var confidence level
			if (dim.getName().trim().equalsIgnoreCase("VaR-Confidence Level"))	{			
				return dim.getOrdinal()-1;
			}
		}	
		return -1;
	}
	
	public IDimension getDimenion(String dimensionName){
 	   return ANZUtils.findDimensionByName( helper.getPivot() , dimensionName); 
	}
	
	public int getDimensionOrdinalPosition(String dimensionName){
		 return ANZUtils.findDimensionByName( helper.getPivot() , dimensionName).getOrdinal() - 1;
	}
	
	public ILocation updateLocationValue(ILocation location, List<Map<String,Object>> dimToOverRides){
	
		Object[][] overriddenLocationArray = location.arrayCopy();
		for (Map<String, Object> dimToOverRide : dimToOverRides) {
			for(Map.Entry<String,Object> dimension : dimToOverRide.entrySet())		{
				IDimension dim = ANZUtils.findDimensionByName( helper.getPivot() , dimension.getKey());
				int dimIdx = dim.getOrdinal() - 1;
				boolean allMem =  Boolean.parseBoolean( (String) dim.getProperties().get("IsAllMembersEnabled") );
					if(allMem){
						overriddenLocationArray[dimIdx][1] = dimension.getValue();	
					}else{
						overriddenLocationArray[dimIdx][0] = dimension.getValue();
					}
			}
		}
		return new Location(overriddenLocationArray);
	}
	
	public ILocation rebuildLocation(ILocation location){
		Object[][] overriddenLocationArray = location.arrayCopy();
		
		int varIdx = this.getDimensionOrdinalPosition("VaR-Confidence Level"); //findVarConfidenceLevelIndex();
		int cobIdx = this.getDimensionOrdinalPosition("COB Date");
		
		Location dateLoc=null;
		
		
		if(overriddenLocationArray[cobIdx][0]!=null){	  
			Object[][] locationArray = location.arrayCopy();	
			
			Object[]dateArray=locationArray[cobIdx];				
		
		//	IDate limitDate = Registry.create(IDate.class, parser.parse((String) dateArray[0]).getTime());
			IDate limitDate = Registry.create(IDate.class, parser.parse((String) dateArray[0]).getTime());
			
			overriddenLocationArray[cobIdx][0] = limitDate;		
			dateLoc= new Location(overriddenLocationArray);
			
			
		}		
		
		if (varIdx > 0){
			try{
				if(overriddenLocationArray[varIdx][0] == null ) {
					if (dateLoc!=null){
						return dateLoc;
					}	else {
						return location;
					}
				}			
				double newVal = Double.parseDouble((String)overriddenLocationArray[varIdx][0]);
				overriddenLocationArray[varIdx][0] = newVal;
			} catch(Exception e)	{
				e.printStackTrace();
			}
		}
		
		return new Location(overriddenLocationArray);
	}
	
	public ILocation rebuildLocation(ILocation location, String exposureDate){
		Object[][] overriddenLocationArray = location.arrayCopy();
		
		int varIdx = this.getDimensionOrdinalPosition("VaR-Confidence Level"); //findVarConfidenceLevelIndex();
		int cobIdx = this.getDimensionOrdinalPosition("COB Date");
		
		Location dateLoc=null;
		
		
		if(overriddenLocationArray[cobIdx][0]!=null){	  
			IDate limitDate = Registry.create(IDate.class, parser.parse(exposureDate).getTime());
			overriddenLocationArray[cobIdx][0] = limitDate;		
			dateLoc= new Location(overriddenLocationArray);
		}		
		
		if (varIdx > 0){
			try{
				if(overriddenLocationArray[varIdx][0] == null ) {
					if (dateLoc!=null){
						return dateLoc;
					}	else {
						return location;
					}
				}			
				double newVal = Double.parseDouble((String)overriddenLocationArray[varIdx][0]);
				overriddenLocationArray[varIdx][0] = newVal;
			} catch(Exception e)	{
				e.printStackTrace();
			}
		}
		
		return new Location(overriddenLocationArray);
	}
	
	public List<LimitDetail> findLimits(IDate limitDate,final boolean combineLimit)
	{
		Validate.notNull(limitMasterData, "Reference to limit master data is not set");
		Validate.notNull(helper, "Reference to query helper is not set");
		
		LimitDataHolder holder = limitMasterData.getLimits().get(limitDate);
		if(holder == null) return Collections.emptyList();
		
		List<LimitDetail> limits = new ArrayList<LimitDetail>();
		for(LimitDetail limit : holder.getLimitDetails())
		{	
			limits.add(limit);		
		}
		return limits;		
	}
	
	public List<LimitDetail> findLimits(IDate limitDate, String containerName)
	{
		Validate.notNull(limitMasterData, "Reference to limit master data is not set");
		Validate.notNull(helper, "Reference to query helper is not set");
		
		LimitDataHolder holder = limitMasterData.getLimits().get(limitDate);
		if(holder == null) return Collections.emptyList();
		
		List<LimitDetail> limits = new ArrayList<LimitDetail>();
		for(LimitDetail limit : holder.getLimitDetails())
		{	
			if(limit.getLocationAttributes().contains(containerName)){
				limits.add(limit);		
			}
		}
		return limits;		
	}
	
	
	public ILocation computeLocation(TIntObjectMap<TIntObjectMap<Object>> locationParams) 
	{
		final Map<String,Object> queryParams = new HashMap<String,Object>();
		
		locationParams.forEachEntry( new TIntObjectProcedure<TIntObjectMap<Object>>() {			
			@Override
			public boolean execute(int dimensionIndex, TIntObjectMap<Object> levels) {
				
				final Map<Integer, String> levelIndexMap = helper.getDimensionIndexCache().get(dimensionIndex+1);
				levels.forEachEntry( new TIntObjectProcedure<Object>() {
					
					@Override
					public boolean execute(int levelIndex, Object value) {
						String levelFQN = levelIndexMap.get(levelIndex);					
						queryParams.put(levelFQN, value);
						return true;
					}
				});
				return true;
			}
		});
		return helper.computeLocation(queryParams);	
	}
	
	public LimitDetail matchLimitLocation(ILocation location, List<LimitDetail> limits)
	{
		for(LimitDetail limit : limits)
		{
			if(matchLimitLocations(location,limit)) return limit;
		}
		return null;
	}
	
	public boolean matchLimitLocations(final ILocation location, LimitDetail limit) 
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
				return true;				
			}
		}
		return false;
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
	
public String generateKeyFromLocation(ILocation location, List<String> defaultMembers){
		
		Object[][]loc = location.arrayCopy();
		StringBuilder str = new StringBuilder();
	  	 if(loc[0]==null){
	  		str.append( defaultMembers.get(0) );
	  	 } else{
	  		 Object[] value = loc[0];
			 if(value.length==2){
				 if(value[0]!=null ){
					 str.append( defaultMembers.get(0) );
				 } else {
					  str.append("AllMember\\");
					  str.append(value[1].toString());
				 }
			 } 
	  	}
		 for (int i  = 1; i  < loc.length; i ++) {
			 Object[] value = loc[i];
			 if(value[0] != null || value.length==1){
		  		 str.append("|");
		  		 if(value[0]==null){
		  		 str.append(defaultMembers.get(i));//
		  		 } else{
		  			str.append(value[0]);
		  		 }
		  	} else{
		  		if(value.length>1) {
		  			str.append("|AllMember\\");
		  			if(value[0]!=null){
			  		    str.append(value[0].toString());
			  			for (int j = 1; j < value.length; j++) {
				  			str.append("|").append(value[j].toString());	
						}
		  			}else{
		  				    str.append(value[1].toString());
				  			for (int j = 2; j < value.length; j++) {
					  			str.append("\\").append(value[j].toString());	
							}
		  			}
		  		} 
		  	}
		} 
		return str.toString();
	}
	
	public List<String> getDefaultMember() {
		if (defaultMember == null || defaultMember.contains("null") || defaultMember.contains(null) ) {
			defaultMember = helper.retrieveDefaultMembers();
		}
		return defaultMember;
	}

	public void setDefaultMember(List<String> defaultMember) {
		this.defaultMember = defaultMember;
	}

	public void printCubeDefaultMember() {

		List<String> defMembers = this.getDefaultMember();

		LOGGER.info("**********************************Cube Default Member value/Limit Extract Init***************************************");
		StringBuilder defaultMember = new StringBuilder();
		for (String defMember : defMembers) {
			defaultMember.append(defMember).append("|");
		}
		LOGGER.info("Default Member Data:" + defaultMember.toString());
		LOGGER.info("***********************************************************************************");

	}


}
