package com.quartetfs.pivot.anz.limits.parsing;

import java.util.Collection;

import com.quartetfs.fwk.messaging.IFileListener;
import com.quartetfs.fwk.messaging.impl.DirectoryCSVWatcher;

/**
 * 
 *
 */
public class LimitCSVWatcher extends DirectoryCSVWatcher {

	private static final long serialVersionUID = 6756088762624630157L;
		
	@Override
	protected void filesAction(Collection<String> added, Collection<String> modified, Collection<String> removed) {
		//TODO : Filter file list if required 
		for(IFileListener listener : listeners)
    	{
    		listener.onFileAction(this, added, modified, removed);
    	}
    		
    }

}
