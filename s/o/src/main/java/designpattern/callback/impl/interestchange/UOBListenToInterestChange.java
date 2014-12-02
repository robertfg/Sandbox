package designpattern.callback.impl.interestchange;

import designpattern.callback.ISubject;

public class UOBListenToInterestChange implements ISubject<Interest> {

	@Override
	public void recievedEvent(Interest o) {
	   System.out.println( "UOB Interest Price:" + o.getPrice() + "," + o.getInstrument() );
	   
		
	}

}
