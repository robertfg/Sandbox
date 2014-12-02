package designpattern.singleton;

public class Singleton {

	
	private Singleton singleton;
	
	private Singleton(){
		
	}
	
	public Singleton getInstance(){
		
		if(singleton  == null){
			singleton = new Singleton();
		}
		
		return singleton;
		
		
	}
	
}
