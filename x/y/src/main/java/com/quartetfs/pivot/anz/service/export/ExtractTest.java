package com.quartetfs.pivot.anz.service.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.query.impl.QueryHelper;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.fwk.query.QueryException;
//import com.quartetfs.pivot.anz.utils.QueryHelper;



public class ExtractTest {

	public ExtractTest(IActivePivot pivot){
		this.pivot = pivot;
	}
	private IActivePivot pivot;
	
	public void extract(){
		
		DateParser parser = (DateParser) Registry.getPlugin(IParser.class).valueOf("date[yyyyMMdd]"); 
		
		  List<Map<String, Object>> queryParameters = new ArrayList<Map<String,Object>>();
		        Map<String, Object> queryParameter = new HashMap<String, Object>();
		   
		 	QueryHelper queryHelper = new QueryHelper(pivot);
			Collection<String>  measures = new ArrayList<String>();
			
			measures.add("M_RESULTV.SUM");
			measures.add("M_RESULT.SUM");
			
		   IDate cobDate = Registry.create(IDate.class, parser.parse("20130301").getTime());
		   
			queryParameter.put("COB Date@COB Date", cobDate);
			queryParameter.put("Container@Container", "IR_VANNA");
			queryParameter.put("Base Currency@Base Currency", ILocation.WILDCARD);
			 

			queryParameter.put("Currency Code@Underlying-CCY", ILocation.WILDCARD);
			queryParameter.put("Scenario-Term@Scenario-Term", ILocation.WILDCARD); 
			queryParameter.put("Position ID@Position ID",   ILocation.WILDCARD);
			queryParameter.put("Scenario-Term Bucket@Scenario-Term Bucket", ILocation.WILDCARD);
			queryParameter.put("Spread-Type1@Spread Type", ILocation.WILDCARD);
			queryParameter.put("Scenario-Term Underlying@Scenario-Term Underlying", ILocation.WILDCARD);
			
	
			queryParameters.add(  queryParameter );
			
			
			 
			
			
			try {
				ICellSet cellSet = queryHelper.getAggregates(queryParameter,  measures);
			//	System.out.println("done");
			} catch (QueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
}
