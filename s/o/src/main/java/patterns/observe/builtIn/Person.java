package patterns.observe.builtIn;

import java.util.Observable;
import java.util.Observer;

public class Person implements Observer{

	String personName;
	
	
	public Person(String personName) {
		this.personName = personName;
	}


	public String getPersonName() {
		return personName;
	}


	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public void update(Observable arg0, Object arg1) {
		
		System.out.println("Hello "+personName+", Product is now "+arg1+" on flipkart");
		try {
			System.out.println("i'm going to sleep put block");
	    	Thread.sleep(5000);
	    	System.out.println("i'm awake now");
	    	
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	    
	}

}