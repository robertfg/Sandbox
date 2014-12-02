package com.apachebite;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class Publisher {

	private JmsTemplate template = null;

	public void setTemplate(JmsTemplate template) {
		this.template = template;
	}

	/**
	 * Generates JMS messages
	 */
	public void sendObjectCreation(NotificationObject objectCreation)
			throws JMSException {
		final NotificationObject createObject = objectCreation;
		template.setDeliveryMode(DeliveryMode.PERSISTENT);
		
		
		
		template.send(new MessageCreator() {

			public Message createMessage(Session session) throws JMSException {
				ObjectMessage createMessage = session
						.createObjectMessage(createObject);

				System.out.println("Sending  Message Notification");
				System.out.println(createMessage);

				return createMessage;
			}
		});

	}
}