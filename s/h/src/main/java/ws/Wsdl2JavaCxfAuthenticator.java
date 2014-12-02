package ws;

import java.net.PasswordAuthentication;

public class Wsdl2JavaCxfAuthenticator {

	
	 public Wsdl2JavaCxfAuthenticator() {
	        java.net.Authenticator.setDefault(new java.net.Authenticator() {
	            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication("apuser", "r3r4NZ".toCharArray());
	            }
	        });
	    }
	 
	 
}
