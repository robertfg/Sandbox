package com.quartetfs.pivot.anz.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.transaction.ISchemaTransactionManager;
import com.quartetfs.fwk.transaction.TransactionException;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
 
public class TransactionUtils {
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	public static void rollback(ISchemaTransactionManager tm) throws TransactionException
	{
		try
		{
			if (tm.isTransactionStarted())
			{
				LOGGER.log(Level.SEVERE,MessagesANZ.ROLLBACK_CURRENT);
				tm.rollback();
			}	
		}
		catch (TransactionException e1) 
		{
			LOGGER.log(Level.SEVERE, MessagesANZ.ROLLBACK_PROBLEM, e1);
			throw new TransactionException(e1);
		}
	}
}
