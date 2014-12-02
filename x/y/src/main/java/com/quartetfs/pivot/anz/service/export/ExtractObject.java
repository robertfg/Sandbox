package com.quartetfs.pivot.anz.service.export;

import java.util.List;


public class ExtractObject implements ICoordinator{
	
	private List<String[]> rows;
	private ExtractType extractType;      

	private String filePath;

	private boolean done;
	private int count;
	private int totalCount;
	private long id;
	private boolean fromFM;
	
	
   
	
	public ExtractObject(long id, boolean done) {
		this.id = id;
		this.done = done;	

	}
 
	public ExtractObject(long id, List<String[]> rows,  boolean done, ExtractType extractType, int totalCount) {
		this.id = id;
		this.setRows(rows);
		this.done = done;
		this.extractType = extractType;
		this.totalCount = totalCount;
		
	
	}
	
	public ExtractObject(long id, List<String[]> rows,  boolean done, ExtractType extractType, String filePath, int totalCount) {
		this(id,rows,done,extractType,totalCount);
		this.filePath = filePath;
		this.totalCount = totalCount;
	}

	public ExtractObject(long id, List<String[]> rows,  boolean done, ExtractType extractType, String filePath, int totalCount, boolean fromFM) {
		this(id,rows,done,extractType,totalCount);
		this.filePath = filePath;
		this.totalCount = totalCount;
		this.fromFM = fromFM;
	}

	
	public List<String[]> getRows() {
		return rows;
	}

	public void setRows(List<String[]> rows) {
		this.rows = rows;
	}

	
	public ExtractType getExtractType() {
		return extractType;
	}

	public void setExtractType(ExtractType extractType) {
		this.extractType = extractType;
	}


	public enum ExtractType {
		 VAR_CONFIDENCE, VAR_PNL, NON_VAR, HYPO, VAR_STRESS,VAR_STRESS_PNL,VAR_PNL_PORTFOLIO,VAR_STRESS_PNL_PORTFOLIO,HYPO_NODE,VAR_STRESS_CONFIDENCE,
		 VAR_SIX_YEAR_CONFIDENCE, VAR_SIX_YEAR_PNL,VAR_SIX_YEAR_PNL_PORTFOLIO;  //; is required here.
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}


	@Override
	public Long getId() {
		return id; 
	}

	@Override
	public Integer getCount() {
		
		return count;
	}

	@Override
	public void incrementCount(int count) {
		count++;
	}

	@Override
	public Integer getTotalCount() {
		return totalCount;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	public boolean isFromFM() {
		return fromFM;
	}

	public void setFromFM(boolean fromFM) {
		this.fromFM = fromFM;
	}
	
}
