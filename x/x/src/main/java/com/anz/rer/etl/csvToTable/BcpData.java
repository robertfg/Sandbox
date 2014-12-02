package com.anz.rer.etl.csvToTable;

import java.util.List;
import java.util.Map;

public class BcpData {
	
	private String name;
	private List<String[]> csv;
	private int id;
	private int totalLineNumber;
	private int columnPerRow;
	private int totalPartition;
	private int tranCtr;
	
	/*private BcpConfig bcpConfig;*/
	private String status;
	private String filePath;
	private long start;
	private long end;
	private String state;
	private int retry;
	private boolean done;
	private String[] header;
	
	private String batchId;
	private String psrCode;
	private String varType;
	private String cobDate;
	private String containerName;
	private String extractType;
	private String key;
	private String hUid;
	
	
	public BcpData(){}

	public BcpData(String name,String filePath, String status){
		 this(name, filePath);
	     this.setStatus(status);
	}
	
	public BcpData(String name,String filePath){
		 this(name);
	     this.filePath = filePath;	 
	}
	
	public BcpData(String name) {
		 this.name = name;
		 this.parseBcpDataName(name);
	}
		
	public BcpData(String name, String status, String filePath, String[] header){
		 this.name = name;
		 this.setFilePath( filePath );
		 this.setStatus(status); 
	  	 this.parseBcpDataName(name);
	  	 this.header = header;
	  	 
	}
	
	public BcpData(String name,boolean done, String[] header){
		this.done = done;
		this.name = name;
		this.header = header;
	}

	public BcpData(List<String[]> csv, int id, int columnPerRow,
			int totalPartition,/*BcpConfig bcpConfig,*/ String name, int totalLineNumber, long start) {
		super();
		this.csv = csv;
		this.id = id;
		this.columnPerRow = columnPerRow;
		this.totalPartition = totalPartition;
		//this.bcpConfig = bcpConfig;
		this.name = name;
		this.totalLineNumber = totalLineNumber;
		this.start = start;
		this.parseBcpDataName(name);
	}
	

	public BcpData(List<String[]> csv, int id, int columnPerRow,
			int totalPartition, String name, int totalLineNumber, long start, String filePath) {
	   	    this(csv, id, columnPerRow,totalPartition, name,  totalLineNumber,start);
	   	    this.filePath = filePath;
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String[]> getCsv() {
		return csv;
	}
	public void setCsv(List<String[]> csv) {
		this.csv = csv;
	}

	public void setColumnPerRow(int columnPerRow) {
		this.columnPerRow = columnPerRow;
	}
	public int getColumnPerRow() {
		return columnPerRow;
	}
	public void setTotalLineNumber(int totalLineNumber) {
		this.totalLineNumber = totalLineNumber;
	}
	public int getTotalLineNumber() {
		return totalLineNumber;
	}
	public void setTotalPartition(int totalPartition) {
		this.totalPartition = totalPartition;
	}
	public int getTotalPartition() {
		return totalPartition;
	}

	public void setTranCtr(int tranCtr) {
		this.tranCtr = tranCtr;
	}

	public int getTranCtr() {
		return tranCtr;
	}

	/*public void setBcpConfig(BcpConfig bcpConfig) {
		this.bcpConfig = bcpConfig;
	}

	public BcpConfig getBcpConfig() {
		return bcpConfig;
	}*/

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getEnd() {
		return end;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getStart() {
		return start;
	}
	
	public String totalExecutionTime(String header,long end){
		return header + ":" + this.getName() + " ["  + ( ( end - this.getStart()) )   +" ms ]";
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getState() {
		return state;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public int getRetry() {
		return retry;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public String[] getHeader() {
		return header;
	}

	public void setHeader(String[] header) {
		this.header = header;
	}
	
	private void parseBcpDataName(String bcpDataName){
		 
		String[] parsedName = bcpDataName.replace(".APX", "").split("#");
		
		  psrCode = parsedName[0];
		  
		  containerName= parsedName[1];
		  
		  cobDate = parsedName[4];
		  batchId = parsedName[5];
		
		  try{
			  varType = parsedName[2].split("-")[1]; // PNL-VAR_1500
		  } catch(Exception e){
			  varType = parsedName[2];
		  }
		  
		  extractType = parsedName[8];
		  
		  key = varType + "#" + extractType + "#" + cobDate;
	}



	public String getBatchId() {
		return batchId;
	}



	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}



	public String getPsrCode() {
		return psrCode;
	}



	public void setPsrCode(String psdCode) {
		this.psrCode = psdCode;
	}



	public String getVarType() {
		return varType;
	}



	public void setVarType(String varType) {
		this.varType = varType;
	}



	public String getCobDate() {
		return cobDate;
	}



	public void setCobDate(String cobDate) {
		this.cobDate = cobDate;
	}



	public String getContainerName() {
		return containerName;
	}



	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String getExtractType() {
		return extractType;
	}

	public void setExtractType(String extractType) {
		this.extractType = extractType;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String gethUid() {
		return hUid;
	}

	public void sethUid(String hUid) {
		this.hUid = hUid;
	}
	
	
	
}
