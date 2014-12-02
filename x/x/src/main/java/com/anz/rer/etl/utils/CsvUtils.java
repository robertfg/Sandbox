package com.anz.rer.etl.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class CsvUtils {
	
	public static final String COMMA = ",";
	
 
	private final static Logger logger = Logger.getLogger(CsvUtils.class);
	
	public List<String[]> loadCsv(String csvFileName, String delimeter, boolean skipFirstLine) {
		BufferedReader br = null;
		CSVReader csvReader = null;
		String[] csvLineData;
		List<String[]> csvLines = null;
		
		try {
			br = new BufferedReader(new FileReader(csvFileName), 65535);
			csvReader = new CSVReader(br,  delimeter.charAt(0));
			if (csvReader != null) {
				try {
					csvLines = new ArrayList<String[]>();
                    int ctr = 0;
					while ((csvLineData = csvReader.readNext()) != null) {
						if(skipFirstLine){
							skipFirstLine=false;
						} else {
							 if( !csvLineData[0].startsWith("#")){
							 	  csvLines.add(csvLineData);
							  }
						}
						//logger.info(ctr++);
					}
					
				} catch (IOException e) {
					logger.error("Failed to load table: " + e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("Source data file not found: " + csvFileName);
			e.printStackTrace();
		} catch (NullPointerException e) {
			logger.error("File is not presented for date: " + csvFileName);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (csvReader != null) {
				try {
					csvReader.close();
				} catch (IOException e) {
					logger.error("Failed to load FxDailyRate of VMR feed: "
							+ e.getMessage());
					e.printStackTrace();
				}
			}
		}

		return csvLines;
	
	}
    
	
	public static List<String[]>  loadCsv(String csvFileName,String delimeter,boolean skipLine, String ignoreString  ) {
	   
	   List<String[]> csvLines = new ArrayList<String[]>();
	   
	   Scanner lineScan = null;
	    int ctr = 0;
		try {
			lineScan = new Scanner( new FileReader(csvFileName));
			if (lineScan != null) {
					while (lineScan.hasNextLine()) {
						String input = lineScan.nextLine();
    						if(skipLine){
								skipLine=false;
							} else {
								 if( !input.startsWith("#")){
									 csvLines.add( input.split( delimeter,-1 ) );  
								 } 
							}
    						//logger.info(ctr++);
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lineScan.close();
			
		}
		return csvLines;
		
	}
	
	public static List<String[]>  loadCsv2(String csvFileName,String delimeter,boolean skipLine, String ignoreString  ) {
		   
		 
		    List<String[]> csvLines = new ArrayList<String[]>();
		   
		   
			
			try {
				
				BufferedReader bufferRead = new BufferedReader(new FileReader(csvFileName), 65535);
				
				String devstr;
				int ctr = 0;
				while ((devstr = bufferRead.readLine()) != null) {
					csvLines.add( devstr.split(delimeter,-1) );
					devstr = null;
					//System.out.println(ctr++);
				}
				bufferRead.close();
			    
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				
				
			}
			return csvLines;
		
			
		}
	public static int getNumberOfLine(String fileName) throws IOException{
			LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(fileName));
	        try {
	        	  lineNumberReader.skip(Long.MAX_VALUE);
	        	  return  lineNumberReader.getLineNumber();
	        } catch (Exception done) {
	            done.printStackTrace();
	        }finally{
	        	if(lineNumberReader!=null){
	        		lineNumberReader.close();
	        		lineNumberReader = null;
	        	}
	        	
	        }
	        return 0;
	}
	
 
	
	public static void load(File f) throws IOException {
		    FileInputStream fis = new FileInputStream(f);
		    FileChannel fc = fis.getChannel();
		    MappedByteBuffer mmb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    byte[] buffer = new byte[(int)fc.size()];
		    mmb.get(buffer);
		    fis.close();

		    BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer)));
                 int ctr=0;    
                 String line = null;
                 List<String[]> csvs = new ArrayList<String[]>();
		         for (line = in.readLine(); line != null; line = in.readLine()) {
                	 System.out.println(ctr++);
                	 csvs.add(line.split("!@!",-1));
		         }

		    in.close();
		  }

	
	
	
	
	public static void main(String[] args) throws IOException{
		
		String xd = "Global Markets,Commodities,\"Metals, Bulks & Bullion\",Base Metals,Base Metals - Exotics,CM TR LME EXO,ANZBG MELB";
      
		System.out.println( xd.split(",",-1));
		

		String s = "a1, a2, a3, \"a4,a5\", a6";
		       s = "a1~a2~a3~a4,a5~a6";
		
		Pattern pattern = Pattern.compile("~");
		
		
		Scanner	scan = new Scanner(" " + s + " " );
		
		scan.useDelimiter(pattern);
		int ctr1=0;
		
		while (scan.hasNext()) {
		  System.out.println( scan.next().trim());
		
		}
		
		
		
		
		String xx=";x;";
		System.out.println(xx.split(";")[1] );
		
		File file = new File("C:\\devs\\textfile.txt");
		System.out.println("Path : " + file.getAbsolutePath());
		System.out.println(file.getParent());
		System.out.println(file.getName());
		
       double x1 = -210740612.46000001;

       System.out.println(x1);
//		But in the QA4 DB, the value is -210740608.00000000
 

		
		//System.out.println(System.currentTimeMillis() + System.nanoTime());
		
		String str = "MRE_POSITION_20120719_20120720111809_txt.gz";
		System.out.println(   str.split( "\\_" )[1] );
		
		
		
		double x = Double.valueOf("-1.81E-07");
		System.out.println(x+2.3);
		
		
		String errLocal = "christopher (5) anaboasdfasdfasdfasdfasdf";
		
		int start = errLocal.indexOf("(");
    	int end   = errLocal.indexOf(")"); 
      System.out.println( errLocal.substring( start + 1,end  ));	
      
      
		double y = 1.7976931348623157E308;
		
		
		
		
		//long start = System.currentTimeMillis();
		
		//CsvUtils.load(new File("c:\\temp\\anz\\SIGM0#IR_GAMMA#NON-VAR#null#20120704#1473904595284651.apx"));
		
		/*try {
			int x = CsvUtils.getNumberOfLine("c:\\temp\\anz\\SIGM0#IR_GAMMA#NON-VAR#null#20120704#1473904595284651.apx");
			
		List<String[]> csvs = 	CsvUtils.loadCsv2("c:\\temp\\anz\\SIGM0#IR_GAMMA#NON-VAR#null#20120704#1473904595284651.apx", ",", true, "#");
			System.out.println( csvs.size() );
			System.out.println(x);
		long end = System.currentTimeMillis();
		
		System.out.println( ((end - start) / 1000)  + " s");
			
		} catch (IOException e) {
			
			
			e.printStackTrace();
		}
		*/
		
	
		
	}
    
}
