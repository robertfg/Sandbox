package com.quartetfs.pivot.anz.drillthrough.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.quartetfs.biz.pivot.logging.impl.MessagesServer;

public class DrillThroughRowContent {

	private static Logger logger = Logger.getLogger(MessagesServer.LOGGER_NAME, MessagesServer.BUNDLE);
	private Object[] content;
	private Map<Object, Object> terms=new HashMap<Object, Object>();
	private List<Integer> otherMeasures;
	public DrillThroughRowContent(Object[] content, List<Integer> otherMeasures){
		this.content=content;
		this.otherMeasures=otherMeasures;
	}
	
	public Object getTransposeColValue(Object tenor) {
		return terms.get(tenor);
	}
	
	public Map<Object, Object> getTerms() {
		return terms;
	}

	public Object[] getContent(){
		return content;
	}
	
	public Object aggregateTransposeMeasure(Object key,Object value){
		double newValue = Double.parseDouble(value.toString());
		Double existingValue = (Double)terms.get(key);
		newValue+=existingValue==null ? 0 : existingValue;			
		return terms.put(key,newValue);
	}
	
	public void aggregateTransposeMeasure(Map<Object, Object> terms)
	{
		for(Map.Entry<Object, Object> entry : terms.entrySet())
		{
			aggregateTransposeMeasure(entry.getKey(), entry.getValue());
		}
	}
	
	public void aggregateOtherMeasures(Object[] otherContent)
	{
		for(int i:otherMeasures){
			content[i]=getDoubleValue(content,i) + getDoubleValue(otherContent,i) ;
		}
	}
	
	private double getDoubleValue(Object[] dataValue,int index)
	{
		try
		{
			Object val =dataValue[index];
			return parseDouble(val);
		}catch(NumberFormatException cs){
			logger.log(Level.SEVERE, String.format("Unable to parse measure index %s, Data %s", index,Arrays.toString(dataValue)), cs);
			throw  new RuntimeException(cs);
		}
	}

	private double parseDouble(Object val) {
		return val==null || StringUtils.isBlank(val.toString())?
				0d :
				Double.parseDouble(val.toString());
	}
}
