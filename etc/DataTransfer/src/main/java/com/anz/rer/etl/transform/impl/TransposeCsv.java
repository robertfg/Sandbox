package com.anz.rer.etl.transform.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.anz.rer.etl.utils.CsvObject;

  
public class TransposeCsv implements Callable<CsvObject> {
	private BlockingQueue<CsvObject> destQueue;
	private TransposeConfig transposeConfig;
	private CsvObject csvObject;
	
	private final static Logger logger = Logger.getLogger(TransposeCsv.class);
	public TransposeCsv(TransposeConfig transposeConfig,
			BlockingQueue<CsvObject> destQueue,CsvObject csvObject) {
	   
		this.destQueue  = destQueue;
		this.transposeConfig = transposeConfig;
		this.csvObject = csvObject;
	   
	}
	
	public TransposeCsv(TransposeConfig transposeConfig) {
	   
		this.transposeConfig = transposeConfig;
		
	   
	}
	public CsvObject transform( CsvObject csvObject ) {
		
		long start = System.currentTimeMillis();
	
		try{
			List<Object[]> rows = new ArrayList<Object[]>();
			for (Object[] csvs : csvObject.getRows()) {
				Object[] row = new Object[transposeConfig.getUvrLength()];
				
				for(Map.Entry<Integer, Integer> mapping: transposeConfig.getCsvToUvrMappingConfig().entrySet()) {
					try{
					row[mapping.getKey()] = csvs[mapping.getValue()];
					}catch(java.lang.ArrayIndexOutOfBoundsException a){
						break;
						
					}
					
				}
				//V1AL0#VAR AND P&L#PNL-VAR_1D#178384439952698#20130307#178384439952698#-1#20130402.APX
				if(csvObject.getName().substring(0, 5).equals("V1AL0") || csvObject.getName().substring(0, 5).equals("VXAL0")){
					row[27] = combine(csvs,"|",2,501);
					row[68] = csvs[502];
					
				}else if(csvObject.getName().substring(0, 5).equals("VSAL0")){
					row[27] = combine(csvs,"|",2,262);
					row[68] = csvs[263];
			
				}else if(csvObject.getName().substring(0, 5).equals("VFAL0")){
					row[27] = combine(csvs,"|",2,1541);
					row[68] = csvs[1542];
					
				} else {
				
					if(csvs.length>=38){
						row[69] = combine(new Object[]{csvs[33],csvs[34],csvs[38]},"|");
					}else if(csvs.length==35){
						row[69] = combine(new Object[]{csvs[33],csvs[34],"N/A"},"|");
					}
					
					if(csvs.length>=46){ //equityHierachyIndex
						//row[47] = combine(new Object[]{"a", "b","C","d"},"|");
						//HSCE|HSCE Total|Asia|Indices|ANZ Group
						row[47] = combine(new Object[]{ "N/A",  csvs[44], csvs[45],  csvs[43],"N/A"},"|");
						  
					}
					
					if(csvs.length>=49){ //DealCurrencyGroup Family-FXO DealCurrencyGroup
						//row[102] = combine(new Object[]{"",csvs[49],csvs[50],"",""},"|");
						//HKD|Asia Pegged|Group: HKD|Major|Non-Precious Metals|Onshore|Asian CCY|Asian CCY
 //0,1,2,3..
						row[102] = combine(new Object[]{"N/A",csvs[47],csvs[49],"N/A","N/A","N/A","N/A","N/A"},"|");
						
					}
					
					if(csvs.length>=51){ //currGroupHierachyIndex CurrencyFamily"     expression="2/currencyGrouping/1"
						//row[48] = combine(new Object[]{"",csvs[50],csvs[50],"a","b","c","d"},"|");
						row[48] = combine(new Object[]{"N/A",csvs[48],csvs[50],"N/A","N/A","N/A","N/A"},"|");
						
					}
					
					if(csvs.length>=52){ //currPairHierachyIndex  //CurrL2"			expression="2/currencyHierarchy/3"
						//row[46] = combine(new Object[]{" ",csvs[51], " ", csvs[51], " "},"|");
						//0,1,2,3,
						row[46] = combine(new Object[]{"N/A","N/A", "N/A", csvs[51], "N/A"},"|");
					}
					  
				}
				rows.add(row);
			}
			
			if(rows!=null && !rows.isEmpty()) {
				csvObject.setRows(rows);
				logger.info("rows.size:" + rows.size());
			}else {
				logger.info("rows.size:" + 0);
			}
		
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			
		}
		
		logger.info("Time to transpose: " + csvObject.getRows().size() + " rows, time:" + (System.currentTimeMillis() - start) + " ms., ObjName:" + csvObject.getName());
		 return csvObject;
	}
	
	private String combine(Object[] combineString, String delimeter){
		//SOY|OILSEEDS|Agriculture
		StringBuilder combineStr =  new StringBuilder((String) combineString[0]) ;
		
		for (int i = 1; i < combineString.length; i++) {
			if(combineString[i]==""){
				combineString[i]=" ";
			}
			
			combineStr.append(delimeter).append(combineString[i]);
		}
		
		return combineStr.toString();
		
		
	}
	
	private String combine(Object[] csvSrc, String delimeter, int from, int to){
		
		//SOY|OILSEEDS|Agriculture
		StringBuilder combineStr =  new StringBuilder( (String) csvSrc[from]) ;
		int i = from+1;
		for ( ; i <= to; i++) {
			
			combineStr.append(delimeter).append( csvSrc[i] );
		}
		
		return combineStr.toString();
		
		
	}
	
	public static void main(String[] args){
		String[] arr = new String[]{"1","2","3"};
		
		String rowStr=  Arrays.toString(arr );
					rowStr=rowStr.replace("[", "");
					rowStr=rowStr.replace("]", "");
					
					
		System.out.println(rowStr);
		
		System.out.println(arr);
		String s = "S";
		String col = "";
		
		for (int i = 1; i < 262; i++) {
			
			col+= s+i + "$" + s + i+"$$";
		}
		
		System.out.println(col);
	}

	@Override
	public CsvObject call() throws Exception {

		CsvObject csvObj = transform(csvObject);
		
		if(csvObj!=null){
			logger.info("TransposeTask putting to stamping");
			destQueue.put(csvObj);
		}else{
			logger.info("TransposeTask csvObject is null");
		}  
		return csvObj;
		
	}
	
}
