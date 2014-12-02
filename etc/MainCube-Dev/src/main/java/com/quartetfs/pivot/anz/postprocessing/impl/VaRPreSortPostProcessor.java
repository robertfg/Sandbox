/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.Arrays;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.postprocessing.impl.ABasicPostProcessor;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

/**
 * Sort the P&L Vector
 * 
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = VaRPreSortPostProcessor.PLUGIN_KEY)
public class VaRPreSortPostProcessor extends ABasicPostProcessor<Object> {
	private static final long serialVersionUID = -2542930616060389612L;

	public static final String PLUGIN_KEY = "SORT";

	public VaRPreSortPostProcessor(final String name, final IActivePivot pivot) {
		super(name, pivot);
	}

	@Override
	protected Object doEvaluation(final ILocation location,
			final Object[] underlyingMeasures) throws QuartetException {
		double[] cloned = null;
		if (underlyingMeasures[0] instanceof double[]) {
			final double[] value = (double[]) underlyingMeasures[0];
			cloned = value.clone();
			Arrays.sort(cloned);
		}
		return cloned;
	}

	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

}
