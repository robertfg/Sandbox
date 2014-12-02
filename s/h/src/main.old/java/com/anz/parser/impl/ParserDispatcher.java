package com.anz.parser.impl;

import com.anz.parser.IFileDispatcher;

public class ParserDispatcher implements IFileDispatcher{

	/**
	 *  How is the file differentiate from one another
	 */
	public void dispatchFile(String fileName) {
		String beanId = "fileIdentifier";
		this.analyzefile(fileName);
	}
	
	/**
	 * apply bussiness logic in determining the file
	 * @param fileName
	 * @return
	 */
	private String analyzefile(String fileName){
		
		System.out.println("Start Analyzing:" + fileName);
				Runnable mapper = new Mapper(fileName);
						new Thread(mapper).start();
		System.out.println("ended Analyzing:" + fileName);
		return "test1";
		
	}
	
	
}
