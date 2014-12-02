package asynch;

public interface AsyncCallback<T> {

	public void callback(T result) throws Exception;
	public void setTaskCount(int task);
}