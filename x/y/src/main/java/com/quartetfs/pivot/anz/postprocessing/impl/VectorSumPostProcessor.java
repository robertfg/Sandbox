/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.postprocessing.impl;

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
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = VectorSumPostProcessor.PLUGIN_KEY)
public class VectorSumPostProcessor extends ABasicPostProcessor<Object> {
	private static final long serialVersionUID = -2542930616060389612L;

	public static final String PLUGIN_KEY = "VECTOR_SUM";

	public VectorSumPostProcessor(final String name, final IActivePivot pivot) {
		super(name, pivot);
	}

	@Override
	protected Object doEvaluation(final ILocation location,
			final Object[] underlyingMeasures) throws QuartetException {
		
		double sum  = 0.0;
		
		if (underlyingMeasures[0] instanceof double[]) {
			final double[] value = (double[]) underlyingMeasures[0];
			for (int i = 0; i < value.length; i++) {
				sum+=value[i];
			}
		}
		return sum;
	}

	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

}
