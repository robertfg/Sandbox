package com.anz.rer.etl.config;

public class CsvReaderTaskConfig {

	 private boolean skipFirstLine ;
	 private String  rowDelimeter    = "\\r\\n|\\r|\\n|\\n\\r";
	 private String  columnDelimeter = "\\," ;
	 private String  ignoreString    = "#";
	 private int     csvPartition    = 100000;
	 
	public boolean isSkipFirstLine() {
		return skipFirstLine;
	}
	public void setSkipFirstLine(boolean skipFirstLine) {
		this.skipFirstLine = skipFirstLine;
	}
	public String getRowDelimeter() {
		return rowDelimeter;
	}
	public void setRowDelimeter(String rowDelimeter) {
		this.rowDelimeter = rowDelimeter;
	}
	public String getColumnDelimeter() {
		return columnDelimeter;
	}
	public void setColumnDelimeter(String columnDelimeter) {
		this.columnDelimeter = columnDelimeter;
	}
	public String getIgnoreString() {
		return ignoreString;
	}
	public void setIgnoreString(String ignoreString) {
		this.ignoreString = ignoreString;
	}
	public int getCsvPartition() {
		return csvPartition;
	}
	public void setCsvPartition(int csvPartition) {
		this.csvPartition = csvPartition;
	}

	 
}
