package com.quartetfs.fwk.messaging.impl;

import javolution.text.TypeFormat;

import com.quartetfs.fwk.QuartetPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.types.impl.PluginValue;


@QuartetPluginValue(interfaceName = "com.quartetfs.fwk.format.IParser")
public class DoubleZeroFieldParser extends PluginValue implements IParser<Double> {

	/** serialVersionUID */
	private static final long serialVersionUID = 8622580548346340052L;

	@Override
	public String description() { return "Parser for string fields even with length zero"; }

	@Override
	public Object key() { return "doubleZero"; }

	@Override
	public Double parse(CharSequence sequence) throws QuartetRuntimeException {
		if (sequence==null || sequence.length() == 0)
			return null;
		else 
			return TypeFormat.parseDouble(sequence);
	}
	
}
