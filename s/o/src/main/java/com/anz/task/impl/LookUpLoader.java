package com.anz.task.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.anz.task.Tasklet;
import com.anz.util.lookup.ILookUp;

public class LookUpLoader implements Tasklet {
      
	List<ILookUp> lookUps; 

	public List<ILookUp> getLookUps() {
		return lookUps;
	}

	public void setLookUps(List<ILookUp> lookUps) {
		this.lookUps = lookUps;
	}


	@Override
	public String taskName() {
		return "LookUp Loader";
	}

	@Override
	public void run() {
	 
		 try {
			 ExecutorService executor = Executors.newFixedThreadPool(4);
			    List<Future<Integer>> futures = new ArrayList<Future<Integer>>(lookUps.size());
			    for (ILookUp lookUp : lookUps) {
				   futures.add( executor.submit(lookUp));
				} 
			    for(Future<Integer> future: futures){
			    	future.get();
			        System.out.println("getsss");
			    } 
			    executor.shutdown();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}
  
	
}
