package com.anz.rer.etl.utils;

import java.util.List;

public class CsvObject {

	private boolean done;
	private List<Object[]> rows;
    private String name;
    
    private int totalLineNumber;
	private int totalPartition;
	private int tranCtr;
	
	public CsvObject() {
	}
	
	public CsvObject(boolean done, List<Object[]> row) {
		this.done = done;
		this.rows = row;
		
	} 

	public void setRows(List<Object[]> rows) {
		this.rows = rows;
	}

	
	
	public CsvObject(List<Object[]> row) {
		super();
		this.rows = row;
	}
	
	public CsvObject(boolean done) {
		this.done = done;		
	}
	
	public boolean isDone() {
		return done;
	}
	
	public List<Object[]> getRows() {
		return rows;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTotalLineNumber() {
		return totalLineNumber;
	}

	public void setTotalLineNumber(int totalLineNumber) {
		this.totalLineNumber = totalLineNumber;
	}

	public int getTotalPartition() {
		return totalPartition;
	}

	public void setTotalPartition(int totalPartition) {
		this.totalPartition = totalPartition;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public int getTranCtr() {
		return tranCtr;
	}

	public void setTranCtr(int tranCtr) {
		this.tranCtr = tranCtr;
	}
}
