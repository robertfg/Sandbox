package com.quartetfs.pivot.anz.utils;

import java.util.List;
import java.util.Map;

public class VectorConfig {
	
	private Integer[] columnToVectorize = new Integer[0];
	private Integer[] columnKeyId;
	private Map<Integer,String> columnFormatting;
	private Map<Integer,String> removeTrailer;
	
	private Integer[] columnToExtract;
	private Integer[] mandatory;
    private List<Integer> columnToMerge;
	private String removeFromChar;  
	private String vectorDelimeter = "|";
	private String columnKeyDelimeter = "#";
	private String colMergeDelimeter  = "|";
	private String columnDelimeter  = ",";
	
	
	public Integer[] getColumnToVectorize() {
		return columnToVectorize;
	}

	public void setColumnToVectorize(Integer[] columnToVectorize) {
		this.columnToVectorize = columnToVectorize;
	}

	public Integer[] getColumnKeyId() {
		return columnKeyId;
	}

	public void setColumnKeyId(Integer[] columnKeyId) {
		this.columnKeyId = columnKeyId;
	}

	public String getVectorDelimeter() {
		return vectorDelimeter;
	}

	public void setVectorDelimeter(String vectorDelimeter) {
		this.vectorDelimeter = vectorDelimeter;
	}

	public String getColumnKeyDelimeter() {
		return columnKeyDelimeter;
	}

	public void setColumnKeyDelimeter(String columnKeyDelimeter) {
		this.columnKeyDelimeter = columnKeyDelimeter;
	}

	public Map<Integer, String> getColumnFormatting() {
		return columnFormatting;
	}

	public void setColumnFormatting(Map<Integer, String> columnFormatting) {
		this.columnFormatting = columnFormatting;
	}

	public Integer[] getColumnToExtract() {
		return columnToExtract;
	}

	public void setColumnToExtract(Integer[] columnToExtract) {
		this.columnToExtract = columnToExtract;
	}

	public Integer[] getMandatory() {
		return mandatory;
	}

	public void setMandatory(Integer[] mandatory) {
		this.mandatory = mandatory;
	}

	public String getRemoveFromChar() {
		return removeFromChar;
	}

	public void setRemoveFromChar(String removeFromChar) {
		this.removeFromChar = removeFromChar;
	}

	

	public String getColMergeDelimeter() {
		return colMergeDelimeter;
	}

	public void setColMergeDelimeter(String colMergeDelimeter) {
		this.colMergeDelimeter = colMergeDelimeter;
	}

	public List<Integer> getColumnToMerge() {
		return columnToMerge;
	}

	public void setColumnToMerge(List<Integer> columnToMerge) {
		this.columnToMerge = columnToMerge;
	}

	public Map<Integer,String> getRemoveTrailer() {
		return removeTrailer;
	}

	public void setRemoveTrailer(Map<Integer,String> removeTrailer) {
		this.removeTrailer = removeTrailer;
	}

	public String getColumnDelimeter() {
		return columnDelimeter;
	}

	public void setColumnDelimeter(String columnDelimeter) {
		this.columnDelimeter = columnDelimeter;
	}
	
	

}
