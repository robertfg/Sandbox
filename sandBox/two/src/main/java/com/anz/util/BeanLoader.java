package com.anz.util;

public class BeanLoader {

	 private static BeanLoader beanLoader;
	 private BeanLoader() {}
	 
	 private static synchronized BeanLoader getObject() {
		 
		 if(beanLoader==null){
			 beanLoader = new BeanLoader();
			}
			return beanLoader;
	 }
	 
	 public Object getBean(String beanId){
		 
		 return new Object();
	 }
	 
	 
}
