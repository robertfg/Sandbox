package com.apachebite.test;

import javax.jms.JMSException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.apachebite.NotificationObject;
import com.apachebite.Publisher;

public class PublisherMain {

	public static void main(String ar[]) {
		ApplicationContext appContext = new ClassPathXmlApplicationContext(
				new String[] { "SPRING-INF/publisher.xml" });

		NotificationObject obj = new NotificationObject();

		obj.setID("1");

		obj.setType("Request");
		obj.setMessage("send passbook");
		Publisher publisher = (Publisher) appContext.getBean("publisher");
		try {
			publisher.sendObjectCreation(obj);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}