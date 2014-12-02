package com.quartetfs.pivot.anz.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.quartetfs.biz.types.IDate;
/**
 * This class keeps labels for vectorized psr the key is IDate and container Name 
 *
 */
public class VectorLabelService {

	private ConcurrentMap<VectorLabelKey, List<String>> keyToLabel=new ConcurrentHashMap<VectorLabelKey, List<String>>();
	private ConcurrentMap<String, LinkedHashSet<String>> containerToLabel=new ConcurrentHashMap<String, LinkedHashSet<String>>();
	
	public List<String> get(IDate date,String container){
			
		List<String> emptyList=Collections.emptyList();
			if (date!=null && container!=null){
				return keyToLabel.get(new VectorLabelKey(date, container));
			}
			if (container!=null){
				Set<String> result=getAll(container);
				if(result==null) return new ArrayList<String>();
				return new ArrayList<String>(result);
			}
			return emptyList;
	}
	
	private Set<String> getAll(String containerName){
		return containerToLabel.get(containerName);
	}
		
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(keyToLabel).toString();
	}
	
	public boolean isLabelLoaded(IDate date,String container){
		return keyToLabel.containsKey(new VectorLabelKey(date, container));
	}
	
	public void put(IDate date, String container,List<String>values){
		keyToLabel.put(new VectorLabelKey(date, container), values);
		containerToLabel.put(container,new LinkedHashSet<String>(combineAllLabel(container))); //changes here must add up all label
	}
	
	private List<String> combineAllLabel(String container){
		List<String> labels = new ArrayList<String>();
		
		for (Map.Entry<VectorLabelKey, List<String>> entry : keyToLabel.entrySet())
		{   
			VectorLabelKey vLabel = entry.getKey();
			 if(vLabel.getContainer().equals(container)){
	              labels.addAll(entry.getValue());	 
			 }
		}
		return labels;
	}
	
	static class VectorLabelKey{
		private IDate date;
		private String container;
		public VectorLabelKey(IDate date, String container) {
			this.date=date;
			this.container=container;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj==null || !(obj instanceof VectorLabelKey))
				return false;
			VectorLabelKey other=(VectorLabelKey)obj;
			return new EqualsBuilder().append(date, other.date).
			append(container, other.container).isEquals();
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37).append(date).append(container).toHashCode();
		}
		
		@Override
		public String toString() {
			return String.format("[Date: %s, Container %s]",date,container);
		}

		public IDate getDate() {
			return date;
		}

		public void setDate(IDate date) {
			this.date = date;
		}

		public String getContainer() {
			return container;
		}

		public void setContainer(String container) {
			this.container = container;
		}
	}
}
