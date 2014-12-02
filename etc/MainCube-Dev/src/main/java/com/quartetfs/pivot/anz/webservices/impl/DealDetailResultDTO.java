package com.quartetfs.pivot.anz.webservices.impl;

import java.util.Collections;
import java.util.List;

public class DealDetailResultDTO {
	
	private long    dealNumber;
	private int     noOfReturnRecords;
	
	
	private List<DealFiguresDTO> deals = Collections.emptyList();
	
	public void setDealNumber(long dealNumber) {
		this.dealNumber = dealNumber;
	}
	
	public long getDealNumber() {
		return dealNumber;
	}

	public void setDeals(List<DealFiguresDTO> deals) {
		this.deals = deals;
	}

	public List<DealFiguresDTO> getDeals() {
		return deals;
	}

	public void setNoOfReturnRecords(int noOfReturnRecords) {
		this.noOfReturnRecords = noOfReturnRecords;
	}

	public int getNoOfReturnRecords() {
		return noOfReturnRecords;
	}
	
	
}
