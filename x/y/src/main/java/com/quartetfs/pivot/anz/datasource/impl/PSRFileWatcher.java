package com.quartetfs.pivot.anz.datasource.impl;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.quartetfs.fwk.messaging.IFileListener;
import com.quartetfs.fwk.messaging.impl.DirectoryCSVWatcher;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.impl.PSRDetail;

public class PSRFileWatcher  extends DirectoryCSVWatcher
{
	private static final Logger LOGGER =  Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private static final long serialVersionUID = 186643841546680448L;
	private Set<String> alreadyProcessedFiles = new HashSet<String>();
	private PSRDetail psrDetail;	
	private Pattern psrPattern;
	
	public PSRFileWatcher(PSRDetail psrDetail,Properties prop)
	{
		this.psrDetail = psrDetail;
		this.psrPattern = Pattern.compile(prop.getProperty("psr.pattern"));
	}
	
	@Override
	protected void filesAction(Collection<String> added, Collection<String> modified, Collection<String> removed) 
	{
		if(CollectionUtils.isEmpty(added)) return;
		
		filterProcessedFiles(added);
		Collections.sort((List<String>)added);
		
		for(IFileListener listener : listeners)
    	{
    		listener.onFileAction(this, added, null, null);
    	}    		
    }
	
	public void filterProcessedFiles(Collection<String> files)
	{
		CollectionUtils.filter(files, new Predicate() {			
			@Override
			public boolean evaluate(Object object) {
				File file =  new File(object.toString());		
				
				if(!isValidPSR(file))
					return false;				
				boolean value = alreadyProcessedFiles.contains(file.getName());
				if(value)
				{
					LOGGER.warning(String.format("File %s already processed",file.getName()));
				}
				else
				{
					alreadyProcessedFiles.add(file.getName());
				}
				
				return value ? false:true;
			}

			private boolean isValidPSR(File file) {
				final Matcher m = psrPattern.matcher(file.getName());
				if(m.matches()) 
				{
					String psrName = m.group(1).substring(0, 5);
					
					if (!psrDetail.isValidPSR(psrName))
					{
						LOGGER.log(Level.INFO, MessagesANZ.NOT_ALLOWED_PSR,file.getPath());
						return false;
					}
				}
				else 
				{
					LOGGER.log(Level.WARNING, MessagesANZ.CANNOT_PARSE_PSR, file.getPath());
					return false;
				}
				return true;
			}
		});
	}
	
}
