package com.anz.rer.etl.transform.impl;

import java.util.ArrayList;
import java.util.List;

import com.anz.rer.etl.transform.ICriteria;
import com.anz.rer.etl.transform.ITransformer;
import com.anz.rer.etl.utils.CsvUtils;

public class TrimDownRecords implements ITransformer<List<String[]>,List<String[]>>{

	
    
	public TrimDownRecords() {
		
	}

	@Override
	public List<String[]> transform(List<String[]> param) {
	      
		if(param!=null && param.size()>0){
			List<String[]> retVal = new ArrayList<String[]>();
			for (final String[] csv : param) { 
				
				// Criteria Interface or Generic if
			if(	 new ICriteria<String[]>(){
						@Override
						public boolean valid(String[] param) {
							if(param[0]==null){
							return false;	
							}
							return true; 
						}
				 }.valid(csv)){
				retVal.add(csv);
			}
			
			}
			return retVal;
		}
		return null;
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
			String csvFileName = "c:\\project\\data\\mxrate\\CURR_SPOT_20120301.txt";
			String delimeter = ";";
	  
		List<String[]> csvs = CsvUtils.loadCsv(csvFileName, delimeter, false,"#");
		TrimDownRecords tdrec = new TrimDownRecords();
						
		csvs =  Transformers.trimRecords( tdrec , csvs);
		
		
		
		
		 
		
	}
}
