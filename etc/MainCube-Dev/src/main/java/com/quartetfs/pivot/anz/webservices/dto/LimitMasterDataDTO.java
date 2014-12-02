package com.quartetfs.pivot.anz.webservices.dto;

import java.util.List;


public class LimitMasterDataDTO {

	private List<LimitDTO> limits;
	private String measureName="M_RESULT.SUM";
	
	public List<LimitDTO> getLimits() {
		return limits;
	}
	
	public void setLimits(List<LimitDTO> limits) {
		this.limits = limits;
	}
	
	public void setMeasureName(String measureName) {
		this.measureName = measureName;
	}
	
	public String getMeasureName() {
		return measureName;
	}
		
}
