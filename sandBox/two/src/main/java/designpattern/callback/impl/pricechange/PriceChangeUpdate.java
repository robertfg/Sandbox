package designpattern.callback.impl.pricechange;

import designpattern.callback.ACallbackManager;
import designpattern.callback.ISubject;

public class PriceChangeUpdate extends ACallbackManager {

	public static void main(String[] args) {
		PriceChangeUpdate priceChange = new PriceChangeUpdate();
		
		ISubject UOB =  new UOBListenToPriceChange();
		ISubject DBS =  new DBSListenToPriceChange();
		 
		priceChange.registerCallBack(UOB);
		priceChange.registerCallBack(DBS);
		
		Price latestPrice = new Price();
		latestPrice.setPrice(100);
		
		priceChange.setPredicate(latestPrice);
		
		
		
		
	}
	
	
	

}
