package com.quartetfs.pivot.anz.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.apache.log4j.MDC;

import sun.security.action.GetPropertyAction;

import com.quartetfs.pivot.anz.utils.ANZConstants;

public class ContextFormatter extends Formatter {
	
	
	private static final GetPropertyAction GET_PROPERTY_ACTION = new sun.security.action.GetPropertyAction("line.separator");
	private static final GetPropertyAction ACTION = GET_PROPERTY_ACTION;
	Date dat = new Date();
	private final static String format = "{0,date} {0,time}";
	private MessageFormat formatter;

	private Object args[] = new Object[1];

	// Line separator string. This is the value of the line.separator
	// property at the moment that the SimpleFormatter was created.
	@SuppressWarnings("restriction")
	private String lineSeparator = (String) java.security.AccessController.doPrivileged(ACTION);
      
	/**   
	 * Format the given LogRecord.
	 *   
	 * @param record
	 *            the log record to be formatted.
	 * @return a formatted log record
	 */
	public synchronized String format(LogRecord record) {
		StringBuilder sb = new StringBuilder();
		// Minimize memory allocations here.
		dat.setTime(record.getMillis()); 
		args[0] = dat;
		StringBuffer text = new StringBuffer();
		if (formatter == null) {
			formatter = new MessageFormat(format);
		}
		formatter.format(args, text, null);
		sb.append(text);
		sb.append(" ");
		addContexValue(sb);		
		sb.append(" ");
		
		if (record.getSourceClassName() != null) {
			sb.append(record.getSourceClassName());
		} else {
			sb.append(record.getLoggerName());
		}
		sb.append(" ");
		//sb.append(lineSeparator);
		String message = formatMessage(record);
		sb.append(record.getLevel().getLocalizedName());		
		
		sb.append(" ");
		sb.append(message);
		sb.append(lineSeparator);
		if (record.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch (Exception ex) {
			}
		}
		return sb.toString();
	}

	private void addContexValue(StringBuilder sb) {
		Object value = MDC.get(ANZConstants.CONTEXT);
		if(value!=null)
		{
			sb.append(" [").append(value).append("]");
		}
	}
}
