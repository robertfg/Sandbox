package com.quartetfs.pivot.anz.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class CubeEvent {
	private String sourceDetail;
	private Date startTime;
	private Date endTime;
	private EventType type;
	private Calendar cal; 
	
	
	public CubeEvent(String sourceDetail, EventType type) {
		this.type=type;
		this.sourceDetail=type.prefixSource(sourceDetail);
		this.startTime=getTime();
	}
	
	public CubeEvent(String sourceDetail, EventType type,Calendar cal) {
		this.type=type;
		this.sourceDetail=type.prefixSource(sourceDetail);
		this.cal=cal;
		this.startTime=getTime();
	}
	
	public Date getKey() throws ParseException{
		SimpleDateFormat sdf=new SimpleDateFormat("ddMMyyyy");
		return sdf.parse(sdf.format(startTime));
	}
	
	private Date getTime(){
		return (cal!=null)?cal.getTime():new Date();
	}
	
	public void complete(){
		endTime=getTime();
	}
	
	public String getSourceDetail() {
		return sourceDetail;
	}
	
	public String getDisplayDetail(){
		int index=sourceDetail.indexOf(":");
		if (index==-1) {
			return sourceDetail;
		}else{
			return sourceDetail.substring(index+1);
		}
	}
		
	
	public Date getStartTime() {
		return startTime;
	}
	
	public Date getEndTime() {
		return endTime;
	}
	
	public EventType getType() {
		return type;
	}
	
}
