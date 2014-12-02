package com.quartetfs.pivot.anz.comparator.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.ordering.impl.CustomComparator;
import com.quartetfs.pivot.anz.utils.ANZConstants;

@QuartetExtendedPluginValue(
		interfaceName="com.quartetfs.fwk.ordering.IComparator",
		key=DateComparator.key
)
public class DateComparator <T> extends  CustomComparator<T>{

	private static final long serialVersionUID = 1206L;
	public static final String key = "DateComparator";	
	
	
	public int compare(Object value1, Object value2) {
		// Handle null values
		if (value1 == null && value2 == null)
			return 0;
		else if (value1 == null)
			return +1;
		else if (value2 == null)
			return -1;
		
	 	Date date1 = getDate((String)value1);
       	Date date2 = getDate((String)value2);         	        	
       	return date1.compareTo(date2);     
	}	
	
	private Date getDate(String value){
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		
		Date retVal=null;
		
	
		if (value.equalsIgnoreCase(ANZConstants.DEFAULT_DISCRIMINATOR)){
			try {
				retVal=sdf.parse(String.valueOf( "01-DEC-9999" ));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{		
			try {
				String[] target= StringUtils.split(value, "-");
				String toDate=target[0]+"-"+target[1]+"-20"+target[2];
        		retVal=sdf.parse(String.valueOf( toDate ));		
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		return retVal;
	}
	
	
	
	
	@Override
	public void setFirstObjects(List<T> firstObjects) {
		super.setFirstObjects(firstObjects);
	}
	
	@Override
	public void setLastObjects(List<T> lastObjects)
	{
		super.setLastObjects(lastObjects);
	}

	
		
}