package com.anz.rer.etl.subcube;

import java.io.File;

import org.apache.log4j.Logger;

import com.anz.rer.etl.directory.IFileProcessor;
import com.anz.rer.etl.directory.IFileResolver;
import com.anz.rer.etl.mxHierarchy.MxHierarchyImpl;



public class SubCubeDataFiller implements IFileProcessor {
	
	private final static Logger logger = Logger.getLogger(MxHierarchyImpl.class);
	private IFileResolver fileResolver;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean validate(File fileName) {
		
		return false;
	}


	@Override
	public boolean preProcess() {
		
		return false;
	}


	@Override
	public boolean doProcess() {
		
		return false;
	}


	@Override
	public boolean postProcess() {
		
		return false;
	}


}
