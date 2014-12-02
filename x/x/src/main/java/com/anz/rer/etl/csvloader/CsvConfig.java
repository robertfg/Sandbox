package com.anz.rer.etl.csvloader;

public class CsvConfig {

	private String columnName;
	private int maxLength;
	private int columnOrder;
	private String columnType;
	private int csvColumnNumber;
	private String defValue;
	private String format;
	 
	
	
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

	public void setDefValue(String defValue) {
		this.defValue = defValue;
	}

	public String getDefValue() {
		return defValue;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getFormat() {
		return format;
	}
}