package designpattern.callback.impl.interestchange;

import designpattern.callback.ISubject;

public class DBSListenToInterestChange implements ISubject<Interest> {

	@Override
	public void recievedEvent(Interest o) {
		System.out.println( "DBS Interest Price:" + o.getPrice()  + "," + o.getInstrument() );
		
	}

}
