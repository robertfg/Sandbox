/*
 * (C) Quartet FS 2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.webservices;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * 
 * java.net authenticator that can login to the default
 * ActivePivot Sandbox application with the "admin/admin" account
 * when HTTP Basic or Digest authentications are in place.
 * 
 * @author Quartet FS
 *
 */
public class HTTPAuthenticator extends Authenticator {

	/** Default username */
	static final String USERNAME = "admin";
	
	/** Default password */
	static final String PASSWORD = "admin";
	
	/** Install the basic authenticator */
	public static final void install() {
		Authenticator.setDefault(new HTTPAuthenticator());
	}
	
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(USERNAME, PASSWORD.toCharArray());
    }

}
