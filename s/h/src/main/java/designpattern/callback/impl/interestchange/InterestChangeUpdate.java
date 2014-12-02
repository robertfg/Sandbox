package designpattern.callback.impl.interestchange;

import designpattern.callback.ACallbackManager;
import designpattern.callback.ISubject;

public class InterestChangeUpdate extends ACallbackManager {
	
	public static InterestChangeUpdate priceChange = new InterestChangeUpdate();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {
		
		ISubject UOB =  new UOBListenToInterestChange();
		ISubject DBS =  new DBSListenToInterestChange();
		 
		priceChange.registerCallBack(UOB);
		priceChange.registerCallBack(DBS);
		
		Interest interest = new Interest();
				 interest.setPrice(100);
				 interest.setInstrument("fantastic");
		
		priceChange.setPredicate(interest);
	}
	
	
	
	

}
