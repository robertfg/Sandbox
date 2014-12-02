package queue.topic;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class MessageListenerTest implements MessageListener {

  public void onMessage(Message message) {

    if (message instanceof TextMessage) {

      try {
         System.out.println("Received Message:["+
                      ((TextMessage) message).getText()+"]");
      } 
      catch (Exception ex) {    
         System.out.println("Exception in onMessage " + ex.toString() + "\n" +
                     ex.getStackTrace());
      }
    } 
    else {
      System.out.println("Message must be of type TextMessage");
    }
  }
}