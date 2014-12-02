package com.anz.rer.etl.transform.impl;

import java.util.List;

import com.anz.rer.etl.transform.ITransformer;

public class Transformers {
    
	public static List<String[]> trimRecords( ITransformer<List<String[]>, List<String[]>> transform, List<String[]> csvs) {
		return transform.transform(csvs);
	}
	
	
	public static List<String[]> transposeCsv( ITransformer<List<String[]>, List<String[]>> transform, List<String[]> csvs) {
		return transform.transform(csvs);
	}
	
	
	

	
}
