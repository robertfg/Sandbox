package com.anz.util;

import java.util.List;

import com.anz.task.Tasklet;

public class TaskExecuter {

	private List<Tasklet> tasks;
	
	public List<Tasklet> getTasks() {
		return tasks;
	}

	public void setTasks(List<Tasklet> tasks) {
		this.tasks = tasks;
	}

	public void run(){
		
		
		for (Tasklet task : tasks) {
			try {
			 	new Thread(task).start();
			} catch (Exception e) {
			
				e.printStackTrace();
			}
			
		}
		
//		  ExecutorService executor = Executors.newFixedThreadPool(4);
//		    List<Future<Integer>> futures = new ArrayList<Future<Integer>>(lookUps.size());
//		    
//		    for (Tasklet task : tasks) {
//			   futures.add( executor.submit(lookUp));
//			} 
//		    
//		    for(Future<Integer> future: futures){
//		    	future.get();
//		        System.out.println("getsss");
//		    } 
		
		
	}
	
}
