package com.quartetfs.pivot.anz.service.export;

public class ExtractHeader  {

	private int index;
	private int loc;
	
	private String name;
	private String type;
	private String overWriteIf;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getOverWriteIf() {
		return overWriteIf;
	}
	public void setOverWriteIf(String overWriteIf) {
		this.overWriteIf = overWriteIf;
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getLoc() {
		return loc;
	}
	public void setLoc(int loc) {
		this.loc = loc;
	}
}
