package com.quartetfs.pivot.anz.drillthrough.impl;

import java.util.List;

import com.quartetfs.biz.pivot.dto.DrillthroughRowDTO;
import com.quartetfs.biz.pivot.server.impl.PointKeyExtractor;
import com.quartetfs.pivot.anz.drillthrough.TransposeRequestInput;
import com.quartetfs.pivot.anz.webservices.impl.TransposeDrillThroughService;
import com.quartetfs.tech.point.impl.Point;

public class NASpreadTypeTranspose  {

	private TransposeRequestInput transposeDTO;
	
	public NASpreadTypeTranspose(TransposeRequestInput transposeDTO) {
		super();
		this.transposeDTO = transposeDTO;
	}

	public void transpose(List<DrillthroughRowDTO> spreadTypeRows) 
	{
		List<Integer> aggregateColumnIdx = transposeDTO.getColumnDetails().getAggregateColumnIdx();
		PointKeyExtractor extractor = transposeDTO.getExtractor();

		// Find Header index
		int transposeColIndex = transposeDTO.getColumnDetails().getHeaders().indexOf(TransposeDrillThroughService.transposeColumn);
		int measureColIndex = transposeDTO.getColumnDetails().getHeaders().indexOf("M_RESULTV");
		int measureMresult = transposeDTO.getColumnDetails().getHeaders().indexOf("M_RESULT");
		
		for (DrillthroughRowDTO row : spreadTypeRows) {
			Object termValue = row.getContent()[transposeColIndex];
			
			if(!transposeDTO.isAllowedTerm(termValue.toString()))
			{
				continue;
			}
//			if(row.getContent()[3].equals("3461394") ){
//				System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
//			}else{
//				System.out.println(row.getContent()[3]);
//			}
			Object pnl = row.getContent()[measureMresult];
			Point key = extractor.extractKey(row.getContent());
			// add row into map
			DrillThroughRowContent rowValues = transposeDTO.getTransposedData().get(key);
			if (rowValues == null) {
				rowValues = new DrillThroughRowContent(row.getContent(), aggregateColumnIdx);
				transposeDTO.getTransposedData().put(key, rowValues);
			} else {
				rowValues.aggregateOtherMeasures(row.getContent());
			}
			rowValues.aggregateTransposeMeasure(termValue, pnl);
				
		}
		
	}

}
