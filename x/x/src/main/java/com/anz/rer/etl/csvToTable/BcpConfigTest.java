package com.anz.rer.etl.csvToTable;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;

import com.anz.rer.etl.csvloader.CsvConfig;
import com.anz.rer.etl.utils.XMLUtils;

public class BcpConfigTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String bcpName = "SEDG0#EQ_DELTAGAMMA#NON-VAR#1684331982109592#20130131#1684331982109592.APX";
		try {
			BcpData bcpData = BcpConfigTest.buildBcpData("C:\\temp\\ANZ\\" + bcpName, bcpName);
		
			 SortedMap<Integer, CsvConfig> csvToTableMapping = BcpConfigTest.loadConfig("c:\\temp\\anz\\config\\Staging.FactRiskMeasureNonVaRDetail.XML");
			 
			 BcpConfigTest.parse(bcpData,csvToTableMapping);
			 
		} catch (Exception e) {
						e.printStackTrace();
		}

	}

	
	public static BcpData buildBcpData(String csvFileName, String bcpDataName) throws Exception{
	    int ctr=0;    
        int id  = 1;
        List<String[]> csvLines = new ArrayList<String[]>();
        String[] header = null;
         
        boolean gotData = false;
	BcpFileReader bcpReader = new BcpFileReader(csvFileName);

	for (String csv : bcpReader) {
		if(!csv.startsWith("#")){
			
   	    	csvLines.add(csv.split(",",-1));
		   gotData = true;
   	   }
	}
	bcpReader.Close();
	bcpReader = null;
	return new BcpData(csvLines,id, 10 ,10, bcpDataName,1,1 );
 
	
	
	}

	public static void parse(BcpData bcpData, SortedMap<Integer, CsvConfig> csvToTableMapping){
		StringBuilder sqlRawData = new StringBuilder(50);  
			try{
				long start = System.currentTimeMillis();
				 
				List<String[]> csvs = bcpData.getCsv();
				for (int i = 0; i < csvs.size(); i++) { 
					String[] csv = csvs.get(i);
					for (Map.Entry<Integer, CsvConfig> entry : csvToTableMapping.entrySet() ) {
					         CsvConfig columnConfig = entry.getValue(); 
					         try{
					        	 sqlRawData.append(csv[columnConfig.getCsvColumnNumber()]);
					         }catch(ArrayIndexOutOfBoundsException a){
					        	 a.printStackTrace();
					         }catch(Exception e){
					        	 e.printStackTrace();
					         }
							 
					}
				}
				}catch(java.lang.ArrayIndexOutOfBoundsException a ){
					a.printStackTrace();
				}catch(Exception e){
				e.printStackTrace();	
				}
		 System.out.println(sqlRawData.length() +  "");
	} 
		 
		 
		 
		 
		 
		 
		 
			
	public static SortedMap<Integer, CsvConfig>   loadConfig(String fileName){

		 
		SortedMap<Integer, CsvConfig> csvToTableMapping = new TreeMap<Integer, CsvConfig>();
		 
		 
		 
		 try {
			
				File xmlFile = new File(fileName);
				Document xmlConfig = XMLUtils.parse(xmlFile);
				XMLUtils.removeAllNamespaces(xmlConfig);
				List list = xmlConfig.selectNodes("//RECORD/FIELD");
				
				
				
				for (Iterator iter = list.iterator(); iter.hasNext();) {
					Node field = (Node) iter.next();
					
					String id = field.valueOf("@ID");
					String xpathStr = "//ROW/COLUMN[@SOURCE='" + id + "']";
					
					Node column = field.selectSingleNode(xpathStr);
					
					com.anz.rer.etl.csvloader.CsvConfig csvConfig = new com.anz.rer.etl.csvloader.CsvConfig();
							  csvConfig.setColumnName(column.valueOf("@NAME"));
							  csvConfig.setColumnOrder(Integer.valueOf(id));
							  csvConfig.setMaxLength(Integer.valueOf(field.valueOf("@MAX_LENGTH")));
							  csvConfig.setColumnType(column.valueOf("@xsi:type"));
							  csvConfig.setCsvColumnNumber(Integer.valueOf(column.valueOf("@CSVSOURCE")));
							  csvConfig.setDefValue( column.valueOf("@VALUE") );
							  csvConfig.setFormat( column.valueOf("@FORMAT") );
							  
							  
							  
					csvToTableMapping.put(csvConfig.getColumnOrder(), csvConfig);
				}
			} catch (DocumentException e) { 
				e.printStackTrace();
				
			} catch(Exception e){
				e.printStackTrace();
				
			}
		 
		  return csvToTableMapping;
		 
	}
}
