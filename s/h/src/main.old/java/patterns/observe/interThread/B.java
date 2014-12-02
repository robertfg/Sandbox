package patterns.observe.interThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

class B extends Observable {

	private List<Observer> observers = new ArrayList<Observer>();
	
	public B(){ }
    public B(Observer o){ 
    
    	this.addObserver(o);
    }
    
    
    

    public void done(){

          this.setChanged(); // protected method
          this.notifyObservers( " Im done");

    }

   
    private void notifyAllObservers(Object obj){
    	/*for (Observer observer : observers) {
			observer.
		}*/
    }

	public List<Observer> getObservers() {
		return observers;
	}



	public void setObservers(List<Observer> observers) {
		this.observers = observers;
	}


}