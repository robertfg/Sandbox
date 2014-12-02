package designpattern.callback.impl.interestchange;

import designpattern.callback.ISubjectMessage;

public class Interest implements ISubjectMessage {
 
	
	private float price;

	private String instrument;
	
	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public String getInstrument() {
		return instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}
	
	
}
