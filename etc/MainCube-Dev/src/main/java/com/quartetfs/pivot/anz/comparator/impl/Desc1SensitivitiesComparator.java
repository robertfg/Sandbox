/*
 * (C) Quartet FS 2007-2009
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.comparator.impl;

import java.util.List;

import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.ordering.IComparator;
import com.quartetfs.fwk.ordering.impl.CustomComparator;

/**
 * <b>Desc1SensitivitiesComparator</b><br/>
 * Allows to order the Desc1 value for the Sensitivities cube. <br/>
 * Values defined as first objects will be put first in the ordering followed by
 * int and float values
 * 
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.fwk.ordering.IComparator", key = Desc1SensitivitiesComparator.KEY)
public class Desc1SensitivitiesComparator implements IComparator<String> {
	private static final long serialVersionUID = 3875520433718524317L;
	/** attribute key for the class. */
	public static final String KEY = "Desc1SensitivitiesComparator";

	private final CustomComparator<String> customComparator;

	public Desc1SensitivitiesComparator() {
		customComparator = new CustomComparator<String>();
	}

	/**
	 * Compare the values possible for the Desc1 value in the Sensitivities
	 * cube.<br/>
	 * The final order of the values will be : String, int and float.<br/>
	 * If both values contain a percentage mark so are dealing with an int, or
	 * float value. If both value are int, or float, we just need to compare
	 * them and get the comparator value. If only one is an int, or a float, we
	 * will put int values first in the order.<br/>
	 * String values will be ordered according to a custom comparator
	 * initialized using a list of firstObjects.
	 * 
	 * @param object1
	 *            the first object to be compared.
	 * @param object2
	 *            the second object to be compared.
	 * @return a negative integer, zero, or a positive integer as the first
	 *         argument is less than, equal to, or greater than the second.
	 */
	@Override
	public int compare( String object1,  String object2) {
		if (object1.endsWith("%") && object2.endsWith("%")) {
			boolean ok1 = false;
			boolean ok2 = false;
			int retValue = -1;
			object1 = object1.replace(".00", "");
			object2 = object2.replace(".00", "");
			
			
			// Verify if we are dealing with int values
			Integer int1 = null;
			try {
				int1 = Integer.valueOf(object1.substring(0,
						object1.length() - 1));
				ok1 = true;
			} catch (final Exception e) {
				retValue = 1;
			}
			Integer int2 = null;
			try {
				int2 = Integer.valueOf(object2.substring(0,
						object2.length() - 1));
				ok2 = true;
			} catch (final Exception e) {
				retValue = -1;
			}
			if (ok1 && ok2) {
				return int1.compareTo(int2);
			} else if (ok1 || ok2) {
				return retValue;
			}

			ok1 = false;
			ok2 = false;
			retValue = -1;

			// Verify if we are dealing with float values
			Float float1 = null;
			try {
				float1 = Float.valueOf(object1.substring(0,
						object1.length() - 1));
				ok1 = true;
			} catch (final NumberFormatException e) {
				retValue = -1;
			}
			Float float2 = null;
			try {
				float2 = Float.valueOf(object2.substring(0,
						object2.length() - 1));
				ok2 = true;
			} catch (final NumberFormatException e) {
				retValue = 1;
			}
			if (ok1 && ok2) {
				return float1.compareTo(float2);
			} else if (ok1 || ok2) {
				return retValue;
			}
		}

		// We are dealing with string
		return customComparator.compare(object1, object2);
	}

	@Override
	public String getType() {
		return KEY;
	}

	public void setFirstObjects(final List<String> firstObjects) {
		customComparator.setFirstObjects(firstObjects);
	}

}
