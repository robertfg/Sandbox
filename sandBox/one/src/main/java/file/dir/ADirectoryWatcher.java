package file.dir;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import org.apache.log4j.Logger;
 
public class ADirectoryWatcher extends TimerTask implements IFileListener {
	private final static Logger logger = Logger.getLogger( ADirectoryWatcher.class );

	private String path;
	private String filter;
	private File filesArray[];
	
	private boolean firstTime;
	private HashMap<File, Long> dir = new HashMap<File, Long>();
	private DirectoryFilterWatcher dfw;
	private HashSet<File> checkedFiles = new HashSet<File>();

	private List<String> ignoreFileList = new ArrayList<String>();
	
	public ADirectoryWatcher(String path) {
		this(path, "");
	}

	public ADirectoryWatcher(String path, String filter) {
		logger.info("Watching directory:" + path );
		logger.info("Filter:" + filter);
		this.path = path;
	 	       dfw = new DirectoryFilterWatcher(filter);
		filesArray = new File(path).listFiles(dfw);
	}

	@SuppressWarnings("unchecked")
	public final void run() {
		if(firstTime){
			filesArray = new File(path).listFiles(dfw);
		    try{
		    	for (int i = 0; i < filesArray.length; i++) {
		    		
		    		boolean ignoreFile = false;
		    		for (String fileName : ignoreFileList) { 
						if(filesArray[i].getName().indexOf(fileName)!=-1){
							ignoreFile = true;
							break;
						}
					}
		    		
		    		if(ignoreFile){
		    			continue;
		    		}
		    		
		    		Long current = dir.get(filesArray[i]);
					checkedFiles.add(filesArray[i]);
					
					if (current == null) {
						dir.put(filesArray[i], new Long(filesArray[i].lastModified()));
						onChange(filesArray[i], "add");
					} else if (current.longValue() != filesArray[i].lastModified()) {
						dir.put(filesArray[i], new Long(filesArray[i].lastModified()));
						onChange(filesArray[i], "modify");
					} else { 
						onChange(filesArray[i], "reprocess");
										
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

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void onChange(File file, String action) {
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
}
