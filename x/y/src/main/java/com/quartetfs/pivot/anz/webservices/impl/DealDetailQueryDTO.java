package com.quartetfs.pivot.anz.webservices.impl;


public class DealDetailQueryDTO {
	private int maxResults;
	private long dealNumber;
	private String[] cobDate;

	public void setCobDate(String[] ddMMyyyy) {
		this.cobDate = ddMMyyyy;
	}

	public String[] getCobDate() {
		return cobDate;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public long getDealNumber() {
		return dealNumber;
	}

	public void setDealNumber(long dealNumber) {
		this.dealNumber = dealNumber;
	}

}
