package com.quartetfs.pivot.anz.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.biz.types.IDate.DatePart;
import com.quartetfs.pivot.anz.service.IDateService;
import com.quartetfs.pivot.anz.staticdata.impl.StaticDataType;
import com.quartetfs.pivot.anz.staticdata.impl.VaRDates;
public class DateServiceTest {

	private IDateService service;
	private IDate dateToQuery;
	private SimpleDateFormat dataformat=new SimpleDateFormat("dd/MM/yyyy");
	@Before
	public void setup() throws ParseException{
	/*	service=new DateService();
		
		Set<IDate> varDates=new LinkedHashSet<IDate>();
		IDate today=new com.quartetfs.biz.types.impl.Date();
		today.applyTime(0, 0, 0, 0);
		IDate prev=today.clone();
		for( int i=0;i<1000;i++){
			varDates.add(prev);
			prev=prev.clone().add(DatePart.DAY, -1);
		}
		VaRDates dates=new VaRDates(today, StaticDataType.VARDATES, varDates);
		service.onStaticDataCompleted(dates);
		
		
		dateToQuery=today;*/
	}
	
	
	
	
	@Test
	public void testGetHistoricalDatesForDate(){
		 
	/*	Collection<IDate> dates=service.getHistoryDates(dateToQuery, 500);
		assertNotNull(dates);
		StringBuilder csvDates=new StringBuilder();
		for (IDate dt:dates){
			csvDates.append(dataformat.format(dt.javaDate())+",");
		}
		System.out.println(csvDates);
		assertNotNull(csvDates);
		
		assertEquals(500,dates.size());*/
	}
	
	

}
