package com.quartetfs.pivot.anz.webservices.dto;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ExtractExcessParam {

	private String extractDate;
	private String containerName;
	private String snapShot;
	private String limitDate;
	
	public String getLimitDate() {
		return limitDate;
	}

	public void setLimitDate(String limitDate) {
		this.limitDate = limitDate;
	}

	public String getExtractDate() {
		return extractDate;
	}
	
	public void setExtractDate(String extractDate) {
		this.extractDate = extractDate;
	}	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
		.append("extractDate",extractDate).append("containerName",containerName ).append("snapShot",snapShot)
		.toString();
	}

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String getSnapShot() {
		return snapShot;
	}

	public void setSnapShot(String snapShot) {
		this.snapShot = snapShot;
	}	
	
	
}
