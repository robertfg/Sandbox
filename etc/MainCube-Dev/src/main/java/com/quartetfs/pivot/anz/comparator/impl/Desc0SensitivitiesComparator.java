package com.quartetfs.pivot.anz.comparator.impl;	

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;

import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.ordering.IComparator;
import com.quartetfs.fwk.ordering.impl.CustomComparator;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.TenorComparator;

/**
 *  Be-spoke comparator for DESC0 Values
 * @author Quartet Financial Systems
 *
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.fwk.ordering.IComparator", key = Desc0SensitivitiesComparator.KEY)
public class Desc0SensitivitiesComparator implements IComparator<String> {

	private static final long serialVersionUID = -1992176009151920641L;
	public static final String KEY="Desc0SensitivitiesComparator";
	private static final Pattern p=Pattern.compile("[A-Z|a-z|0-9]{3}");
	private CustomComparator<String> customComparator;
	private TenorComparator tenorComparator;
	public Desc0SensitivitiesComparator() {
		customComparator=new CustomComparator<String>();
		tenorComparator= new TenorComparator();
	}

	@Override
	public String getType() {
		return KEY;
	}


	private Desc0Value [] getDesc0Values(String...values)
	{
		Desc0Value result[]=new Desc0Value[values.length];
		int count=0;
		for (String value:values) {
			result[count++]=resolve(value);
		}
		return result;
	}

	/**
	 * This method tries to resolve value into one of the following in the order mentioned below. Therefore Number would appear before ALL MoneyNess and N/A will appearat last 
	 * 1. NUMBER
	 * 2. Special Literals "ALL MONEYNESS"
	 * 3. CURRENCY 
	 * 4.All other Values except N/A 
	 * 5. N/A 
	 * @param value
	 * @return
	 */
	private Desc0Value resolve(String value) {
		if (value==null || value.trim().length()==0){
			value=ANZConstants.UNAVAILABLE;
		}
		// check special cases
		if (value.trim().equalsIgnoreCase("ALL MONEYNESS")) {
			return new Desc0Value(value, ValueType.ALL_MONEYNESS);
		}else if (value.trim().equalsIgnoreCase(ANZConstants.UNAVAILABLE))
		{
			return new Desc0Value(value, ValueType.NA);
		}
		Desc0Value result=getTenor(value);

		// try if its number
		if (result==null) {
			result=getNumber(value);
		}
		//check for currency
		if (result==null) {
			result=getCurrency(value);
		}
		// else consider as noram string
		if (result==null) {
			result=new Desc0Value(value, ValueType.OTHERS);
		}
		return result;
	}
	
	private Desc0Value getTenor(String o1) {
		if(o1.length()==2 || o1.length()==3) {
			if( o1.toLowerCase().endsWith("m") || o1.toLowerCase().endsWith("d") || o1.toLowerCase().endsWith("y")  ){
				return new Desc0Value(o1, ValueType.TENOR  );
			}
		}
		return null;
		
	}

	private Desc0Value getCurrency(String o1) {
		Desc0Value result=(p.matcher(o1).matches())? new Desc0Value(o1, ValueType.CURRENCY):null;
		return result;
	}

	private Desc0Value getNumber(String value)
	{
		try {
			Number num=NumberUtils.createNumber(value.trim());
			return new Desc0Value(num,ValueType.NUMBER);
		} catch (NumberFormatException e) {
			return null;
		} catch(StringIndexOutOfBoundsException e){
			return null;
		} catch(Exception e){
			return null;
		}
	}

	@Override
	public int compare(String o1, String o2) {
		//if any value belongs to custom compartor delegate the task to comparator 
		if (customComparator.getFirstObjects().contains(o1) || customComparator.getFirstObjects().contains(o2)
				|| customComparator.getLastObjects().contains(o1) || customComparator.getLastObjects().contains(o2))
			return customComparator.compare(o1, o2);

		//else we need to apply custom logic to make sure numbers comes before string
		Desc0Value [] result=getDesc0Values(o1, o2);
		return result[0].compareTo(result[1]);
	}

	public void setFirstObjects(final List<String> firstObjects) {
		customComparator.setFirstObjects(firstObjects);
	}

	/**
	 * Represent Category of Data DESC0 field can hold. 
	 * 
	 *
	 */
	private  enum ValueType {
		NUMBER(1),ALL_MONEYNESS(2),TENOR(3),CURRENCY(5),OTHERS(4),NA(5);
		int weight;
		ValueType(int i)
		{
			weight=i;
		}

		public Integer getInteger() {
			return weight;
		}

	}
	/**
	 * 
	 * Helper class represent value in DESC0 column
	 *
	 */
	@SuppressWarnings("unchecked")
	class Desc0Value implements Comparable<Desc0Value>{

		private Object value;
		private ValueType type;
		public Desc0Value(Object value,ValueType type ) {
			this.value=value;
			this.type=type;
		}
		@Override  
		public int compareTo(Desc0Value other) {
			try{
				if (other==null){
					return -1;
				} else if(other.type.equals( ValueType.TENOR ) && type.equals(ValueType.TENOR)){
					return tenorComparator.compare( String.valueOf(value), String.valueOf(other.value) );
			
				} else if (other.type==type) {
					 return ((Comparable<Object>)value).compareTo((Comparable<Object>) other.value);
				}else
				{
					return type.getInteger().compareTo(other.type.getInteger());
				}
			} catch(Exception e){
				//LOGGER.info("Data not yet handled by CUBE: A=" + other.value + " - B:" + value );
			    //LOGGER.info( "Exception:" + e.getLocalizedMessage() );
			}
			return 1;
		}

	}
}
