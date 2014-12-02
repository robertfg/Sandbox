package com.anz.rer.etl.vectorizer;

import java.util.HashMap;
import java.util.Map;

public class VectorConfig {
	
	private Integer[] columnToVectorize;
	private Integer[] columnKeyId;
	private Map<Integer,String> columnFormatting;
	private Integer[] columnToExtract;
	private Integer[] mandatory;
    private Integer[] columnToMerge;
	private String removeFromChar;
	private String vectorDelimeter    = "|";
	private String columnKeyDelimeter = "#";
	private String colMergeDelimeter  = "|";
	private String columnDelimeter    = ",";
	private int vectorSize = 5;
	private Map<String,Integer> vectorMapping = new HashMap<String,Integer>();
	private int vectorKeyColum;
	
	
	
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

	public Integer[] getColumnToMerge() {
		return columnToMerge;
	}

	public void setColumnToMerge(Integer[] columnToMerge) {
		this.columnToMerge = columnToMerge;
	}

	public String getColMergeDelimeter() {
		return colMergeDelimeter;
	}

	public void setColMergeDelimeter(String colMergeDelimeter) {
		this.colMergeDelimeter = colMergeDelimeter;
	}

	public String getColumnDelimeter() {
		return columnDelimeter;
	}

	public void setColumnDelimeter(String columnDelimeter) {
		this.columnDelimeter = columnDelimeter;
	}

	public int getVectorSize() {
		return vectorSize;
	}

	public void setVectorSize(int vectorSize) {
		this.vectorSize = vectorSize;
	}

	public Map<String,Integer> getVectorMapping() {
		return vectorMapping;
	}

	public void setVectorMapping(Map<String,Integer> vectorMapping) {
		this.vectorMapping = vectorMapping;
	}

	public int getVectorKeyColum() {
		return vectorKeyColum;
	}

	public void setVectorKeyColum(int vectorKeyColum) {
		this.vectorKeyColum = vectorKeyColum;
	}
	
	public String getVectorHeader(){
		StringBuilder ret = new StringBuilder(500);
		for(Map.Entry<String,Integer> entry: vectorMapping.entrySet() ){
			ret.append(entry.getKey()).append( getVectorDelimeter() ) ;
		}
		
         return ret.substring(0,ret.length()-1);		
	}
	

}

	
	/*
	 * 
	 *    int[] colToVectorize, 
			int[] keyId, 
			String keyDelimeter, 
			String vectorDelimeter,
			int[]mandatory, 
			int[] colToExtract, 
			Map<Integer,String> formatting,  
			List<Integer> colToMerge,
			String colMergeDelimeter, 
			Map<Integer,String> removeTrailer, 
			String columnDelimeter, 
			String removeFromChar 
	 * 
	 * */


