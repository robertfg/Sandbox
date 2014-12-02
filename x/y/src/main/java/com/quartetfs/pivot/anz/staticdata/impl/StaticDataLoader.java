package com.quartetfs.pivot.anz.staticdata.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.bind.ValidationException;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.fwk.messaging.IFileWatcher;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.staticdata.IStaticData;
import com.quartetfs.pivot.anz.staticdata.IStaticDataListener;
import com.quartetfs.pivot.anz.staticdata.IStaticDataLoader;

/**
 * This class is listener to file watcher and uses StaticDataFileParser to parse a static data file.
 * It can be multithreaded  , by default it uses one thread to parse file therefore only one file is parsed at a time.
 * 
 * Responsibility
 * 1. Validate incoming file for correct name 
 * 2. Submit file information to Worker pool for parsing
 * 
 * @author Quartet Financial System
 *
 */
public class StaticDataLoader implements IStaticDataLoader {

	private Pattern refPattern;
	private List<IStaticDataListener> listeners=new ArrayList<IStaticDataListener>();
	static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME,MessagesANZ.BUNDLE);
	private DateParser parser;
	 
	 public StaticDataLoader(){
	    parser  =(DateParser) Registry.getPlugin(IParser.class).valueOf("date[ddMMyyyy]");
	 }
	
	@Override
	public boolean loadFile(File file)throws Exception {
		
		IDate hierarchyDate=extractHierarchyDate(file);
		StaticDataType hierarchyType=extractStaticDataType(file);
		if (hierarchyType==null){
			return false;
		}
		validate(hierarchyDate,hierarchyType, file.getPath());
		
		IStaticData staticData = hierarchyType.getParser().parse(file,hierarchyDate,hierarchyType );
		if (staticData!=null){
			StaticDataLoader.this.notify(staticData);
		}	
		return true;
		
	}
	
	private void validate(IDate hierarchyDate, StaticDataType hierarchyName,String path)throws ValidationException {
		if (hierarchyDate==null || hierarchyName==null){
			throw new ValidationException(String.format("%s Invalid hierarchy Date and/or Name, while parsing file %s","StaticDataLoader",path));
		}
		
	}
	
	
	public IDate extractHierarchyDate(File file){
		File parent=file.getParentFile();
		File grandParent=null;
		IDate result=null;
		if (parent!=null){
			grandParent=parent.getParentFile();
		}
		
		if (grandParent!=null){
			String dateStr=grandParent.getName();
			//DateFormat formatter=new SimpleDateFormat("ddMMyyyy");
			
			java.util.Date date;
			try {
				 //date = parser.parse(dateStr);//formatter.parse(dateStr);
				 result = Registry.create(IDate.class, parser.parse(dateStr).getTime() );
				//result=new Date( date);
				result.applyTime(0, 0, 0, 0);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				return null;
			}
			
		}
		return result;
	}
	
	public StaticDataType extractStaticDataType(File file){
		String fileName=file.getName().substring(0,file.getName().indexOf("_"));
		if(fileName==null){
			LOGGER.log(Level.WARNING,"Invalid file name unable to extract StaticDataType from file Name, character _ missing");
			return null;
		}
		StaticDataType dataType=StaticDataType.fromString(fileName);
		return dataType;
	}
	
	


	@Override
	public void onFileAction(IFileWatcher watcher, Collection<String> newFiles,
			Collection<String> modifiedFiles, Collection<String> deletedFiles) {

		if ((newFiles != null && !newFiles.isEmpty())) {
			for (final String filePath : newFiles) {
				if (refPattern.matcher(filePath).matches()) {
					File file = new File(filePath);
					try{
						
						boolean result = loadFile(file);
						if (!result) {
							LOGGER.log(Level.SEVERE, String.format("Unable to parse static data file %s",file.getAbsolutePath()));
						}else{
							LOGGER.log(Level.INFO,String.format("Sucessfully parsed Static Data file %s",file.getAbsolutePath()));
						}
					}catch (InterruptedException e) {
						LOGGER.log(Level.SEVERE,"Interruppted when trying to read from queue in StaticDataLoader: "+e.getMessage(),e);
					} catch (ValidationException e) {
						LOGGER.log(Level.SEVERE ,String.format("Validation exception when parsing static data file %s %s",file,e.getMessage()),e);
					}catch(Exception e){
						LOGGER.log(Level.SEVERE ,String.format("exception when parsing static data file %s %s",file,e.getMessage(),e));
					}	

				}
			}
		}

	}

	public void setReferencePattern(String pattern) {
		this.refPattern = Pattern.compile(pattern);
	}

	@Override
	public void addStaticDataListener(IStaticDataListener listener) {
		listeners.add(listener);
		
	}

	@Override
	public void notify(IStaticData staticData) {
		for (IStaticDataListener listener:listeners){
			listener.onStaticDataCompleted(staticData);
		}
	}
	
}
