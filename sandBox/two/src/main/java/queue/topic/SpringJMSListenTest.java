package queue.topic;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;


public class SpringJMSListenTest {

  public SpringJMSListenTest() {
  }

  public static void main(String[] args) {
      try {
            System.out.println("trying to get spring context");
            ApplicationContext ctx =  
            		 new ClassPathXmlApplicationContext("SPRING-INF/mq-topic.xml");   

            System.out.println("got spring context");

            SpringJMSListenTest springJMSTest =   
                (SpringJMSListenTest)ctx.getBean("springJMSListenTest");
         } 
         catch (Exception ex) {
            ex.printStackTrace();
         }
  }
}
