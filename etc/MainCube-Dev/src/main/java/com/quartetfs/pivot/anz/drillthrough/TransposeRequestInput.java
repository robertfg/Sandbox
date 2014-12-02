package com.quartetfs.pivot.anz.drillthrough;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.server.impl.PointKeyExtractor;
import com.quartetfs.pivot.anz.drillthrough.DrillThroughConstants.Headers;
import com.quartetfs.pivot.anz.drillthrough.impl.DrillThroughRowContent;
import com.quartetfs.tech.point.impl.Point;

public class TransposeRequestInput 
{
	
	private Map<Point, DrillThroughRowContent> transposedData;
	private PointKeyExtractor extractor;
	private IActivePivot pivot;	
	
	private DimensionDetails dimensionDetails;
	private FilterDetails filterDetails;
	private ColumnDetails columnDetails;
	
	
	public TransposeRequestInput(IActivePivot pivot, ColumnDetails columnDetails,
			DimensionDetails dimensionDetails,FilterDetails filterDetails,Map<Point, DrillThroughRowContent> transposedData) {
		super();
		this.pivot=pivot;		
		this.transposedData = transposedData;		
		this.dimensionDetails=dimensionDetails;
		this.filterDetails=filterDetails;
		this.columnDetails=columnDetails;
		this.extractor=new PointKeyExtractor(generateKeyIndex());
		
	}
	
	private List<String> generateKeyIndex()
	{
		List<String> indexForKey=new ArrayList<String>();
		int counter=0;
		for (String header:columnDetails.getHeaders())
		{
			if (isPartOfKey(header))
			{
				indexForKey.add(String.valueOf(counter));
			}
			else if(isPartOfAggregate(header))
			{
				columnDetails.getAggregateColumnIdx().add(counter);
			}
			counter++;			
		}
		return indexForKey;
	}

	private boolean isPartOfAggregate(String header) {
		return columnDetails.getAggregateFields().contains(header);
	}

	private boolean isPartOfKey(String header) 
	{
		return !columnDetails.getExcludeCols().contains(header) && !isPartOfAggregate(header);
	}
	
	public PointKeyExtractor getExtractor() {
		return extractor;
	}
	
	public Map<Point, DrillThroughRowContent> getTransposedData() {
		return transposedData;
	}
	
	
	public IActivePivot getPivot() {
		return pivot;
	}	
	
	private boolean hasTermFilter()
	{
		return StringUtils.isNotBlank(filterDetails.getTermFilter());
	}
	
	private boolean hasUnderlyingTermFilter()
	{
		return StringUtils.isNotBlank(filterDetails.getUnderlyingtermFilter());
	}
	
	public boolean isAllowedTerm(String term)
	{
		return hasTermFilter()  ? filterDetails.getTermFilter().equalsIgnoreCase(term) : true;
	}
	
	public boolean isAllowedUnderlyingTerm(String term)
	{
		return hasUnderlyingTermFilter() ? filterDetails.getUnderlyingtermFilter().equalsIgnoreCase(term) : true;
	}
	
	
	public DimensionDetails getDimensionDetails() {
		return dimensionDetails;
	}
	
	public FilterDetails getFilterDetails() {
		return filterDetails;
	}
	
	public ColumnDetails getColumnDetails() {
		return columnDetails;
	}
	
	public static class DimensionDetails
	{
		private IDimension termDimension;
		private IDimension underlyingTermDimension;
		public DimensionDetails(IDimension termDimension,IDimension underlyingTermDimension) 
		{
			super();
			this.termDimension = termDimension;
			this.underlyingTermDimension = underlyingTermDimension;
		}
		
		
		public IDimension getTermDimension() {
			return termDimension;
		}
		
		public IDimension getUnderlyingTermDimension() {
			return underlyingTermDimension;
		}
	}
	
	public static class FilterDetails
	{
		private String underlyingtermFilter;
		private String termFilter;
		public FilterDetails(String underlyingtermFilter, String termFilter) {
			super();
			this.underlyingtermFilter = underlyingtermFilter;
			this.termFilter = termFilter;
		}
		
		public String getUnderlyingtermFilter() {
			return underlyingtermFilter;
		}
		
		public String getTermFilter() {
			return termFilter;
		}		
	}
	
	public static class ColumnDetails
	{
		private List<String> headers;
		private Set<String> excludeCols; 
		private Map<Headers,Integer> headerIdx;
		private List<Integer> aggregateColumnIdx=new ArrayList<Integer>();
		private Set<String> aggregateFields;
		
		public ColumnDetails(List<String> headers, Set<String> excludeCols,
				Map<Headers, Integer> headerIdx,Set<String> aggregateFields) {
			super();
			this.headers = headers;
			this.excludeCols = excludeCols;
			this.headerIdx = headerIdx;			
			this.aggregateFields = aggregateFields;
		}
		
		public List<Integer> getAggregateColumnIdx() {
			return aggregateColumnIdx;
		}
		
		public Set<String> getAggregateFields() {
			return aggregateFields;
		}
		
		public Set<String> getExcludeCols() {
			return excludeCols;
		}
		
		public Map<Headers, Integer> getHeaderIdx() {
			return headerIdx;
		}
		
		public List<String> getHeaders() {
			return headers;
		}
		
	}
}
