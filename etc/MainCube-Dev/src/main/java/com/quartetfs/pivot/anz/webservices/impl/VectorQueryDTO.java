package com.quartetfs.pivot.anz.webservices.impl;

import com.quartetfs.biz.pivot.query.IDrillthroughQuery;

public class VectorQueryDTO {

	private IDrillthroughQuery dtQuery;//used as wrapper to retriweve the locations & the contextValues
	private String containerName; // should be one of the vectorized container
	
	public void setDtQuery(IDrillthroughQuery dtQuery) {
		this.dtQuery = dtQuery;
	}
	public IDrillthroughQuery getDtQuery() {
		return dtQuery;
	}
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}
	public String getContainerName() {
		return containerName;
	}
}
