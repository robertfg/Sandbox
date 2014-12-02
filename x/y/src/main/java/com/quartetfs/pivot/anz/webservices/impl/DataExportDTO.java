package com.quartetfs.pivot.anz.webservices.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.util.CollectionUtils;

public class DataExportDTO {
	
	private String cobDate;
	private String containerName;
	private List<String> portfolio;
	private Map<String,String> fieldFilter;
	private String[] columnToExtract;
	private String jobId;
	private StringBuilder refDates;
	private boolean fromFM;
	
	
	public String getCobDate() {
		return cobDate;
	}
	public void setCobDate(String cobDate) {
		this.cobDate = cobDate;
	}
	public String getContainerName() {
		return containerName;
	}
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}
	public List<String> getPortfolio() {
		return portfolio;
	}
	public void setPortfolio(List<String> portfolio) {
		this.portfolio = portfolio;
	}
	
	public boolean hasPortfolio()
	{
		return !CollectionUtils.isEmpty(portfolio);
	}
	
	public boolean hasContainerName()
	{
		return containerName!=null;
	}
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
		.append("cobDate",cobDate)
		.append("containerName",containerName)
		.append("portfolio",portfolio)
		.toString();		
	}
	public void setFieldFilter(Map<String,String> fieldFilter) {
		this.fieldFilter = fieldFilter;
	}
	public Map<String,String> getFieldFilter() {
		return fieldFilter;
	}
	public void setColumnToExtract(String[] columnToExtract) {
		this.columnToExtract = columnToExtract;
	}
	public String[] getColumnToExtract() {
		return columnToExtract;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getJobId() {
		return jobId;
	}
	public StringBuilder getRefDates() {
		return refDates;
	}
	public void setRefDates(StringBuilder refDates) {
		this.refDates = refDates;
	}
	public boolean isFromFM() {  
		return fromFM;
	}
	public void setFromFM(boolean fromFM) {
		this.fromFM = fromFM;
	}	
}
