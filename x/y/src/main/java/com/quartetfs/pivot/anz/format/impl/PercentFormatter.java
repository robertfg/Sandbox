/*
 * (C) Quartet FS 2008
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.format.impl;

import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.format.impl.APatternFormatter;


/**
 * <code>PercentFormatter</code> use method <code>format(Object)</code> to
 * format double object to the value which you want. DecimalFormat is a concrete
 * subclass of PercentFormat that formats double numbers. We use DecimalFormat in
 * method format(Object) to format the values.
 * 
 * Use method <code>setPattern(String)</code> to set the pattern which you
 * want to the formatted value displayed like.
 * 
 * For example : Format a double value to get the percent, you can set the pattern like "0.0%",
 * the formatted double value will be displayed like "2.5%".
 * 
 * @author Quartet Financial Systems
 * 
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.fwk.format.IFormatter", key = PercentFormatter.TYPE)
public class PercentFormatter extends APatternFormatter {

	/**
	 * serial version uid for the class PercentFormatter.
	 */
	private static final long serialVersionUID = 6195223919588183103L;

	/** Extended plugin value type */
    public static final String TYPE = "PERCENT";

    /** Thread local simple percent format. * */
    protected final ThreadLocalPecentFormat percentFormat = new ThreadLocalPecentFormat();

	public PercentFormatter() { 
	   super();
	}
	
    public PercentFormatter(String pattern) { 
    	 super(pattern);
    }

	/**
	 * @param value
	 *            the double value which wanted to be formatted
	 * @return Object format the "Object value" using the DecimalFormat and its
	 *         format pattern.
	 * @see com.quartetfs.biz.mondrian.pivolap.formatting.IFormatter#
	 *      format(java.lang.Object)
	 */
	public String format(Object value) {
		return percentFormat.get().formatValue(value);
	}

	/**
	 * Returns a representation of the PercentFormatter class.
	 * 
	 * @return String.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Percent Formatter";
	}

	/**
	 * @return plugin type
	 */
	public String getType() { return TYPE; }
	
	/**
     * Thread local wrapped percent format.
     */
    protected final class ThreadLocalPecentFormat extends ThreadLocal<PercentFormat> {
        public PercentFormat initialValue() {
            if(pattern == null) {
                return new PercentFormat();
            } else {
                return new PercentFormat(pattern);
            }
        }
    }

}
