package com.apachebite;

import java.io.Serializable;

public class NotificationObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2846550236119432271L;
	private String ID;
	private String Type;
	private String Message;

	public String getID() {
		return ID;  
	}

	public void setID(String iD) {
		ID = iD;
	} 

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}

}