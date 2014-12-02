package patterns.observe.oldschool;

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

	public void update(String availabiliy) {
		
		
		System.out.println("Hello "+personName+", Product is now "+availabiliy+" on flipkart");
	    try {
			System.out.println("i'm going to sleep put block");
	    	Thread.sleep(5000);
	    	System.out.println("i'm awake now");
	    	
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	    
	}
}