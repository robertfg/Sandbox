package com.anz.rer.etl.polling;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.anz.rer.etl.directory.IFileListener;
import com.anz.rer.etl.directory.impl.DirectoryFilterWatcher;

public abstract class ADirectoryPolling extends APollingTask implements IFileListener {
   
	private final static Logger logger = Logger.getLogger( ADirectoryPolling.class );

	private String rootDir;
	private String filter;
	
	private boolean firstTime;
	private HashMap<File, Long> dir = new HashMap<File, Long>();
	private DirectoryFilterWatcher dfw;
	private HashSet<File> checkedFiles = new HashSet<File>();

	private List<String> ignoreFileList = new ArrayList<String>();
	
	private Pattern refPattern;
	
	
	public ADirectoryPolling(int interval, long startDelay) {
		super(interval, startDelay);
	
	}

	public ADirectoryPolling(int interval, long startDelay, String path, String filter) {
		super(interval, startDelay);
		logger.info("Watching directory:" + path );
		logger.info("Filter:" + filter);
		this.rootDir = path;
	 	       dfw = new DirectoryFilterWatcher(filter);
		//filesArray = new File(path).listFiles(dfw);
		refPattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
	}

	@SuppressWarnings("unchecked")
	public final void run() {
		
		traverseDir( rootDir );
	} 

	private void traverseDir(String path){
		if(firstTime){
			File[] filesArray = new File(path).listFiles(); // .listFiles(dfw);
		    try{
		    	for (int i = 0; i < filesArray.length; i++) {
		    	  if(filesArray[i].isDirectory() ){
		    		  traverseDir( filesArray[i].getAbsolutePath() );
		    	  }	else {
		    		  String name = filesArray[i].getName().replace("#", "");
		    		  if( refPattern.matcher(name.trim()).matches()) {
							for (String fileName : ignoreFileList) { 
								if(filesArray[i].getName().indexOf(fileName)!=-1){
									return;
								}
							}
				    		Long current = dir.get(filesArray[i]);
							checkedFiles.add(filesArray[i]);
							if (current == null) {
								dir.put(filesArray[i], new Long(filesArray[i].lastModified()));
								onChange(filesArray[i], "add");
							} else if (current.longValue() != filesArray[i].lastModified()) {
								dir.put(filesArray[i], new Long(filesArray[i].lastModified()));
								onChange(filesArray[i], "modify");
							}
		    	  	}	else {
		    	  		//System.out.println("dont match:" +  filesArray[i].getAbsolutePath());
		    	  		checkedFiles.add(filesArray[i]);
		    	  	}
		    	  }
					
				
		    	}
		    	
		
				// now check for deleted files
				Set<File> ref = ((HashMap<File, Long>) dir.clone()).keySet();
				ref.removeAll((Set<File>) checkedFiles);
				Iterator<File> it = ref.iterator();
				while (it.hasNext()) {
					File deletedFile = it.next();
					dir.remove(deletedFile);
					onChange(deletedFile, "delete"); 
				}
				
				
				
				
		    } catch (java.lang.NullPointerException e){
		    	logger.info("Please configure the source directory");
		    	e.printStackTrace();
		    } catch(Exception e){
		    	logger.info(e);
		    	e.printStackTrace();
		    }
		}
		firstTime = true;
		
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getFilter() {
		return filter;
	}

	

	public HashSet<File> getCheckedFiles() {
		return checkedFiles;
	}

	public void setCheckedFiles(HashSet<File> checkedFiles) {
		this.checkedFiles = checkedFiles;
	}

	public List<String> getIgnoreFileList() {
		return ignoreFileList;
	}

	public void setIgnoreFileList(List<String> ignoreFileList) {
		this.ignoreFileList = ignoreFileList;
	}

	public String getRootDir() {
		return rootDir;
	}

	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}
}
