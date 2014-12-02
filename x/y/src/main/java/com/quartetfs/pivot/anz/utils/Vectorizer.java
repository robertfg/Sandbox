package com.quartetfs.pivot.anz.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.lang.time.FastDateFormat;

import com.quartetfs.pivot.anz.comparator.impl.CustomTenorComparator;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.export.ExtractObject;

public class Vectorizer implements Callable<Long> {
	
	   private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	   private List<String[]> csvData;
	   private AtomicInteger intKey;
	   
	   private VectorConfig vectorConfig;
	   private ExtractObject extractObject;

	   private BlockingQueue<ExtractObject> destQueue;
	   private ConcurrentHashMap<String, String[] > vecContainer = new ConcurrentHashMap<String, String[]>();
//	/   private TreeMap<String, TreeMap<String, Double>> vegaValues = new TreeMap<String, TreeMap<String,Double>>( new CustomTenorComparator<String>() );
	  
	   
		public Vectorizer(ExtractObject extractObject,VectorConfig vectorConfig,
			BlockingQueue<ExtractObject> destQueue) {
			this.extractObject = extractObject;
			this.vectorConfig = vectorConfig;
			this.destQueue = destQueue;
			
	 }
		
		public Vectorizer(){
			
		}

		
		
		private ThreadLocal<DateFormat> dateFormat=new ThreadLocal<DateFormat>(){
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat(ANZConstants.YYYYMMDD);
			}
			
		};
	
		private ThreadLocal<DateFormat> sourceDateFormat=new ThreadLocal<DateFormat>(){
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat("dd/MM/yy");
			}
			
		};
   @Override
	public Long call() throws Exception {
		long totalTime = 0;
		destQueue.put( vectorizer( extractObject));
		extractObject = null;  
		return System.currentTimeMillis() - totalTime;
		
		
	}   
	
	/*public void putTenorsValue(String k1, String k2, Double dVal){
	        	
	 	    if(vegaValues.get(k1) == null){
				TreeMap<String,Double> value = new TreeMap<String,Double>(new CustomTenorComparator<String>());
				 value.put(k2,dVal);
				 vegaValues.put(k1, value ); 
			 } else {
				 vegaValues.get(k1).put(k2, dVal);
			 }
		
	 	    
	}*/
	
	
	
	public  ExtractObject vectorizer(  ExtractObject csvs ) {
		
		long startTime = System.currentTimeMillis();
		 intKey = new AtomicInteger();
		
		   
		for (String[] csv : csvs.getRows()) {
			String key = this.buildKey(csv, vectorConfig.getColumnKeyId(),vectorConfig.getColumnKeyDelimeter() ,intKey);
		    csv = formatAndCleanData(csv, vectorConfig);
		   
			String[] data = vecContainer.get(key);
			if (data == null) {
				if(checkMandatory(csv, vectorConfig.getMandatory())){
					if(vectorConfig.getColumnToVectorize()!=null && vectorConfig.getColumnToVectorize().length>0){
						for (int i = 0; i < vectorConfig.getColumnToVectorize().length; i++) {
							if( csv.length > vectorConfig.getColumnToVectorize()[i]){
								if(!csv[vectorConfig.getColumnToVectorize()[i]].equals("N/A") && !csv[ vectorConfig.getColumnToVectorize()[i]].equals("")){
											csv[ vectorConfig.getColumnToVectorize()[i]] = csv[ vectorConfig.getColumnToVectorize()[i]] + vectorConfig.getVectorDelimeter();
								}else {
								    csv[ vectorConfig.getColumnToVectorize()[i]]="";
								}
							}
						}
					}
				
					vecContainer.put(key, csv);
				}
			} else {
				for (int i = 0; i <  vectorConfig.getColumnToVectorize().length; i++) {
					if( data.length >  vectorConfig.getColumnToVectorize()[i] && csv.length >  vectorConfig.getColumnToVectorize()[i] ){
						if(!csv[ vectorConfig.getColumnToVectorize()[i]].equals("N/A") && !csv[ vectorConfig.getColumnToVectorize()[i]].equals("")){
							data[ vectorConfig.getColumnToVectorize()[i]] = data[ vectorConfig.getColumnToVectorize()[i]]  +  csv[ vectorConfig.getColumnToVectorize()[i]] +  vectorConfig.getVectorDelimeter();
						}
					}
				}
				
				vecContainer.put(key, data);
		   }
		}
		
		
		List<String[]> csv = new ArrayList<String[]>();
		
		for ( Map.Entry<String,String[]>  data :  vecContainer.entrySet() ) {
			
			StringBuilder retVal = new StringBuilder();
			StringBuilder merger = new StringBuilder();
			StringBuilder row = new StringBuilder();
			boolean holder = false;		
			
			for (int i = 0; i < vectorConfig.getColumnToExtract().length; i++) {
				if( data.getValue().length > vectorConfig.getColumnToExtract()[i]) {
					if( vectorConfig.getColumnToMerge()!=null && vectorConfig.getColumnToMerge().contains(vectorConfig.getColumnToExtract()[i])){
						merger.append( data.getValue() [ vectorConfig.getColumnToExtract()[i] ]);
						if(data.getValue() [ vectorConfig.getColumnToExtract()[i]] !=null && data.getValue() [ vectorConfig.getColumnToExtract()[i] ].length() > 1 &&  data.getValue() [ vectorConfig.getColumnToExtract()[i] ].indexOf(  vectorConfig.getColMergeDelimeter() ) ==-1){  
							merger.append( vectorConfig.getColMergeDelimeter() ) ;
						}
		                if(!holder){
		            	  row.append( "place_holder" ).append(vectorConfig.getColumnDelimeter());
		            	  holder = true;
		             	} 
					} else {
						 if(  vectorConfig.getRemoveTrailer() !=null && vectorConfig.getRemoveTrailer().get(vectorConfig.getColumnToExtract()[i])!= null   ) {
							 row.append( data.getValue() [ vectorConfig.getColumnToExtract()[i] ].substring(0,data.getValue() [ vectorConfig.getColumnToExtract()[i] ].length() -1 ) ).append(vectorConfig.getColumnDelimeter());
						 } else {
							 row.append( data.getValue() [ vectorConfig.getColumnToExtract()[i] ]  ).append(vectorConfig.getColumnDelimeter());
						 }
					}
				} else {
					 row.append( ""  ).append(vectorConfig.getColumnDelimeter() );
				}
			}
			
			int	idx = row.indexOf("place_holder");
			if(idx>1){
				if( vectorConfig.getRemoveFromChar()!=null){
					int remFromInt =  merger.indexOf( vectorConfig.getRemoveFromChar()); 
					if(remFromInt<1){
						row.replace( idx, idx + 12 ,  merger.replace( merger.length() - 1, merger.length(), "").toString()  );
						retVal.append( row  );
					} else {
						row.replace( idx, idx + 12 ,  merger.replace( remFromInt - 1, merger.length(), "").toString()  );
					}
				}else {
					row.replace(idx, idx + 12 , merger.toString() ); 
					row.replace(idx, idx + 12 , merger.substring(0 - merger.length() -1) );
					retVal.append( row  );
				}
			} else {
				retVal.append( row  );   
			}
			if(retVal.length()>0){
				String[] dest = new String[7];
				System.arraycopy(retVal.toString().split( "," ), 0 ,dest, 0, 7);
				csv.add(dest);
			} else{
			
			}
		}
		
		
		csvs.setRows(csv);
		
		LOGGER.info("Time took to vectorize:" + csvs.getId() + " "  + (System.currentTimeMillis() - startTime) + " ms" +  " Number of Objects:" + csvs.getRows().size() );
		
		vecContainer = null;
		
		 return csvs;
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
				return dateFormat.get().format(sourceDateFormat.get().parse(data));
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
	
	

	public void setCsvData(List<String[]> csvData) {
		this.csvData = csvData;
	}

	public List<String[]> getCsvData() {
		return csvData;
	}


	public static void main(String[] args){
		
		String x= "603800559503268,20121211,VAR_1D,AUD,0.0%,ANZ Group|TradedCAPM|Combined Trading|Traded|IROs & CPI|DT - Aust - IRO|IRO - AUD|IRO - AUD - Vanilla|A ARR OPT AU,108552.50444999982,null,null,null,null,null,null,null,null,null,null,null,";
		String[] dest = new String[7];
		
		System.arraycopy(x.split(","), 0 ,dest, 0, 7);
		System.out.println( dest );
		
	}


	
}
