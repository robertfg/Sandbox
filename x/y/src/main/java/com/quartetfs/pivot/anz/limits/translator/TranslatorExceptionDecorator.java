package com.quartetfs.pivot.anz.limits.translator;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.fwk.messaging.ILineReader;
import com.quartetfs.fwk.messaging.impl.MapTranslator;
import com.quartetfs.fwk.util.IMappedTuple;
import com.quartetfs.pivot.anz.impl.MessagesANZ;

public class TranslatorExceptionDecorator extends MapTranslator{
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	@Override 
	public IMappedTuple translate(ILineReader reader){
		IMappedTuple row= super.translate(reader);
		if(row==null){
			LOGGER.log(Level.SEVERE, String.format("Error in parsing %s file", reader.getCurrentFile().getFileAbsolutePath()));
		}
		return row;
	}
}
