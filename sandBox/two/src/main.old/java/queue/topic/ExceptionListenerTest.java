package queue.topic;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.springframework.jms.connection.CachingConnectionFactory;

// import com.sssw.jms.api.JMQConnectionLostException;

public class ExceptionListenerTest implements ExceptionListener {
	CachingConnectionFactory cachingConnectionFactory;

	public void onException(JMSException arg0) {
		System.err.println("Exception occurred " + arg0);
		cachingConnectionFactory.onException(arg0);
	}

	public CachingConnectionFactory getCachingConnectionFactory() {
		return cachingConnectionFactory;
	}

	public void setCachingConnectionFactory(
			CachingConnectionFactory cachingConnectionFactory) {
		this.cachingConnectionFactory = cachingConnectionFactory;
	}
}
