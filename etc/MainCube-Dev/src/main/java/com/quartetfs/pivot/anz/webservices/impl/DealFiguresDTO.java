package com.quartetfs.pivot.anz.webservices.impl;



public class DealFiguresDTO {
	
	private double mresult;
	private double mresultv;
	
	private double[] oneDayVar;
	private double[] tenDayVar;
	
	private String cobDate;
	
	public double getMresult() {
		return mresult;
	}
	public void setMresult(double mresult) {
		this.mresult = mresult;
	}
	public double getMresultv() {
		return mresultv;
	}
	public void setMresultv(double mresultv) {
		this.mresultv = mresultv;
	}
	public double[] getOneDayVar() {
		return oneDayVar;
	}
	public void setOneDayVar(double[] oneDayVar) {
		this.oneDayVar = oneDayVar;
	}
	public double[] getTenDayVar() {
		return tenDayVar;
	}
	public void setTenDayVar(double[] tenDayVar) {
		this.tenDayVar = tenDayVar;
	}
	public void setCobDate(String cobDate) {
		this.cobDate = cobDate;
	}
	public String getCobDate() {
		return cobDate;
	}
	
	
	
	
	
}
