/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.format.impl;

import com.quartetfs.fwk.format.impl.NumberFormat;

/**
 * Basic single threaded PercentFormat relying on an underlying decimal format.
 * 
 * @author Quartet Financial Systems
 * 
 */
public class PercentFormat extends NumberFormat {

	private static final long serialVersionUID = -939987199798052880L;

	protected PercentFormat() {
		super();
	}

	protected PercentFormat(final String pattern) {
		super(pattern);
	}

	@Override
	public String formatValue(final Object value) {
		try {
			return super.formatValue(value);
		} catch (final Exception e) {
			// we're formatting something not numerical
			// e.g. DEFAULT member of Analysys Dimension
			return value.toString();
		}
	}
}
