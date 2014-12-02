package com.anz.parser.impl;

import com.anz.file.impl.DirectoryWatcherImpl;
import com.anz.parser.impl.ParserDispatcher;


public class DirWatcherTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 DirectoryWatcherImpl d =  new DirectoryWatcherImpl();
		  d.setFileDispatcher(new ParserDispatcher());
		 
	}
}
