package xml;

import java.io.FileInputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class PostSoapXml {

  public static void main(String[] args) {

    try {
	// Create the connection
	SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
	SOAPConnection conn = scf.createConnection();
			
	// Create message
	MessageFactory mf = MessageFactory.newInstance();
	SOAPMessage msg = mf.createMessage();
			
	// Object for message parts
	SOAPPart sp = msg.getSOAPPart();
	StreamSource prepMsg = new StreamSource( new FileInputStream("C:\\devs\\testcases\\DrillThroughAutoTest.xml"));
	sp.setContent(prepMsg);
			
	// Save message
	msg.saveChanges();
			
	// View input
	System.out.println("\n Soap request:\n");
	msg.writeTo(System.out);
	System.out.println();
	
	
	String authorization = new sun.misc.BASE64Encoder().encode(("apuser"+":"+"r3r4NZ").getBytes());
	MimeHeaders hd = msg.getMimeHeaders();
	hd.addHeader("Authorization", "Basic " + authorization);
	
	// Send
	String urlval = "http://localhost:8087/cube/webservices/Queries";
	SOAPMessage rp = conn.call(msg, urlval);
			
	// View the output
	System.out.println("\nXML response\n");
			
	// Create transformer
	TransformerFactory tff = TransformerFactory.newInstance();
	Transformer tf = tff.newTransformer();
			
	// Get reply content
	Source sc = rp.getSOAPPart().getContent();
			
	// Set output transformation
	StreamResult result = new StreamResult(System.out);
	tf.transform(sc, result);
	System.out.println();
			
	// Close connection
	conn.close();
			
    }
    catch (Exception e) {
	System.out.println(e.getMessage());
    }
  }
}
