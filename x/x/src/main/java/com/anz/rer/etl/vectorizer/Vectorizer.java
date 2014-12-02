package com.anz.rer.etl.vectorizer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.anz.rer.etl.utils.CsvObject;

public class Vectorizer {
	   private ConcurrentHashMap<String, String[] > vecContainer = new ConcurrentHashMap<String, String[]>();
	   private AtomicInteger intKey;
	   
	   	
	private ThreadLocal<DateFormat> dateFormat=new ThreadLocal<DateFormat>(){
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyyMMdd");
		}
		
	};
	
	
	
	public  CsvObject vectorizer(  CsvObject csvs, VectorConfig vectorConfig ) {
      try{
		intKey = new AtomicInteger();
	    String vectorHeader = vectorConfig.getVectorHeader();     
		Map<String, HashMap<Integer, String[]>> vectorizedData = new HashMap<String,HashMap<Integer,String[]>>();
		
		
	    for (Object[] csv : csvs.getRows()) {
			String key = this.buildKey((String[])csv, vectorConfig.getColumnKeyId(),vectorConfig.getColumnKeyDelimeter(),intKey);
			
			       csv = this.formatAndCleanData((String[])csv, vectorConfig);
			
			 String[] data = vecContainer.get(key);
			
					if (data == null) {
						
						if(checkMandatory((String[])csv, vectorConfig.getMandatory())){
					
							if( vectorConfig.getColumnToVectorize() !=null && vectorConfig.getColumnToVectorize().length>0){

								for (int i = 0; i < vectorConfig.getColumnToVectorize().length; i++) {
									
									if(vectorConfig.getColumnToVectorize()[i] == vectorConfig.getVectorKeyColum()  ){
										
										
									} else {
									
										if( csv.length > vectorConfig.getColumnToVectorize()[i]){
											if(!csv[vectorConfig.getColumnToVectorize()[i]].equals("N/A") && !csv[vectorConfig.getColumnToVectorize()[i]].equals("")){
												/*csv[vectorConfig.getColumnToVectorize()[i]] = getVectorizedData( vectorConfig, 
											    		   (String)csv[vectorConfig.getVectorKeyColum()], (String)csv[vectorConfig.getColumnToVectorize()[i]]  );
												*/
												csv[vectorConfig.getColumnToVectorize()[i]] = getVectorizedData( 
																							  vectorizedData, 
														                                      key, 
														                                      vectorConfig.getColumnToVectorize()[i],
														                                      (String)csv[vectorConfig.getColumnToVectorize()[i]],
														                                      vectorConfig, 
														                                      (String)csv[vectorConfig.getVectorKeyColum()]
														                                      );
												
												
											}else { 
											    csv[vectorConfig.getColumnToVectorize()[i]] = getVectorizedData( vectorConfig, 
											    		   (String)csv[vectorConfig.getVectorKeyColum()], "0"  );
											}
										}
									}
								}
							}
							csv[vectorConfig.getVectorKeyColum()] = vectorHeader;
							vecContainer.put(key, (String[]) csv);
						}
					} else {
						for (int i = 0; i < vectorConfig.getColumnToVectorize().length; i++) {
							if(vectorConfig.getColumnToVectorize()[i] == vectorConfig.getVectorKeyColum()  ){
								//csv[vectorConfig.getColumnToVectorize()[i]] = vectorHeader;
							} else {
								if( data.length > vectorConfig.getColumnToVectorize()[i] && csv.length > vectorConfig.getColumnToVectorize()[i] ){
									if(!csv[vectorConfig.getColumnToVectorize()[i]].equals("N/A") && !csv[vectorConfig.getColumnToVectorize()[i]].equals("")){
									/*	data[vectorConfig.getColumnToVectorize()[i]] = data[vectorConfig.getColumnToVectorize()[i]]  +  vectorConfig.getVectorDelimeter() + csv[vectorConfig.getColumnToVectorize()[i]] ;*/
									
										data[vectorConfig.getColumnToVectorize()[i]] = getVectorizedData( 
												  vectorizedData, 
			                                      key, 
			                                      vectorConfig.getColumnToVectorize()[i],
			                                      (String)csv[vectorConfig.getColumnToVectorize()[i]],
			                                      vectorConfig, 
			                                      (String)csv[vectorConfig.getVectorKeyColum()]
			                                      );
										
									}
								}
							}
						}
						data[vectorConfig.getVectorKeyColum()] = vectorHeader;
						vecContainer.put(key, data);
				   }
		
		}
		
		
		List<Object[]> vectorizedCsv = new ArrayList<Object[]>();
		
		for ( Map.Entry<String,String[]>  data :  vecContainer.entrySet() ) {
	  	    Object[] vectorCsv = new Object[vectorConfig.getColumnToExtract().length];
			
	  	    for (int i = 0; i < vectorConfig.getColumnToExtract().length; i++) {
		    	vectorCsv[i] = data.getValue() [vectorConfig.getColumnToExtract()[i] ];
	  	    	
			}
	  	  vectorizedCsv.add(vectorCsv);
		}
		
		
		 csvs.setRows(vectorizedCsv);
      }catch(Exception e){
    	  e.printStackTrace();
      }
		 return csvs;
	}
	
	private String getVectorizedData(Map<String, HashMap<Integer, String[]>> vectorizedData, 
									 String key, 
									 int vectorColumn, 
									 String vectorValue,
									 VectorConfig vectorConfig, 
									 String vectorColumnKey ){
		
		HashMap<Integer, String[]> vectorArray =  vectorizedData.get(key);
		String[] vectorValues = null;
		if( vectorArray == null ){
			vectorArray  = new HashMap<Integer,String[]>();
			vectorValues = getVectorizedData(vectorConfig,vectorColumnKey,vectorValue);
			vectorArray.put(vectorColumn, vectorValues  );
			vectorizedData.put(key, vectorArray);
			
		} else {
			  vectorValues = vectorArray.get(vectorColumn);
			if (vectorValues==null){      // different column to be vectorized
				vectorValues = getVectorizedData(vectorConfig,vectorColumnKey,vectorValue);
				vectorArray.put(vectorColumn, vectorValues  );
			}else { //existing
				vectorValues[vectorConfig.getVectorMapping().get(vectorColumnKey)] = vectorValue;
				vectorArray.put(vectorColumn, vectorValues);
			}
			
			
		}
		  
		return arrToString(vectorValues,vectorConfig);
	}
	
	private String[] getVectorizedData(VectorConfig vectorConfig, String vectorKey, String vectorData){
		  String[] vectorValues = new String[vectorConfig.getVectorSize()];
 		  	  vectorValues[vectorConfig.getVectorMapping().get(vectorKey)] = vectorData;
 		  return vectorValues; 
	}
	private String arrToString(String[] vectorValues,VectorConfig vectorConfig){
		
		  StringBuilder reply = new StringBuilder(500);
		  
		  for (int i = 0; i < vectorValues.length; i++) {
			    if(vectorValues[i]==null || vectorValues[i]=="" ){
			    	reply.append("0").append(vectorConfig.getVectorDelimeter());	
			    }else {
			    	reply.append(vectorValues[i]).append(vectorConfig.getVectorDelimeter());
			    }
		  }
		return reply.substring(0,reply.length()-1);
		
	}
	
	
	private String[] formatAndCleanData(String[] csv, VectorConfig vectorConfig ){
		
		//clean and format data // will be remove and move to stored procedure
		if(vectorConfig.getColumnFormatting() != null){
			for (Map.Entry<Integer, String> format : vectorConfig.getColumnFormatting().entrySet()) {
				if(!csv[format.getKey()].equals("N/A") && !csv[format.getKey()].equals("")){	
					csv[format.getKey()] = formatData( format.getValue(), csv[format.getKey()] );
				}
			}
		}
		
		return csv;
		
	}
	
	private String formatData(String formatting, String data){
		if(formatting.equals("yyyyMMdd")){
			 try {
				 dateFormat.set(new SimpleDateFormat("dd/MM/yy"));
				Date d = dateFormat.get().parse(data);
				dateFormat.set(new SimpleDateFormat("yyyyMMdd") );
				return dateFormat.get().format(d);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (formatting.equals("percent")){
			
			data = String.valueOf(   Double.parseDouble(data) * 100  ) + "%";
			
		}
		return data;
	}
	
	private boolean checkMandatory(String[] data, Integer[] mandatory){
		if(mandatory!=null && mandatory.length>0){
			for (int i = 0; i < mandatory.length; i++) {
				if(data[mandatory[i]] == null || data[mandatory[i]].length() == 0 || data[mandatory[i]].equals("null")   ){
					return false;
				}
			}
		}
		return true;
	}
	private String buildKey( String[] arr, Integer[]  key, String delimeter, AtomicInteger intKey){
	     if(key!=null && key.length>0){   
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < key.length; i++) {
					sb.append(  arr[key[i]] ).append(delimeter);
				}
				return sb.substring(0, sb.length()-1);
	     } else {
	    	return String.valueOf(intKey.incrementAndGet());
	     }
	}
	
	
	
}
