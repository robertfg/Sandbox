package com.quartetfs.pivot.anz.drillthrough.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.quartetfs.biz.pivot.dto.DrillthroughRowDTO;
import com.quartetfs.biz.pivot.server.impl.PointKeyExtractor;
import com.quartetfs.pivot.anz.drillthrough.TransposeRequestInput;
import com.quartetfs.pivot.anz.webservices.impl.TransposeDrillThroughService;
import com.quartetfs.tech.point.impl.Point;

public class SwaptionSpreadTypeTranspose 
{
	private TransposeRequestInput transposeDTO;
	
	public SwaptionSpreadTypeTranspose(TransposeRequestInput transposeDTO) {
		super();
		this.transposeDTO = transposeDTO;
	}

	public void transpose(List<DrillthroughRowDTO> spreadTypeRows) 
	{

		List<String> keyIdx = getKeyIndexes(transposeDTO.getColumnDetails().getHeaders(),transposeDTO.getColumnDetails().getAggregateFields());
		PointKeyExtractor extractor = new PointKeyExtractor(keyIdx);
		Map<Point, DrillThroughRowContent> sawptionData = indexData(spreadTypeRows,extractor, transposeDTO.getColumnDetails().getAggregateColumnIdx());

		new IRSwaptionCalcHelper(transposeDTO.getPivot(), extractor, sawptionData,transposeDTO).calculate();
		aggregateRows(transposeDTO, sawptionData);

	}

	private void aggregateRows(TransposeRequestInput transposeDTO,Map<Point, DrillThroughRowContent> sawptionData) {
		
		PointKeyExtractor transposeKey = transposeDTO.getExtractor();
		List<Integer> aggregateColumnIdx = transposeDTO.getColumnDetails().getAggregateColumnIdx();
		int termColIndex = transposeDTO.getColumnDetails().getHeaders().indexOf(TransposeDrillThroughService.transposeColumn);
		int secnarioTermColIndex = transposeDTO.getColumnDetails().getHeaders().indexOf(TransposeDrillThroughService.ScenarioTermUnderlyingColumn);
		
		for (Map.Entry<Point, DrillThroughRowContent> transposedData : sawptionData.entrySet()) 
		{
			String termValue=transposedData.getValue().getContent()[termColIndex].toString();			
			String scenarioTermValue=transposedData.getValue().getContent()[secnarioTermColIndex].toString();
			if(!transposeDTO.isAllowedTerm(termValue) || !transposeDTO.isAllowedUnderlyingTerm(scenarioTermValue))
			{
				continue;
			}
			Point key = transposeKey.extractKey(transposedData.getValue().getContent());
			DrillThroughRowContent rowValues = transposeDTO.getTransposedData().get(key);

			if (rowValues == null) 
			{
				rowValues = new DrillThroughRowContent(transposedData.getValue().getContent(), aggregateColumnIdx);
				transposeDTO.getTransposedData().put(key, rowValues);
			} 
			else 
			{
				rowValues.aggregateOtherMeasures(transposedData.getValue().getContent());
			}
			rowValues.aggregateTransposeMeasure(transposedData.getValue().getTerms());
		}
	}

	private Map<Point, DrillThroughRowContent> indexData(List<DrillthroughRowDTO> spreadTypeRows,PointKeyExtractor extractor, List<Integer> aggregateColumnIdx) 
	{
		Map<Point, DrillThroughRowContent> sawptionData = new HashMap<Point, DrillThroughRowContent>();
		for (DrillthroughRowDTO row : spreadTypeRows) {
							
			Point key = extractor.extractKey(row.getContent());
			// add row into map
			DrillThroughRowContent rowValues = sawptionData.get(key);
			if (rowValues == null) {
				rowValues = new DrillThroughRowContent(row.getContent(), aggregateColumnIdx);
				sawptionData.put(key, rowValues);
			} else {
				rowValues.aggregateOtherMeasures(row.getContent());
			}
			
		}
		return sawptionData;
	}

	private static List<String> getKeyIndexes(List<String> headers,Set<String> excludeCols) 
	{
		List<String> indexForKey = new ArrayList<String>();
		int counter = 0;
		for (String header : headers) {
			if (!excludeCols.contains(header)) {
				indexForKey.add(String.valueOf(counter));
			}
			counter++;
		}
		return indexForKey;
	}

}
