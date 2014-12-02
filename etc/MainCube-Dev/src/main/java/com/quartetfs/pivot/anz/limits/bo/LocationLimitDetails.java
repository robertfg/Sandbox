package com.quartetfs.pivot.anz.limits.bo;

import java.util.ArrayList;
import java.util.List;

import com.quartetfs.biz.pivot.ILocation;

public class LocationLimitDetails {

	private ILocation location;
	private List<LimitDetail> searchResult = new ArrayList<LimitDetail>();
	public LocationLimitDetails(ILocation location,List<LimitDetail> searchResult) 
	{
		super();
		this.location = location;
		this.searchResult = searchResult;
	}
	
	public ILocation getLocation() {
		return location;
	}
	
	public List<LimitDetail> getSearchResult() {
		return searchResult;
	}
}
