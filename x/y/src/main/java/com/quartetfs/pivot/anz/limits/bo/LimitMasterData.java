package com.quartetfs.pivot.anz.limits.bo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.quartetfs.biz.types.IDate;

public class LimitMasterData 
{	
	private ConcurrentMap<IDate,LimitDataHolder> limits = new ConcurrentHashMap<IDate,LimitDataHolder>();
	
	public void add(IDate date , LimitDataHolder limitDataHolder)
	{
		limits.put(date, limitDataHolder);		
	}
	
	public Map<IDate, LimitDataHolder> getLimits() {
		return limits;
	}
	
	public static class LimitDataHolder
	{
		private ConcurrentMap<Object, Object> cache;
		private List<LimitDetail> limitDetails;
		private Set<String> measures;	
		
		public LimitDataHolder(ConcurrentMap<Object, Object> cache,List<LimitDetail> limitDetails,Set<String> measures) {
			super();
			this.cache = cache;
			this.limitDetails = limitDetails;
			this.measures = measures;
		}
		
		public ConcurrentMap<Object, Object> getCache() {
			return cache;
		}
		
		public List<LimitDetail> getLimitDetails() {
			return limitDetails;
		}
		
		public Set<String> getMeasures() {
			return measures;
		}
	}
}
