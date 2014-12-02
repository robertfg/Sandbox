package designpattern.callback.impl.pricechange;

import designpattern.callback.ISubjectMessage;

public class Price implements ISubjectMessage {
 
	
	private float price;

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
	
	
}
