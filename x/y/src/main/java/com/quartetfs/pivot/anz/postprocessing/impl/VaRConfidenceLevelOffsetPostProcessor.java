package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.Arrays;
import java.util.Collection;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.anz.utils.ANZConstants;

/**
 * VaRConfidenceLevelOffsetPostProcessor provides the Scenario Name of the VaR for a given confidence level
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName="com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key=VaRConfidenceLevelOffsetPostProcessor.PLUGIN_KEY)
public class VaRConfidenceLevelOffsetPostProcessor extends
VaRConfidenceLevelPostProcessor {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 9023830714765513580L;
	public final static String PLUGIN_KEY = "VAR_CONFIDENCE_OFFSET";
	public VaRConfidenceLevelOffsetPostProcessor(String name, IActivePivot pivot) {
		super(name, pivot);

	}


	@Override
	protected Object getObjectToWrite(double[] vector, double confidenceLevel,ICellSet cellSet,int rowId) {
		Double var= (Double)super.getObjectToWrite(vector, confidenceLevel,cellSet,rowId);
		Object temp=cellSet.getCellValue(rowId, underlyingMeasures[1]);
		double [] rawVector=null;
		if (temp instanceof double[]){
			rawVector=(double[])temp;
			int index = VaRHelper.getIndexFromRawVector(rawVector, var);
			if (index >= 0)	index++;//scenario name starts from 1
			return "Scenario Nbr: "+index;
		}
		return ANZConstants.BLANK;
	}

	/* (non-Javadoc)
	 * @see com.quartetfs.biz.pivot.postprocessing.IPrefetcher#computeMeasures(java.util.Collection)
	 */
	@Override
	public Collection<String> computeMeasures(Collection<ILocation> locations) {
		return Arrays.asList(underlyingMeasures);
	}



}
