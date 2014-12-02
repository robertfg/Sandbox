package com.quartetfs.pivot.anz.staticdata.impl;


import com.quartetfs.pivot.anz.staticdata.IStaticDataParser;
import com.quartetfs.pivot.anz.utils.ANZConstants;

public enum StaticDataType {
	
	VARDATES("vardates",1,new DateStaticDataParser( ANZConstants.VECTOR_LENGTH)),
	VARSTRESSDATES("varStressDates",1,new DateStaticDataParser(ANZConstants.VECTOR_STRESS_LENGTH)),
	VARDATESUAT("vardatesUAT",1,new DateStaticDataParser( ANZConstants.VECTOR_LENGTH)),
	VARSTRESSDATESUAT("varStressDatesUAT",1,new DateStaticDataParser(ANZConstants.VECTOR_STRESS_LENGTH)),
	VARSIXYEARDATES( "varSixYearDates",   1,new DateStaticDataParser(ANZConstants.VAR_1540_VECTOR_LENGTH));
	
	
	
	
	 
	private String name;
	private int maxDepth;
	private IStaticDataParser parser=null;
	
	
	StaticDataType(String name,int depth){
		this.name=name;
		this.maxDepth=depth;
	}
	
	StaticDataType(String name,int depth,IStaticDataParser parser){
		this.name=name;
		this.maxDepth=depth;
		this.parser=parser;
	}

	
	public int getMaxDepth() {
		return maxDepth;
	}
	
	public IStaticDataParser getParser() {
		return parser;
	}
	
	public static StaticDataType fromString(String str){
		str = str.toUpperCase();
		for (StaticDataType dataType:values()){
		//	if (dataType.name .equalsIgnoreCase(str)){
			if (str.indexOf( dataType.name,0 ) !=-1  || dataType.name.equalsIgnoreCase(str)){
				return dataType;
			}
		}
		return null;
	}
}
