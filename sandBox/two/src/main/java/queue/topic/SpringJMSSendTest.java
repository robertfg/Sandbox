package queue.topic;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class SpringJMSSendTest {
 
 private JmsTemplate jmsTemplate;

 public JmsTemplate getJmsTemplate() {
     return jmsTemplate;
 }

 public void setJmsTemplate(JmsTemplate MQjmsTemplate) {
     this.jmsTemplate = MQjmsTemplate;
 }

 public void send(final String txt){

     if(jmsTemplate==null){
        System.out.println("Template is null!!");
     }

     System.out.println("Sending Message:["+txt+"]");
     jmsTemplate.send( 
           new MessageCreator() {
                public Message createMessage(Session session) 
                throws JMSException {
                     return session.createTextMessage(txt);
         }
           }
     );
  
     System.out.println("Message Sent");
 }

 public static void main(String argc[]){
     try {
           System.out.println("trying to get spring context");
           ApplicationContext ctx = new ClassPathXmlApplicationContext("SPRING-INF/mq-topic.xml");   
             
             
           
           System.out.println("got spring context");

           SpringJMSSendTest sender = (SpringJMSSendTest)ctx.getBean("jmsSendTest");
            
            sender.send("Test Message 12345");
     } 
     catch (Exception ex) {
           System.out.println("Exception: "+ex.toString());
           ex.printStackTrace();
     }

 }
}