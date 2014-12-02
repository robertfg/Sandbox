package com.quartetfs.fwk.messaging.impl;


import com.quartetfs.fwk.QuartetPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.types.impl.PluginValue;
import com.quartetfs.pivot.anz.utils.ANZUtils;
@QuartetPluginValue(interfaceName = "com.quartetfs.fwk.format.IParser")
public class StringVectorFieldParser extends PluginValue implements IParser<String[]>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3819895817289935976L;

	@Override
	public String description() {
		return "parse custom hierarchy for ANZ specific format";
	}

	@Override
	public Object key() {
		return "String[]";
	}

	@Override
	public String[] parse(CharSequence charsequence)throws QuartetRuntimeException {
		return ANZUtils.generateHierarchy(charsequence.toString());
	}

	
}
