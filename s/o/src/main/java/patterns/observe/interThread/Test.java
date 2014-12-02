package patterns.observe.interThread;

public class Test {

	public static void main(String args[]){
		
		  A a1 = new A();
		  A a2 = new A();
		  A a3 = new A();
		
		 
		  
	      final B b = new B();      // register a       
	              b.addObserver(a1);
	              b.addObserver(a2);
	              b.addObserver(a3);
	               
	           
	              
	      new Thread(new Runnable(){

	            public void run() {

	                  b.done();      // when b is done, a is notified

	            }                

	      }).start();
	}
	
}
