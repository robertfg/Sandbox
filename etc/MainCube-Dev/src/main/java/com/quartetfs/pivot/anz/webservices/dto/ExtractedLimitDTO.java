package com.quartetfs.pivot.anz.webservices.dto;


public class ExtractedLimitDTO {

	public ExtractedLimitDTO(){}
	
	public ExtractedLimitDTO(String id, double amount) {
		super();
		this.id = id;
		this.amount = amount;
	}
	
	
	private String id;
	private double amount;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	
	
			
}
