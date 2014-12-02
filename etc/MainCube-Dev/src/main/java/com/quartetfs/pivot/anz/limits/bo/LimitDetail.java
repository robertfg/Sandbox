package com.quartetfs.pivot.anz.limits.bo;

import gnu.trove.map.TIntObjectMap;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.quartetfs.biz.types.IDate;

public class LimitDetail {

	private String id;
	private IDate limitDate;
	private String measureName;	
	private String weight;
	private String calculationType;
	
	private boolean combineLimit;
	private String locationAttributes;
	private Set<String> combileLimitLevels;
	private List<TIntObjectMap<TIntObjectMap<Object>>> locationValues;
	
//	public LimitDetail(int id, IDate limitDate, String measureName,Double beginValue,Double endValue, String attributes ) {
//		super();
//		this.id = id;
//		this.limitDate = limitDate;
//		this.measureName = measureName;
//		this.beginValue = beginValue;
//		this.endValue   = endValue;
//		this.locationAttributes = attributes;
//	}
	
	public LimitDetail(String id, 
					   IDate limitDate,
					   String weight,
					   String measureName, 
					   String attributes,
					   String calculationType) {
		super();
		this.id = id;
		this.limitDate = limitDate;
		this.measureName = measureName;	
		this.locationAttributes = attributes;
		this.weight=weight;
		this.calculationType=calculationType;
	}
	
	public String getId() {
		return id;
	}
	
	public IDate getLimitDate() {
		return limitDate;
	}
	
	public String getMeasureName() {
		return measureName;
	}
	
	
	
	public boolean isCombineLimit() {
		return combineLimit;
	}
	
	public void setCombineLimit(boolean combineLimit) {
		this.combineLimit = combineLimit;
	}
	
	public String getLocationAttributes() {
		return locationAttributes;
	}
	
	public void setLocationAttributes(String locationAttributes) {
		this.locationAttributes = locationAttributes;
	}
	
	public void setCombileLimitLevels(Set<String> combileLimitLevels) {
		this.combileLimitLevels = combileLimitLevels;
	}
	public Set<String> getCombileLimitLevels() {
		return combileLimitLevels;
	}
	
	public void setLocationValues(
			List<TIntObjectMap<TIntObjectMap<Object>>> locationValues) {
		this.locationValues = locationValues;
	}
	
	public List<TIntObjectMap<TIntObjectMap<Object>>> getLocationValues() {
		return locationValues;
	}
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
		.append("id",id)
		.append("limitDate",limitDate)
		.append("measureName",measureName)		
		.append("combineLimit",combineLimit)
		.append("combileLimitLevels",combileLimitLevels)
		.append("weight",weight)
		.append("calculationType",calculationType)
		.toString();	
	}
	
		
	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getWeight() {
		return weight;
	}
	
	public void setCalculationType(String calculationType) {
		this.calculationType = calculationType;
	}

	public String getCalculationType() {
		return calculationType;
	}

	public static void main(String[] args){
     
		System.out.println(new Double(0.0));
		
	}



	
}
