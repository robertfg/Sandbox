package com.quartetfs.pivot.anz.limits.bo;

import java.util.HashSet;
import java.util.Set;

import org.springframework.jmx.export.annotation.ManagedResource;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;

@ManagedResource
public class LimitConfigInfo {

	private boolean debugLimits;
	private int limitMatcherThreads;
	private int matchTimeOutInSeconds;
	private int limitResolveThreads;
	private Set<String> dateCols   = new HashSet<String>();
	private Set<String> doubleCols = new HashSet<String>();
	private Set<String> longCols   = new HashSet<String>();
	
	private String extractDir;
	private String dimensionSeparator ="|";
	private String locationSeparator ="@:@";
	private String levelValueSeparator ="\\";	
	
	public void setLimitMatcherThreads(int limitMatcherThreads) {
		this.limitMatcherThreads = limitMatcherThreads;
	}
	
	public int getLimitMatcherThreads() {
		return limitMatcherThreads;
	}
	
	public boolean isDebugLimits() {
		return debugLimits;
	}
	
	public void setDebugLimits(boolean debugLimits) {
		this.debugLimits = debugLimits;
	}
	
	public void setMatchTimeOutInSeconds(int matchTimeOutInSeconds) {
		this.matchTimeOutInSeconds = matchTimeOutInSeconds;
	}
	public int getMatchTimeOutInSeconds() {
		return matchTimeOutInSeconds;
	}
	
	public int getLimitResolveThreads() {
		return limitResolveThreads;
	}
	
	public void setLimitResolveThreads(int limitResolveThreads) {
		this.limitResolveThreads = limitResolveThreads;
	}
	
	public void setDateCols(Set<String> dateCols) {
		this.dateCols = dateCols;
	}
	
	public void setDoubleCols(Set<String> doubleCols) {
		this.doubleCols = doubleCols;
	}
	
	public void setLongCols(Set<String> longCols) {
		this.longCols = longCols;
	}
	
	public boolean isDateType(String levelFQN)
	{
		return dateCols.contains(levelFQN);
	}
	
	public boolean isDoubleType(String levelFQN)
	{
		return doubleCols.contains(levelFQN);
	}
	
	public boolean isLongType(String levelFQN)
	{
		return longCols.contains(levelFQN);
	}
	
	public void setExtractDir(String extractDir) {
		this.extractDir = extractDir;
	}
	
	public String getExtractDir() {
		return extractDir;
	}

	public void setLocationSeparator(String dimensionLocationSeparator) {
		this.locationSeparator = dimensionLocationSeparator;
	}
	
	public void setDimensionSeparator(String dimensionSeparator) {
		this.dimensionSeparator = dimensionSeparator;
	}
	
	public void setLevelValueSeparator(String dimensionValueSeparator) {
		this.levelValueSeparator = dimensionValueSeparator;
	}
	
	public String getLocationSeparator() {
		return locationSeparator;
	}
	
	public String getDimensionSeparator() {
		return dimensionSeparator;
	}
	
	public String getLevelValueSeparator() {
		return levelValueSeparator;
	}
	
	public LevelDataType identifyLevelDataType(String levelName)
	{
		return isDateType(levelName) ? LevelDataType.DATE : 
			 isDoubleType(levelName) ? LevelDataType.DOUBLE :
			 isLongType(levelName) ? LevelDataType.LONG :
			 LevelDataType.STRING;
	}
		
	private interface ValueType
	{
		public Object getValue(DateParser parser,String value);
	}
	
	public enum LevelDataType implements ValueType
	{
		STRING,
		DATE
		{
			@Override
			public Object getValue(DateParser parser,String value) {
				return Registry.create(IDate.class, parser.parse(  value ).getTime());//parser.parse(value).getTime();
				
			}
		},
		DOUBLE 
		{
			@Override
			public Object getValue(DateParser parser,String value) {
				return Double.parseDouble(value);
			}
		},
		LONG
		{
			@Override
			public Object getValue(DateParser parser,String value) {
				return Long.parseLong(value);
			}
		};

		@Override
		public Object getValue(DateParser parser,String value) {
			return value;
		}
		
	}
	
}
