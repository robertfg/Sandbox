/*
 * (C) Quartet FS 2007-2009
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.source.impl;

import java.util.ArrayList;
import java.util.List;

import com.quartetfs.biz.pivot.transaction.IKeyExtractor;
import com.quartetfs.fwk.IProperty;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.impl.Property;

/**
 * @author Quartet Financial Systems
 */
public class KeyExtractor implements IKeyExtractor<Object> {

	/** Key property */
	protected static final IProperty property = new Property("Key");

	@Override
	public Object extractKey(Object inputObject) {
		try {
			return property.getValue(inputObject);
		} catch (QuartetException e) {
			throw new QuartetRuntimeException(e);
		}
	}

	@Override
	public List<Object> extractKeys(List<?> inputObjects) {
		final int inputSize = inputObjects.size();
		List<Object> keys = new ArrayList<Object>();
		
		try {
			for(int i = 0; i < inputSize; i++) {
				keys.add(property.getValue(inputObjects.get(i)));
			}
		} catch (QuartetException e) {
			throw new QuartetRuntimeException(e);
		}

		return keys;
	}

}
