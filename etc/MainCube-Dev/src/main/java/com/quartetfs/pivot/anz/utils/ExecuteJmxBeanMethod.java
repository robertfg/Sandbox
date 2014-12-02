package com.quartetfs.pivot.anz.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.quartetfs.pivot.anz.impl.MessagesANZ;

public class ExecuteJmxBeanMethod {
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private String userName;
	private String password;
	private String hostName;
	private String port;
    private String jmxBeanName;
    private String jmxMethodName;
    
	
	public ExecuteJmxBeanMethod(){}
	
	public ExecuteJmxBeanMethod(String userName, String password, String hostAddr, String port) {
		super();
		this.userName = userName;
		this.password = password;
		this.hostName = hostAddr;
		this.port = port;
	}

	@SuppressWarnings("unused")
	public boolean invoke(String jmxBeanName, String jmxMethodName, Object[] params, String[] signature) throws IllegalStateException { 
			boolean retValue = false;
	    
			JMXServiceURL rmiurl;
			try {
				rmiurl = new JMXServiceURL("service:jmx:rmi://" + this.hostName + ":" + this.port + "/jndi/rmi://" + this.hostName + ":" + this.port + "/jmxrmi");
			 

			Map<String, String[]> env = null;
			
			if(this.userName!=null && this.userName.length()>0){
				String[] creds = new String[] { this.userName,this.password };
				env = new HashMap<String, String[]>(1);
				env.put(JMXConnector.CREDENTIALS, creds);
			}
			
			JMXConnector jmxConnector = JMXConnectorFactory.connect(rmiurl, env);
			MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

			ObjectName objName = new ObjectName(jmxBeanName); //MalformedObjectNameException

			@SuppressWarnings("rawtypes")
			Set beans = mbsc.queryMBeans(objName, null);

			ObjectInstance instance = (ObjectInstance) beans.iterator().next();
			MBeanOperationInfo[] operationInfo = mbsc.getMBeanInfo(instance.getObjectName()).getOperations();
			
				if( checkIfCommandIsValidInBean(operationInfo, jmxMethodName) ) {
					Object result = mbsc.invoke(instance.getObjectName(), jmxMethodName, params, signature);
					retValue = true;
						if (jmxConnector != null) {
							jmxConnector.close();
						}
				} else {
					LOGGER.log(Level.SEVERE,MessagesANZ.JMX_BEAN_METHOD_NOT_FOUND, new Object[]{this.jmxMethodName});
					throw new IllegalStateException( MessagesANZ.JMX_BEAN_METHOD_NOT_FOUND  + ":" + jmxMethodName);
				}	
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE,MessagesANZ.JMX_HOST_CONNECTION_PROBLEM, new Object[]{this.hostName,this.port});
				throw new IllegalStateException( MessagesANZ.JMX_HOST_CONNECTION_PROBLEM,e.getCause());
				
			} catch (MalformedObjectNameException e) {
				LOGGER.log(Level.SEVERE,MessagesANZ.JMX_BEAN_NOT_FOUND, new Object[]{this.jmxBeanName});
				throw new IllegalStateException( MessagesANZ.JMX_BEAN_NOT_FOUND , e.getCause());
			} catch(SecurityException e){
				LOGGER.log(Level.SEVERE,MessagesANZ.JMX_AUTHENTICATION_FAILED);
				throw new IllegalStateException( MessagesANZ.JMX_AUTHENTICATION_FAILED ,e.getCause());
			} catch (Exception e){
				throw new IllegalStateException(e.getCause());
			}
	      return retValue;
	}
	
	private boolean checkIfCommandIsValidInBean(MBeanOperationInfo[] methodsInfo , String methodName) {
		
		for (int i = 0; i < methodsInfo.length; i++) {
			if (methodsInfo[i].getName().equals(methodName)) {
				return true;
			}
		}
		
		return false;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setJmxBeanName(String jmxBeanName) {
		this.jmxBeanName = jmxBeanName;
	}

	public String getJmxBeanName() {
		return jmxBeanName;
	}

	public void setJmxMethodName(String jmxMethodName) {
		this.jmxMethodName = jmxMethodName;
	}

	public String getJmxMethodName() {
		return jmxMethodName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
}
