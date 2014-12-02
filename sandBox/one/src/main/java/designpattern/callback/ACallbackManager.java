package designpattern.callback;

import java.util.ArrayList;
import java.util.List;


public  abstract class ACallbackManager implements ICallback<ISubject<ISubjectMessage>>{
	
	private ISubjectMessage subjectMatter;
	
	private List<ISubject<ISubjectMessage>> callBacks = new ArrayList<ISubject<ISubjectMessage>>();
	
	@Override
	public void setPredicate(ISubjectMessage subjectMatter) {
		this.subjectMatter = subjectMatter;
		notifyCallBack();
		  
	}
	
	@Override
	public void registerCallBack(ISubject<ISubjectMessage> o) {
		callBacks.add(o);
	}

	@Override
	public void removeCallBack(ISubject<ISubjectMessage> o) {
		callBacks.remove(o);
	}

	@Override
	public void notifyCallBack() {
		for (ISubject<ISubjectMessage> callback : callBacks) { 
			callback.recievedEvent(subjectMatter);
		}
		
	}


}
