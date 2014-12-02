package designpattern.callback;

public interface ISubject<T> {
	
	void recievedEvent(T o);
	
}
