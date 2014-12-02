package com.quartetfs.pivot.anz.service.export;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExtractCoordinator {

	private ConcurrentMap<Long, ICoordinator> transaction = new ConcurrentHashMap<Long, ICoordinator>();
	
	public synchronized boolean transactionDone(ICoordinator iCoordinator) {

		ICoordinator coordinator = transaction.get( iCoordinator.getId() );
		
		if( coordinator!=null  ){
			coordinator.incrementCount(1); 
			transaction.replace(coordinator.getId(), coordinator);
		} else {
			iCoordinator.incrementCount(1); 
		    transaction.put( iCoordinator.getId(), iCoordinator );
			
		}
		
		coordinator = transaction.get( iCoordinator.getId() );
				
		if( coordinator.getCount() == coordinator.getTotalCount() ){
			   transaction.remove( coordinator.getId() );
			   return true;
		}
		return false;

	}
}
