package com.quartetfs.pivot.anz.comparator.impl;

import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.ordering.IComparator;
import com.quartetfs.pivot.anz.utils.ANZConstants;


@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.fwk.ordering.IComparator", key = NumberPercentSignComaparator.KEY)
public class NumberPercentSignComaparator implements IComparator<Object> {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = -136934196396186422L;
	public static final String KEY="numberPercentComp";
	private final static String percent="%";
	
	@Override
	public String getType() {
		return KEY;
	}

	private Double getDouble(String value){
		if (value.equalsIgnoreCase(ANZConstants.DEFAULT_DISCRIMINATOR)){
			return Double.NEGATIVE_INFINITY;
		}
		
		Double retValue=0.0d;
		try{
			int index=value.indexOf(percent);
			if(index==-1){
				index=value.indexOf("bp");
			}
			value=value.substring(0,index>0?index:value.length());
			retValue=Double.parseDouble(value);
		}catch(NumberFormatException nfe){
			
			
		}
		return retValue;
	}
	
	@Override
	public int compare(Object o1, Object o2) {
		
		Double d1=getDouble(o1.toString());
		Double d2=getDouble(o2.toString());
		return d1.compareTo(d2);
	}

}
