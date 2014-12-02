package com.anz.parser.impl;

import java.util.*;
import java.io.*;

public class BigDaddy implements Iterable<String> {
	private BufferedReader _reader;

	public static void main(String args[]) {
		long t = System.currentTimeMillis();
		int x = 0;
		BigDaddy file;
		try {
			file = new BigDaddy("C:\\devs\\pnl.APX");

			List<String> mapped = new ArrayList<String>();
			int sizing = 100000;
			int ctr = 0;
			for (String line : file) {
				System.out.println(ctr);
				ctr++;
			//	mapped.add(line);
 
//				if (sizing == ctr) {
//					// add to future
//				//	System.out.println(mapped.toString());
//					ctr = 0;
//				//	mapped = null;
//				//	mapped = new ArrayList<String>();
//				}
			//	ctr++;
				x++;
			} //5904285

			System.out.println(x);
			t = System.currentTimeMillis() - t;

			System.out.println(" read speed to memory:" + t + " ms");

		} catch (Exception e) {
			System.out.println(x);
			e.printStackTrace();
		}
		// System.out.println("" + file._reader.);
		// for (String line : file)
		// System.out.println(line);
	}

	public BigDaddy(String filePath) throws Exception {
		_reader = new BufferedReader(new FileReader(filePath));
	}

	public void Close() {
		try {
			_reader.close();
		} catch (Exception ex) {
		}
	}

	public Iterator<String> iterator() {
		return new FileIterator();
	}

	private class FileIterator implements Iterator<String> {
		private String _currentLine;
		private int count;

		List<String> mapped = new ArrayList<String>();
		int sizing = 10000;
		int ctr = 0;
		
		public boolean hasNext() {
			try {
				count++;
				_currentLine = _reader.readLine();
				
				/*mapped.add(_reader.readLine());
				 
				if (sizing == ctr) {
					// add to future
				//	System.out.println(mapped.toString());
					ctr = 0;
					mapped = null;
					mapped = new ArrayList<String>();
				} 
				ctr++;*/
				
			} catch (Exception ex) {
				_currentLine = null;
				ex.printStackTrace();
			}

			return _currentLine != null;
		}

		public String next() {
			return _currentLine;
		}

		public void remove() {
		}

	}
}