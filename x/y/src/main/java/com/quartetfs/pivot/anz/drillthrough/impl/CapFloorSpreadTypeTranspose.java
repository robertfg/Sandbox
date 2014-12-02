package com.quartetfs.pivot.anz.drillthrough.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.dto.DrillthroughRowDTO;
import com.quartetfs.biz.pivot.logging.impl.MessagesServer;
import com.quartetfs.biz.pivot.server.impl.PointKeyExtractor;
import com.quartetfs.pivot.anz.drillthrough.TransposeRequestInput;
import com.quartetfs.pivot.anz.utils.ANZUtils;
import com.quartetfs.pivot.anz.webservices.impl.TransposeDrillThroughService;
import com.quartetfs.tech.point.impl.Point;

public class CapFloorSpreadTypeTranspose 
{
	private static Logger logger = Logger.getLogger(MessagesServer.LOGGER_NAME, MessagesServer.BUNDLE);
	private List<String> termBuckets;
	private int termBucketSize;
	private TransposeRequestInput transposeDTO;
	
	public CapFloorSpreadTypeTranspose(TransposeRequestInput transposeDTO) {
		super();
		this.transposeDTO = transposeDTO;
		this.termBuckets =  ANZUtils.getSortedTermBucket(transposeDTO.getDimensionDetails().getTermDimension());
		this.termBucketSize=termBuckets.size();
	}

	public void transpose(List<DrillthroughRowDTO> spreadTypeRows) 
	{
		
		Set<String> excludeCols = new HashSet<String>(transposeDTO.getColumnDetails().getAggregateFields());
		excludeCols.add(TransposeDrillThroughService.ScenarioTermUnderlyingColumn);
		
		List<String> keyIdx = getKeyIndexes(transposeDTO.getColumnDetails().getHeaders(),excludeCols);
		PointKeyExtractor extractor = new PointKeyExtractor(keyIdx);
		Map<Point, DrillThroughRowContent> groupData = indexData(spreadTypeRows,extractor, transposeDTO.getColumnDetails().getAggregateColumnIdx());

		calculate(transposeDTO, extractor, groupData);
		aggregateRows(transposeDTO, groupData);

	}
	
	private void calculate(TransposeRequestInput transposeDTO,PointKeyExtractor extractor,Map<Point, DrillThroughRowContent> groupData)
	{
		int termColIndex = transposeDTO.getColumnDetails().getHeaders().indexOf(TransposeDrillThroughService.transposeColumn);
		int measureColIndex = transposeDTO.getColumnDetails().getHeaders().indexOf("M_RESULTV");
		
		
		
		for(Map.Entry<Point, DrillThroughRowContent> entry : groupData.entrySet())
		{
			DrillThroughRowContent row = entry.getValue();				
			String termValue=row.getContent()[termColIndex].toString();			
			
			if(!transposeDTO.isAllowedTerm(termValue)) continue;						
			
			
			double pnl=getMeasureValue(measureColIndex, row);					
			int termBucketIdx = termBuckets.indexOf(termValue);
			double value=pnl;
			
			//If this is last element then return same value;
			if(hasNextScenarioTerm(termBucketIdx))
			{
				Object[] rowData = row.getContent().clone();	
				Iterator<String> itr  = termBuckets.listIterator(termBucketIdx+1);
				while(itr.hasNext())
				{
					String nextTermBucket = itr.next();
					rowData[termColIndex] = nextTermBucket;
					
					Point key=extractor.extractKey(rowData);	
					DrillThroughRowContent rowContent = groupData.get(key);
					if(rowContent==null) continue;
					
					double nextValue=Double.valueOf(rowContent.getContent()[measureColIndex].toString());
					value = pnl;//-nextValue;
					break;
				}
			}
			row.aggregateTransposeMeasure(termValue, value);
			
		}
		
	}

	private double getMeasureValue(int measureColIndex,DrillThroughRowContent row) {
		try
		{
			return Double.valueOf(row.getContent()[measureColIndex].toString());
		}
		catch(NumberFormatException e)
		{
			logger.log(Level.SEVERE, String.format("Unable to parse measure index %s, Data %s", measureColIndex,Arrays.toString(row.getContent())), e);
			throw e;
		}		
	}

	private void aggregateRows(TransposeRequestInput transposeDTO,Map<Point, DrillThroughRowContent> capFloorData) {
		
		PointKeyExtractor transposeKey = transposeDTO.getExtractor();
		List<Integer> aggregateColumnIdx = transposeDTO.getColumnDetails().getAggregateColumnIdx();
		int transposeColIndex = transposeDTO.getColumnDetails().getHeaders().indexOf(TransposeDrillThroughService.transposeColumn);
		for (Map.Entry<Point, DrillThroughRowContent> transposedData : capFloorData.entrySet()) 
		{	
			Object termValue = transposedData.getValue().getContent()[transposeColIndex];
			if(!transposeDTO.isAllowedTerm(termValue.toString()))
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
		Map<Point, DrillThroughRowContent> rawData = new HashMap<Point, DrillThroughRowContent>();
		for (DrillthroughRowDTO row : spreadTypeRows) {

			Point key = extractor.extractKey(row.getContent());
			// add row into map
			DrillThroughRowContent rowValues = rawData.get(key);
			if (rowValues == null) {
				rowValues = new DrillThroughRowContent(row.getContent(), aggregateColumnIdx);
				rawData.put(key, rowValues);
			} else {
				rowValues.aggregateOtherMeasures(row.getContent());
			}
			
		}
		return rawData;
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
	
	private boolean hasNextScenarioTerm(int termBucketIdx) {
		return termBucketIdx+1 < termBucketSize;
	}

}
