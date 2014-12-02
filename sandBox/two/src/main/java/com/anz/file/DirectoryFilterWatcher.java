package com.anz.file;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFilterWatcher implements FileFilter {
	private String filter;

	public DirectoryFilterWatcher() {
		this.filter = "";
	}

	public DirectoryFilterWatcher(String filter) {
		this.filter = filter;
	}

	public boolean accept(File file) {
		if ("".equals(filter)) {
			return true;
		}
		return (file.getName().endsWith(filter));
	}
}