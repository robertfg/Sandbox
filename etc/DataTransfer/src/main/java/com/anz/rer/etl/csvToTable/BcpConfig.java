package com.anz.rer.etl.csvToTable;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;

import com.anz.rer.etl.utils.XMLUtils;

public class BcpConfig {
	
	private final static Logger logger = Logger.getLogger(BcpConfig.class);
	private String keyId;
	private String tableName;
	private int csvSourceLength;
	private String bcpConfigFile;
	private SortedMap<Integer, CsvConfig> bcpColumnConfig;
	private String bcpInsertStmnt;
	private Properties properties;
	
	private Map<String,String> bcpHeader = new HashMap<String,String>();
	private String bcpUtilLoc;
	private Map<String,String>containerTableMap;
	
	
	
	
	public BcpConfig(){}
	
	public BcpConfig(Properties properties){
		this.setProperties(properties);
	}
	
	
	public void initialize(){
		bcpColumnConfig = this.loadBcpConfigFile(this.bcpConfigFile);
		bcpInsertStmnt  = this.prepareSqlStatement(bcpColumnConfig, this.tableName );
	}
	
	
	private  SortedMap<Integer, CsvConfig>  loadBcpConfigFile(String fileName) {
		
		SortedMap<Integer, CsvConfig> csvToTableMapping = null;
		
		try {
			logger.info("loading xml config file:" + fileName);
			File xmlFile = new File(fileName);
			Document xmlConfig = XMLUtils.parse(xmlFile);
			XMLUtils.removeAllNamespaces(xmlConfig);
			List list = xmlConfig.selectNodes("//RECORD/FIELD");

			
			csvToTableMapping = new TreeMap<Integer, CsvConfig>();

			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Node field = (Node) iter.next();

				String id = field.valueOf("@ID");
				String xpathStr = "//ROW/COLUMN[@SOURCE='" + id + "']";

				Node column = field.selectSingleNode(xpathStr);

				CsvConfig csvConfig = new CsvConfig();
				csvConfig.setColumnName(column.valueOf("@NAME"));
				csvConfig.setColumnOrder(Integer.valueOf(id));
				
				csvConfig.setMaxLength(Integer.valueOf(field.valueOf("@MAX_LENGTH")));
				
				csvConfig.setColumnType(column.valueOf("@xsi:type"));
				csvConfig.setCsvColumnNumber(Integer.valueOf(column.valueOf("@CSVSOURCE")));

				csvToTableMapping.put(csvConfig.getColumnOrder(), csvConfig);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
			csvToTableMapping = null;
		} catch (Exception e) {
			e.printStackTrace();
			csvToTableMapping = null;
			
		}
		return csvToTableMapping;
	}

	private  String prepareSqlStatement(	SortedMap<Integer, CsvConfig> csvToTableMapping,
			final String tableName) {
		
		String columName = "";
		String values = "";
	
		for (Map.Entry<Integer, CsvConfig> entry : csvToTableMapping.entrySet()) {
			columName += entry.getValue().getColumnName() + ",";
			values += "?,";
		}
	
		String sqlString = " INSERT INTO " + tableName + "("
				+ columName.substring(0, columName.length() - 1) + ") VALUES("
				+ values.substring(0, values.length() - 1) + ")";
	
		return sqlString;
	
	}


	public String getTableName() {
		return tableName;
	}

	public String getTableName(String containerName) {
		logger.info("...............Table name:" + containerName);
		return this.containerTableMap.get(containerName);
		
		
	}

		
	

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	public int getCsvSourceLength() {
		return csvSourceLength;
	}


	public void setCsvSourceLength(int csvSourceLength) {
		this.csvSourceLength = csvSourceLength;
	}


	public String getBcpConfigFile() {
		return bcpConfigFile;
	}


	public void setBcpConfigFile(String bcpConfigFile) {
		this.bcpConfigFile = bcpConfigFile;
	}


	public SortedMap<Integer, CsvConfig> getBcpColumnConfig() {
		return bcpColumnConfig;
	}


	public void setBcpColumnConfig(SortedMap<Integer, CsvConfig> bcpColumnConfig) {
		this.bcpColumnConfig = bcpColumnConfig;
	}


	public String getBcpInsertStmnt() {
		return bcpInsertStmnt;
	}


	public void setBcpInsertStmnt(String bcpInsertStmnt) {
		this.bcpInsertStmnt = bcpInsertStmnt;
	}


	public String getKeyId() {
		return keyId;
	}


	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getProperties() {
		return properties;
	}

	
	public String getBcpUtilLoc() {
		return bcpUtilLoc;
	}

	public void setBcpUtilLoc(String bcpUtilLoc) {
		this.bcpUtilLoc = bcpUtilLoc;
	}

	public Map<String,String> getBcpHeader() {
		return bcpHeader;
	}

	public void setBcpHeader(Map<String,String> bcpHeader) {
		this.bcpHeader = bcpHeader;
	}
	
	public String getBcpHeader(String name){

		for ( Map.Entry<String, String> header: bcpHeader.entrySet()) {
			
			 if(name.toUpperCase().equals( header.getKey().toUpperCase() )){
				return header.getValue(); 
			 }
		}
		return null;
	}

	public Map<String, String> getContainerTableMap() {
		return containerTableMap;
	}

	public void setContainerTableMap(Map<String, String> containerTableMap) {
		this.containerTableMap = containerTableMap;
	}

}
