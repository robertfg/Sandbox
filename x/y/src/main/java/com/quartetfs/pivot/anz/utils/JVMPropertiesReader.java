package com.quartetfs.pivot.anz.utils;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.springframework.core.io.Resource;

public class JVMPropertiesReader {

	private Collection<Resource> resources;

	public void setResource(final Resource resource) {
		resources = Collections.singleton(resource);
	}
	
	public void applyProperties() throws Exception{
        final Properties systemProperties = System.getProperties();
        for(final Resource resource : resources){
            final InputStream inputStream = resource.getInputStream();
            try{systemProperties.load(inputStream);} 
            finally{closeQuietly(inputStream);}
        }
    }

	private void closeQuietly(InputStream inputStream)
	{
		try{inputStream.close();}
		catch(Exception e){
		}
		
	}
	
}
