package com.quartetfs.pivot.anz.service;

import java.util.Collection;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.pivot.anz.staticdata.IStaticDataListener;

public interface IDateService extends IStaticDataListener {

	public IDate getPreviousDay(IDate date,IActivePivot pivot,int timeDimensionIdx);
	public Collection<IDate> getAll(IActivePivot pivot, int timeDimensionIdx);

	//public Collection<IDate> getHistoryDates();
	public Collection<IDate> getHistoryDates(String type);
	
	//public Collection<IDate> getHistoryDates(IDate startDate,int count);
	public Collection<IDate> getHistoryDates(IDate startDate,int count, String type);
	
	//public boolean isVaRDatesLoadedFor(IDate date);
	boolean isVaRDatesLoadedFor(IDate date, String type);
	
	
	public IDate retrieveDateByIndex(IDate cobDate, int index,String type);
	public String[] retrieveDates(IDate cobDate,String type);

	
	
	
//	public Collection<IDate> getStressHistoryDates();
//	public Collection<IDate> getStressHistoryDates(IDate startDate,int count);
	
	
	
	 
}
