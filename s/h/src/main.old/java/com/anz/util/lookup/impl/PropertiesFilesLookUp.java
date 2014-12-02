package com.anz.util.lookup.impl;

import com.anz.util.lookup.ILookUp;

public class PropertiesFilesLookUp implements ILookUp {

	@Override
	public Integer call() throws Exception {
		System.out.println("Im Now getting a PropertiesFileLookUp");
		return 2;
	}

}
