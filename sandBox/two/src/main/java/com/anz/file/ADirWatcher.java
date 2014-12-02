package com.anz.file;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

public abstract class ADirWatcher extends TimerTask {
	private String path;
	private File filesArray[];
	private HashMap<File, Long> dir = new HashMap<File, Long>();
	private DirectoryFilterWatcher dfw;

	public ADirWatcher(String path) {
		this(path, "");
	}

	public ADirWatcher(String path, String filter) {
		this.path = path;
		dfw = new DirectoryFilterWatcher(filter);
		filesArray = new File(path).listFiles(dfw);

		// transfer to the hashmap be used a reference and keep the
		// lastModfied value
		
		//for (int i = 0; i < filesArray.length; i++) {
		//	dir.put(filesArray[i], new Long(filesArray[i].lastModified()));
		//}
	}

	@SuppressWarnings("unchecked")
	public final void run() {
		HashSet<File> checkedFiles = new HashSet<File>();
		filesArray = new File(path).listFiles(dfw);

		// scan the files and check for modification/addition
		for (int i = 0; i < filesArray.length; i++) {
			Long current = dir.get(filesArray[i]);
			checkedFiles.add(filesArray[i]);
			
			if (current == null) {
				// new file
				dir.put(filesArray[i], new Long(filesArray[i].lastModified()));
				onChange(filesArray[i], "add");
			
			} else if (current.longValue() != filesArray[i].lastModified()) {
				// modified file
				dir.put(filesArray[i], new Long(filesArray[i].lastModified()));
				onChange(filesArray[i], "modify");
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
	}

	protected abstract void onChange(File file, String action);
}