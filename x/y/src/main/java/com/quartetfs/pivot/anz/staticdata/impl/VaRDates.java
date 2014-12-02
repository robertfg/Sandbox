package com.quartetfs.pivot.anz.staticdata.impl;

import java.util.Set;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.pivot.anz.staticdata.IStaticData;

public class VaRDates implements IStaticData{

	private IDate date;
	private StaticDataType type;
	private Set<IDate> historyDates;
	
	public VaRDates(IDate date,StaticDataType type, Set<IDate>historyDates) {
		this.date=date;
		this.historyDates=historyDates;
		this.type=type;
	}
	
	public Set<IDate> getHistoryDates() {
		return historyDates;
	}

	@Override
	public IDate date() { 
		return date;
	}

	@Override
	public StaticDataType type() {
		return type;
	}
	
}
