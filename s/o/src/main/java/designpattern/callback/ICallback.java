package designpattern.callback;

public interface ICallback<T> {
	
		public void registerCallBack(T o);
		public void removeCallBack(T o);
		public void notifyCallBack();
		public void setPredicate(ISubjectMessage subjectMatter);
		
		
}
