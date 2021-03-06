package com.zcage.jms;

import java.util.logging.Logger;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

/**
 * This is a simple example to show some basics of JMS for a publish - subscribe
 * scenario.
 */
public class ExamplePublishAndSubscribe {
    public static final String TOPIC1 = "Life-Connect";
    private static Logger jdkLogger = Logger
            .getLogger("com.zcage.jms.ExamplePublishAndSubscribe");

    /**
     * Create a JMS Publisher and Subscriber. Of course in the real world these
     * would be in separate applications. Start these processes and let them run
     * a while before shutting down. Execution comments will be logged.
     */
    public static void main(String[] args) throws Exception {
        startBroker(); // An embedded JMS Broker

        Publisher publisher = new Publisher();
       // publisher.start(); // Runs as a separate thread

        Thread.sleep(3000);
        Subscriber subscriber1 = new Subscriber();
        subscriber1.startListening();
        

        Subscriber subscriber2 = new Subscriber();
        subscriber2.startListening();
        
        Subscriber subscriber3 = new Subscriber();
        subscriber3.startListening();
        
        Subscriber subscriber4 = new Subscriber();
        subscriber4.startListening();
        
        // Let the system run for a bit then shut it down nicely
        Thread.sleep(30000);
        publisher.stopPublishing();
      //  subscriber1.stopListening();
       // subscriber2.stopListening();
       // subscriber3.stopListening();
        //subscriber4.stopListening();

        jdkLogger.info("Exiting");
      //  System.exit(0); // Force exit
    }

    /**
     * Create an Embedded JMS Broker for this example. Requires JDK1.5.
     */
    private static void startBroker() throws Exception {
        jdkLogger.info("Starting Broker");
        BrokerService broker = new BrokerService();
        broker.setUseJmx(true);
        broker.addConnector("tcp://localhost:61617");
        broker.start();
        jdkLogger.info("Broker started");
    }

    /**
     * Use the ActiveMQConnectionFactory to get a JMS ConnectionFactory. In an
     * enterprise application this would normally be accessed through JNDI.
     */
    public static ConnectionFactory getJmsConnectionFactory()
            throws JMSException {
        String user = ActiveMQConnection.DEFAULT_USER;
        String password = ActiveMQConnection.DEFAULT_PASSWORD;
        String url = ActiveMQConnection.DEFAULT_BROKER_URL;

        return new ActiveMQConnectionFactory(user, password, url);
    }
}
