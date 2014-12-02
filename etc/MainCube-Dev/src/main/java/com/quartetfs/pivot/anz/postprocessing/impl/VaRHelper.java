/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.postprocessing.impl;


/**
 * Helper class for confidence level to index computation
 * 
 * @author Quartet Financial Systems
 */
public final class VaRHelper {
	private static final double CONFIDENCE_LEVEL_975 = 0.975;
	private static final double CONFIDENCE_LEVEL_025 = 0.025;
	private static final double CONFIDENCE_LEVEL_01 = 0.01;

	private VaRHelper() {

	}


	public static int getIndexFromRawVector(final double[] vector, final double value)
	{
		for (int i=0;i<vector.length;i++){
			if (vector[i]==value)
				return i;
		}

		return -1;
	}

	/**
	 * Converts confidence level into offset into vector Special case for
	 * Confidence level =100, return 0th element Confidence Level= 97.5 return
	 * upper bound for everything else return Lower bound.
	 * 
	 * @param vectorLength
	 * @param confidenceLevel
	 * @return
	 */
	public static int getIndexFromVectorLength(final int vectorLength, final double confidenceLevel) {
		if(vectorLength==500){
			if (confidenceLevel == 1){
				return 0;
			}else if(confidenceLevel == CONFIDENCE_LEVEL_975 || confidenceLevel ==CONFIDENCE_LEVEL_025){
				return (int) Math.ceil((vectorLength * (1 - confidenceLevel))) - 1;
			}else if (confidenceLevel == CONFIDENCE_LEVEL_01) {
				return (int) Math.floor((vectorLength * (1 - confidenceLevel)));
			}else{
				return (int) Math.floor((vectorLength * (1 - confidenceLevel))) - 1;
			}
		}else if(vectorLength==1540){
				if (confidenceLevel == 1){
					return 0;
				}else if(confidenceLevel == CONFIDENCE_LEVEL_975 || confidenceLevel ==CONFIDENCE_LEVEL_025){
					return (int) Math.ceil((vectorLength * (1 - confidenceLevel))) - 1;
				}else if (confidenceLevel == CONFIDENCE_LEVEL_01) {
					return (int) Math.floor((vectorLength * (1 - confidenceLevel)));
				}else{
					return (int) Math.floor((vectorLength * (1 - confidenceLevel))) - 1;
				}
			
		} else { //Arrays.asList(0d, 0.01d, 0.025d, 0.975d, 0.99d, 1d);
			if (confidenceLevel == 0){
				return 260;
			}else if(confidenceLevel == .01){
				return 258;
			}else if (confidenceLevel == .025) {
				return 254;
			}else if (confidenceLevel == .975) {
				return 6;
			}else if (confidenceLevel == .99) {
				return 2;
			}else{
				return 0;
			}
			
		}
	}

}
