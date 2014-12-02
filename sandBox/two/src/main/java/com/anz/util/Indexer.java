package com.anz.util;

public class Indexer {

	
	public Indexer(String strIdentifier, int position) {
		super();
		this.strIdentifier = strIdentifier;
		this.position = position;
	}
	private String strIdentifier;
	 private int position;
	/**
	 * @param strIdentifier the strIdentifier to set
	 */
	public void setStrIdentifier(String strIdentifier) {
		this.strIdentifier = strIdentifier;
	}
	/**
	 * @return the strIdentifier
	 */
	public String getStrIdentifier() {
		return strIdentifier;
	}
	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	} 
	 
}
