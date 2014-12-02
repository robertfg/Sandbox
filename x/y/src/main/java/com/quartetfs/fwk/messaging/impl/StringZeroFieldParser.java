/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.fwk.messaging.impl;

import com.quartetfs.fwk.QuartetPluginValue;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.types.impl.PluginValue;

/**
 * 
 * Parser for string fields.
 * 
 * @author Quartet Financial Systems
 *
 */
@QuartetPluginValue(interfaceName = "com.quartetfs.fwk.format.IParser")
public class StringZeroFieldParser extends PluginValue implements IParser<String> {

	/** serialVersionUID */
	private static final long serialVersionUID = 8622580548346340052L;

	@Override
	public String description() { return "Parser for string fields even with length zero"; }

	@Override
	public Object key() { return "stringZero"; }

	@Override
	public String parse(CharSequence sequence) throws QuartetRuntimeException {
		if (sequence==null || sequence.length() == 0)
			return null;
		else 
			return sequence.toString();
	}
}
