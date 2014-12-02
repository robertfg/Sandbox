package com.quartetfs.pivot.anz.staticdata;

import java.io.File;

import com.quartetfs.fwk.messaging.IFileListener;


public interface IStaticDataLoader extends IFileListener {

	boolean  loadFile(File file) throws Exception;
	void addStaticDataListener(IStaticDataListener listener);
	void notify(IStaticData staticData);
}
