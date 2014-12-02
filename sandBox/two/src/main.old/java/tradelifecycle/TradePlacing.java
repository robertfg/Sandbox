package tradelifecycle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class TradePlacing {

	ConcurrentHashMap<String,List<Trade>> tradeTransaction
	        = new ConcurrentHashMap<String, List< Trade>>();
	
	
	public void place(Trade trade){
     	String key =  trade.getActNumber() ;
		
     	if(tradeTransaction.get(key) == null ){
			List<Trade> trades = new ArrayList<Trade>();
			            trades.add(trade);  
		
			            tradeTransaction.put( key, trades);
				
		} else {
			tradeTransaction.get(key).add(trade);

		}
	}

	public Trade aggregate( String acctNum, String secIndent, int[] tradeIds){
	
		 List<Trade> trades = tradeTransaction.get(acctNum);
		 
		 Trade result = new Trade();
		 boolean first = true;
	
		 for (Trade trade : trades) {
			 
		    if( secIndent.equals( trade.getSecIdentifier()) &&  this.validId(trade.getId(), tradeIds) ){
		    	      if( first){
						 result = trade;
						 first = false;
					
					 } else {
						 if( result.getVersion() < trade.getVersion() ){
							 result = trade;
							 
						 } else {
							
							 if(  result.getDirection().equals( Direction.BUY )  ){
								 
			                 
								if( trade.getDirection().equals(Direction.SELL) )	{
									if( trade.getOperation().equals(Operation.CANCEL)   ){
										result.setQuantity( result.getQuantity() + trade.getQuantity());
									}else{
										result.setQuantity(  result.getQuantity() - trade.getQuantity() );
		                      		
									}
								} else if( trade.getDirection().equals(Direction.BUY) ){
		                      		if( trade.getOperation().equals(Operation.NEW) || trade.getOperation().equals(Operation.AMEND)  ){
		                      			result.setQuantity( result.getQuantity() + trade.getQuantity());
		                      		} else {
									   result.setQuantity( trade.getQuantity()); 
		                      		}
								}
								
						     } else if( result.getDirection().equals( Direction.SELL) ) {
						    	 
						    	    if( trade.getDirection().equals(Direction.BUY) )	{
						    	    	if( trade.getOperation().equals(Operation.CANCEL)   ){
											result.setQuantity( result.getQuantity() - trade.getQuantity());
						    	    	} else {	
						    	    		result.setQuantity(  result.getQuantity() + trade.getQuantity() );
						    	    	}
						    	    } else if( trade.getDirection().equals(Direction.SELL) ){
			                      		if( trade.getOperation().equals(Operation.NEW) || trade.getOperation().equals(Operation.AMEND)  ){
			                      			result.setQuantity( result.getQuantity() - trade.getQuantity());
				                      		
			                      		}else {		
			                      			result.setQuantity( trade.getQuantity());
			                      		}
			                      	}
						    	 
						     }
								
						 }
						
					 }
					 result.setVersion( trade.getVersion()); 
						result.setDirection( trade.getDirection() );
		 		}
		 }
		 
		 return result; 
	}
	
	private boolean validId(int tradeId, int[] tradeIds){
		  
		for (int i = 0; i < tradeIds.length; i++) {
			if(tradeIds[i] == tradeId ){
				return true;
			}
		}
		 
		return false;
	}
}
