package com.quartetfs.pivot.anz.comparator.impl;

import java.util.ArrayList;
import java.util.List;

import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.impl.MessagesComposer;
import com.quartetfs.fwk.ordering.impl.CustomComparator;
import com.quartetfs.fwk.util.MessageUtil;
import com.quartetfs.pivot.anz.utils.ANZConstants;

@QuartetExtendedPluginValue(
		interfaceName="com.quartetfs.fwk.ordering.IComparator",
		key=AlphaNumReplaceComaparator.key
)
public class AlphaNumReplaceComaparator<T> extends  CustomComparator<T>
{
	private static final long serialVersionUID = 1205L;
	public static final String key = "AlphaNumReplaceComaparator";	
	
	protected List<String> replaceMentStrings =new ArrayList<String>();

//	/*
//	 * First part of code which looks up element from first & last object list is taken from CustomComparator
//	 *  
//	 * @see com.quartetfs.fwk.ordering.impl.CustomComparator#compare(java.lang.Object, java.lang.Object)
//	 * 
//	 */
//	@Override
//	public int compare(Object value1, Object value2) {
//	    // Handle null values
//        if (value1 == null && value2 == null)
//            return 0;
//        else if (value1 == null)
//            return +1;
//        else if (value2 == null)
//            return -1;
//     
//        if (firstObjectSet.contains(value1)) {
//            if (firstObjectSet.contains(value2)) {
//                // Both objects are in the first object list, just look at their
//                // position in the first object list
//                return firstObjects.indexOf(value1)
//                        - firstObjects.indexOf(value2);
//            } else {
//                // Only value1 is in the first object list, is comes first
//                return -1;
//            }
//        } else if (lastObjectSet.contains(value1)) {
//            if (lastObjectSet.contains(value2)) {
//                // Both objects are in the last object list, just look at their
//                // position in the last object list
//                return lastObjects.indexOf(value1)
//                        - lastObjects.indexOf(value2);
//            } else {
//                // Only value1 is in the last object list, is comes last
//                return +1;
//            }
//        } else if (firstObjectSet.contains(value2)) {
//            // Only value2 is in the first object list, is comes first
//            return +1;
//        } else if (lastObjectSet.contains(value2)) {
//            // Only value2 is in the last object list, is comes last
//            return -1;
//        } 
//        else
//        {
//
//        	Double d1=getDouble(value1.toString());
//    		Double d2=getDouble(value2.toString());
//    		return d1.compareTo(d2);
//        }
//    }
	
	/*
	 * First part of code which looks up element from first & last object list is taken from CustomComparator
	 *  
	 * @see com.quartetfs.fwk.ordering.impl.CustomComparator#compare(java.lang.Object, java.lang.Object)
	 * 
	 */
	public int compare(Object value1, Object value2) {
		// Handle null values
		if (value1 == null && value2 == null)
			return 0;
		else if (value1 == null)
			return +1;
		else if (value2 == null)
			return -1;

		Integer rank1 = index.get(value1);
		Integer rank2 = index.get(value2);

		if (rank1 != null) {
			if (rank2 != null) {
				return rank1 - rank2;
			} else {
				// Only value1 is indexed. If it is a "first object"
				// then its rank is negative, if it is a "last object"
				// then its rank is positive.
				return rank1;
			}
		} else if (rank2 != null) {
			// Only value2 is indexed. If it is a "first object"
			// then its rank is negative, if it is a "last object"
			// then its rank is positive.
			return -rank2;
		} else {
			// Neither value1 nor value2 are in any custom list, they
			// just get compared as Comparable java objects
			if (!(value1 instanceof Comparable))
				throw new IllegalArgumentException(MessageUtil.formMessage(
						MessagesComposer.BUNDLE, MessagesComposer.EXC_COMPARE, value1));
			if (!(value2 instanceof Comparable))
				throw new IllegalArgumentException(MessageUtil.formMessage(
						MessagesComposer.BUNDLE, MessagesComposer.EXC_COMPARE, value2));

			// Neither value1 nor value2 are in any custom list, they
			// just get compared as Comparable java objects           
        	Double d1=getDouble(value1.toString());
    		Double d2=getDouble(value2.toString());
    		return d1.compareTo(d2);
		}
	}	
	
	private Double getDouble(String value){
		if (value.equalsIgnoreCase(ANZConstants.DEFAULT_DISCRIMINATOR)){
			return Double.NEGATIVE_INFINITY;
		}
		
		Double retValue=0.0d;
		boolean q = false;
		try{
			for (String repStr : replaceMentStrings) {
			    if(repStr.equalsIgnoreCase("Q")){
			    	q = true;
			    }
				value =  value.replaceAll(repStr, "");	
			   
			}	
			
			if(q){
				value = value.substring(1) + value.substring(0,1);
			}
			retValue=Double.parseDouble(value.trim());
			
		}catch(NumberFormatException nfe){}
		return retValue;
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

	public List<String> getReplaceMentStrings() {
		return replaceMentStrings;
	}

	public void setReplaceMentStrings(List<String> replaceMentStrings) {
		this.replaceMentStrings = replaceMentStrings;
	}
		
}
