package com.apachebite;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.stereotype.Component;


public class Subscriber implements MessageListener {

	// private static final Logger logger =
	// Logger.getLogger(JmsMessageListener.class);

	/**
	 * Implementation of <code>MessageListener</code>.
	 */
	public void onMessage(Message message) {
		try {

			if (message instanceof ObjectMessage) {
				
				ObjectMessage objMessage = (ObjectMessage) message;
				
				System.out.println("————————————————————");
				System.out.println("Message Notification received ");
				NotificationObject receivedObject = (NotificationObject) objMessage.getObject();

				System.out.println("Notification ID: " + receivedObject.getID());
				System.out.println("Notification Type: " + receivedObject.getType());
				System.out.println("Notification Message: "	+ receivedObject.getMessage());
				System.out.println("————————————————————");
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}