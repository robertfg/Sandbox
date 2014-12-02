package com.anz.parser.impl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Mapper implements Runnable{
	String fileName;
	
	
	public Mapper(String fileName) {
		super();
		this.fileName = fileName;
	}



	public Mapper() {

	}



	public List<byte[]> file2list(String file) throws Exception {
		 long t = System.currentTimeMillis();
		InputStream in = null;
		byte[] buf = null; // output buffer
		int bufLen = 20000 * 1024; // == 25MB // 23 seconds, even if memory was set to 1G
		List<byte[]> data = new ArrayList<byte[]>(); // keeps peaces of data
		try {
			in = new BufferedInputStream(new FileInputStream("C:\\devs\\murex_files\\" + file));
			buf = new byte[bufLen];
			byte[] tmp = null;
			int len = 0;
			int ctr=0;
			int byteCount = 0;
			while ((len = in.read(buf, 0, bufLen)) != -1) {
				for( byteCount = buf.length -1; byteCount >= 0; byteCount--) {
					if(buf[byteCount] == 13 && buf[byteCount - 1] == 44 
					|| ( buf[byteCount]==10 && buf[byteCount -1] == 13 
						 && buf[byteCount -2] == 44)) { // 44 is the line comma delimeter which is mandatory	
						if(tmp!=null){
							int tmpLen = tmp.length;
							byte[] tmpContent = tmp;
							tmp = new byte[ tmpLen +  byteCount];  
							System.arraycopy(tmpContent, 0, tmp, 0,  tmpLen);
							System.arraycopy(buf, 0, tmp, tmpLen,  byteCount); 
						} else {
							tmp = new byte[byteCount];
							System.arraycopy(buf, 0, tmp, 0, byteCount); 
						}
						data = new ArrayList<byte[]>();
						data.add( tmp );
						tmp = null;
						tmp = new byte[buf.length-1  - ( (byteCount) ) ];
						System.arraycopy(buf, byteCount + 1 , tmp, 0, tmp.length );
						break;
					}
				}
			  
				/* this can be put outside or if you want to process it immediately put it this way */
				Runnable reduce = new Reducer(data); // can be put as injection, write to split file or wite to memory
				new Thread(reduce).start();
					data = null;
					ctr++;
			}
			
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		   t = System.currentTimeMillis()-t; 
		   System.out.println(" read speed to memory:"+t+" ms");

		return data;
	}
	
	
	
	public static void main(String[] args){
		
		Mapper m = new Mapper();
		
		String filename = "C:\\devs\\etl\\VXAL0_BM00#VR111111_VR2\\VXAL0_BM00#VR111111_VR2.csv";
		try {
		//	filename = "C:\\devs\\murex_files\\testcr.txt";
			
			m.file2list(filename);
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
//		
	}



	@Override
	public void run() {
		try {
			file2list(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
