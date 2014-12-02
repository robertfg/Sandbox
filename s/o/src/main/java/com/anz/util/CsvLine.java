package com.anz.util;

public class CsvLine {
	
 public CsvLine(String data, String id) {
		super();
		this.data = data;
		this.id = id;
	}

 String data;
 String id;
 
public String getData() {
	return data;
}

public void setData(String data) {
	this.data = data;
}
 
}
