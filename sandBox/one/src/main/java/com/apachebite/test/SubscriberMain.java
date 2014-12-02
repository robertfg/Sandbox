package com.apachebite.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SubscriberMain {

	public static void main(String ar[]) {
		ApplicationContext appContext = new ClassPathXmlApplicationContext(
				new String[] { "subscriber.xml" });

		System.out.println("STARTED LISTENING…………………………….");
		appContext.getBean("jmsMessageListener");

	}
}