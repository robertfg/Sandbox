package designpattern.callback.impl.pricechange;

import designpattern.callback.ISubject;

public class UOBListenToPriceChange implements ISubject<Price> {

	@Override
	public void recievedEvent(Price o) {
	   System.out.println( "I Got the message BankAListenToSubject:" + o.getPrice() );
		
	}

}
