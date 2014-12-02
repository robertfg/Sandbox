package com.quartetfs.pivot.anz.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.quartetfs.pivot.anz.utils.CubeEventKeeper.CubeEventCriteria;

public class CubeEventFormatter {
	private final String headerFormat="%1$-40s,%2$-10s,%3$-35s,%4$-35s,%5$10s\n";
	private final String headers[]={"Source Detail","Type","Start Time","End Time","Elapsed Time(ms)"};
	private SimpleDateFormat timeFormat=new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
	private StringBuilder temp = new StringBuilder();
	private CubeEventCriteria criteria;
	
	public CubeEventFormatter(CubeEventCriteria criteria){
		this.criteria=criteria;
	}
	
	private String getHeaders(){
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(headerFormat,headers));
		return sb.toString();
	}
	
	public CubeEventFormatter format(Date d ,List<CubeEvent>events){
		temp.append(getHeaders());
			for (CubeEvent event:events){
				if (criteria.apply(event)){
					temp.append(formatRow(event));
				}
			}
			return this;
	}
	
	
	
	public String complete(){
		String retValue=temp.toString();
		temp.delete(0, temp.length());
		return retValue;
	}
	
	private long getElapsedTime(Date start,Date end){
		if (start==null||(end==null)){
			return 0;
		}
		long elapsedMilli=end.getTime()-start.getTime();
		return elapsedMilli;
	}
	private String formatRow(CubeEvent event){
		StringBuilder sb=new StringBuilder();
		String endTime="N/A";
		if (event.getEndTime()!=null){
			endTime=timeFormat.format(event.getEndTime());
		}
		
		sb.append(String.format(headerFormat, event.getDisplayDetail(),event.getType().name(),timeFormat.format(event.getStartTime()),endTime,getElapsedTime(event.getStartTime(), event.getEndTime())));
		return sb.toString();
	}
	
}
	

