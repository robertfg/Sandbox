package com.quartetfs.pivot.anz.drillthrough;

import com.quartetfs.biz.pivot.dto.DrillthroughHeaderDTO;

public class DrillThroughConstants {

	private DrillThroughConstants() {
	}

	public enum Headers {
		TRANSPOSE, MEASURE, DEALNUMBER, SPREADTYPE, SCENARIOTERMUNDERLYING;
	}

	// Transpose Drill Through Service constants
	public static final String TRANSPOSE_COL = "transposeColumn";
	public static final String MEASURE_COL = "measure";
	public static final String DEAL_COL = "dealnumber";
	public static final String EXCLUDE_COL = "excludeColumn";

	public static final DrillthroughHeaderDTO DEFAULT_TOTAL_HEADER = new DrillthroughHeaderDTO(
			"M_RESULT_Total");
	public static final String DEFAULT_PREFIX = "M_RESULT_";

	public static void main(String[] args){
		System.out.println(  Headers.TRANSPOSE );
		
	}
}
