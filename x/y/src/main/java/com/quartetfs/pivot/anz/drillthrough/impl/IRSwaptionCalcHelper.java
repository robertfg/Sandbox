package com.quartetfs.pivot.anz.drillthrough.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.logging.impl.MessagesServer;
import com.quartetfs.biz.pivot.server.impl.PointKeyExtractor;
import com.quartetfs.pivot.anz.drillthrough.TransposeRequestInput;
import com.quartetfs.pivot.anz.utils.ANZUtils;
import com.quartetfs.pivot.anz.webservices.impl.TransposeDrillThroughService;
import com.quartetfs.tech.point.impl.Point;

public class IRSwaptionCalcHelper {

	private static Logger logger = Logger.getLogger(MessagesServer.LOGGER_NAME, MessagesServer.BUNDLE);
	private List<String> termBuckets;
	private List<String> underlyingTermBuckets;
	private PointKeyExtractor extractor;
	private Map<Point, DrillThroughRowContent> sawptionData;
	private int termBucketSize;
	private int underlyingTermSize;
	private int termColIndex;
	private int secnarioTermColIndex;
	private int measureColIndex;
	private TransposeRequestInput transposeDTO;
	
	public IRSwaptionCalcHelper(IActivePivot pivot,PointKeyExtractor extractor,Map<Point, DrillThroughRowContent> sawptionData,TransposeRequestInput transposeDTO) 
	{
	
		this.extractor = extractor;
		this.sawptionData = sawptionData;
		this.transposeDTO=transposeDTO;
		
		//Find Header index			
		this.termColIndex = transposeDTO.getColumnDetails().getHeaders().indexOf(TransposeDrillThroughService.transposeColumn);
		this.secnarioTermColIndex = transposeDTO.getColumnDetails().getHeaders().indexOf(TransposeDrillThroughService.ScenarioTermUnderlyingColumn);
		this.measureColIndex = transposeDTO.getColumnDetails().getHeaders().indexOf("M_RESULTV");
				
		this.termBuckets =  ANZUtils.getSortedTermBucket(transposeDTO.getDimensionDetails().getTermDimension());
		this.underlyingTermBuckets =  ANZUtils.getSortedTermBucket(transposeDTO.getDimensionDetails().getUnderlyingTermDimension());
		this.termBucketSize=termBuckets.size();
		this.underlyingTermSize=underlyingTermBuckets.size();	
	}
	
	public void calculate()
	{
		for(Map.Entry<Point, DrillThroughRowContent> entry : sawptionData.entrySet())
		{
			DrillThroughRowContent row = entry.getValue();				
			String termValue=row.getContent()[termColIndex].toString();
			String scenarioTermValue=row.getContent()[secnarioTermColIndex].toString();
			if(!transposeDTO.isAllowedTerm(termValue) || !transposeDTO.isAllowedUnderlyingTerm(scenarioTermValue))
			{
				continue;
			}
			
			double pnl=0d;
			try
			{
				pnl = Double.valueOf(row.getContent()[measureColIndex].toString());
			}
			catch(NumberFormatException e)
			{
				logger.log(Level.SEVERE, String.format("Unable to parse measure index %s, Data %s", measureColIndex,Arrays.toString(row.getContent())), e);
				throw e;
			}
			
			Object[] rowData = row.getContent().clone();			
			int termBucketIdx = termBuckets.indexOf(termValue);
			int underlyingTermIdx = underlyingTermBuckets.indexOf(scenarioTermValue);
			
			double value=pnl;
			
			//if both member is not the longest term
			if( hasNextScenarioTerm(termBucketIdx) && hasNextScenarioUnderlying(underlyingTermIdx))
			{
				//Calc by shifting termBucket					
				Iterator<String> itr = termBuckets.listIterator(termBucketIdx+1);
				String nextTermBucket=null;
				boolean found=false;
				while(itr.hasNext())
				{
					nextTermBucket =itr.next();
					rowData[termColIndex] = nextTermBucket;
					Point key=extractor.extractKey(rowData);	
					DrillThroughRowContent rowContent = sawptionData.get(key);
					if(rowContent==null) continue;
					
					double nextValue=Double.valueOf(rowContent.getContent()[measureColIndex].toString());
					value=pnl;//-nextValue;					
					found=true;
					break;
				}
				if(!found)
				{
					//If calc by termbucket does't work then calc by shifting underlyingTermBucket
					value=calcUsingNextUnderlyingTermBucket(pnl,rowData,underlyingTermIdx);
				}
			}
			//If term bucket is longest & underlyingterm bucket is not longest 
			else if(isLastTerm(termBucketIdx) && hasNextScenarioUnderlying(underlyingTermIdx))
			{
				value=calcUsingNextUnderlyingTermBucket(pnl, rowData,underlyingTermIdx);		
			}			
			row.aggregateTransposeMeasure(termValue, value);		
		}
	}


	private boolean isLastTerm(int termBucketIdx) 
	{
		return termBucketIdx+1 == termBucketSize;
	}


	private boolean hasNextScenarioUnderlying(int underlyingTermIdx) {
		return underlyingTermIdx+1 < underlyingTermSize;
	}

	private boolean hasNextScenarioTerm(int termBucketIdx) {
		return termBucketIdx+1 < termBucketSize;
	}	
	
	
	
	private double calcUsingNextUnderlyingTermBucket(double currentTermValue,final Object[] termsLocations,int underlyingTermIdx) 
	{
		Iterator<String> itr = underlyingTermBuckets.listIterator(underlyingTermIdx+1);
		while(itr.hasNext())
		{
			String nextUnderlyingBucket = itr.next();
			termsLocations[secnarioTermColIndex] = nextUnderlyingBucket;				
			for(String currentTerm : termBuckets)
			{
				termsLocations[termColIndex] = currentTerm;	
				Point key=extractor.extractKey(termsLocations);	
				DrillThroughRowContent rowContent = sawptionData.get(key);
				if(rowContent==null) continue;
				
				double nextValue=Double.valueOf(rowContent.getContent()[measureColIndex].toString());											
				return currentTermValue;//-nextValue;				
			}
		}
		return currentTermValue;
	}
	
	
}
