package com.anz.parser.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MergeFile {

	
	
	/**
	 * read first file, create array of 2 dimensional array[byte[]],[int]
	 * read second file, create map<id>,<fileIndexPosition>
	 * 
	 * divide the first fille map
	 * 
	 * 
	 */
	
	 public void test() {
		 //Map<byte, int> indexes = 
	 }
	 
	 
	 public static void main(String[] args){
		 
		 
		 List<Index> y = new ArrayList<Index>();
	      
		  for (int i = 0; i < 1000000 ; i++) {
			
		 //     y.add(new Index("sup"));	  
		}
		 
		 
	
	 }
	 
	 private class Index {
		 
		 public Index(byte[] strIdentifier, int position) {
			super();
			this.strIdentifier = strIdentifier;
			this.position = position;
		}
		private byte[] strIdentifier;
		 private int position;
		/**
		 * @param strIdentifier the strIdentifier to set
		 */
		public void setStrIdentifier(byte[] strIdentifier) {
			this.strIdentifier = strIdentifier;
		}
		/**
		 * @return the strIdentifier
		 */
		public byte[] getStrIdentifier() {
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
}
