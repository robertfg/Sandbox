package designpattern.callback.impl.pricechange;

import designpattern.callback.ISubject;

public class DBSListenToPriceChange implements ISubject<Price> {

	@Override
	public void recievedEvent(Price o) {
		System.out.println("DBS:" + o.getPrice());
		
	}

}
