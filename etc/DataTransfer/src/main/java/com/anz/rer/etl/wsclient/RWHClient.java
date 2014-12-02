package com.anz.rer.etl.wsclient;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */



import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;

import com.anz.rer.etl.webservice.RWHService;
import com.anz.rer.etl.webservice.RwhInfo;


public final class RWHClient {

    public RWHClient() {
    }

    public static void main(String args[]) throws Exception {
    	
    	StringBuilder positionID = new StringBuilder();
   for (int i = 0; i < 10; i++) {
	   positionID.append( i + ":123456").append(" or ");
   } 	
   
   

   System.out.println(positionID.substring(0,positionID.length()- 4) + "x");
   String filter[] = positionID.toString().split("or");
   
   for (int i = 0; i < filter.length; i++) {
	   System.out.println( "x" + filter[i].trim() + "x"  );
   }
   
   System.exit(1);
  
   
    	
    System.out.println(URLDecoder.decode("%7ECRDT+TRD+CDS%7C%7ECRDT+TRD+CDS%7C%7ECRDT+TRD+CDS%7C%7ECRDT+TRD+CDS%7C%7ECRDT+TRD+CDS%7C%7ECRDT+TRD+CDS%7C%7ECRDT+TRD+CDS%7C%7ECRDT+TRD+CDS%7C%7ECRDT+TRD+CDS%7CCRDT+TRD+CDS%7CCorp+Debt+-+Aust%7CDebt%2FCredit+AU%7CLegacy%7CST+-+Aust%7CCredit%7CCredit+%26+DCM%7CTraded%7CCombined+Trading%7CTradedCAPM%7CANZ+Group", "UTF-8"));
    
    
	String wsUrl = "http://10.52.16.1:10811/ANZEtlApp-2.0.0-SNAPSHOT/RWHService";
	//String wsUrl = "http://10.68.68.9:8080/ANZEtlApp-2.1.0-SNAPSHOT/RWHService";
	
		 
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        
		factory.setServiceClass( RWHService.class );
		 
//		PhaseInterceptorChain.getCurrentMessage().put(Message.ENCODING, "UTF-8");
		
		factory.getInInterceptors().add(new LoggingInInterceptor( ));
		factory.getOutInterceptors().add(new LoggingOutInterceptor( ));
		
		
		factory.setAddress(wsUrl);
	  
		RWHService rwh = (RWHService) factory.create();
		
	List<RwhInfo> rx =	 rwh.getRiskWareHouseCache(20121213);
		for (RwhInfo rwhInfo : rx) {
			System.out.println("=================================================================================");
			System.out.println(rwhInfo.getPositionID()); 
			System.out.println(rwhInfo.getPortfolioHierarchyPath()); 
			System.out.println(rwhInfo.getPortfolio());
			System.out.println(rwhInfo.getMxFamily());
			System.out.println(rwhInfo.getMxGroup());
			System.out.println(rwhInfo.getMxType());
			System.out.println("Instrument" + rwhInfo.getInstrument());
			
			System.out.println("=================================================================================");
			  
			 
			 
		}

		//		Return r = 	rwh.getRWHCache(20121210);
		//System.out.println(r);
		//System.out.println("asdfasdfasdfasdfa");
		//System.out.println(rwh.getRWInfoByPositionID(new Integer(20121205), 1).getPortfolioHierarchyPath() );
	
		// RwhInfo r = rwh.getRWInfoByPositionID(20121205, 1089);
	//	 System.out.println( r.getGeographyHierarchyPath()  );
	 
    }
    
    public static void methodA() throws UnsupportedEncodingException{

		/*Return cached = rwh.getRWHCache(new Integer(20121121));
		List<RwhInfo> x =   rwh.getRiskWareHouseCache(new Integer(20121121));
		 System.out.println( x.size() );
	*/
    }
    
   
}

