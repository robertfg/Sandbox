package com.anz.rer.etl.csvToTable;

public class CsvConfig {
	private String columnName;
	private int maxLength;
	private int columnOrder;
	private String columnType;
	private int csvColumnNumber;
	
	
	public void setColumnOrder(int columnOrder) {
		this.columnOrder = columnOrder;
	}

	public int getColumnOrder() {
		return columnOrder;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public void setCsvColumnNumber(int csvColumnNumber) {
		this.csvColumnNumber = csvColumnNumber;
	}

	public int getCsvColumnNumber() {
		return csvColumnNumber;
	}
}
