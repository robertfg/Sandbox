package com.quartetfs.pivot.anz.service.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import com.quartetfs.pivot.anz.service.export.GetDataFromIndexerTask.TaskObject;
import com.quartetfs.pivot.anz.utils.ANZConstants;

public class FileWriterTask implements Callable<Long> {

	private BlockingQueue<TaskObject> taskQueue;
	private OutputStream out;
    
    
	public FileWriterTask(BlockingQueue<TaskObject> taskQueue, OutputStream out) {
		this.taskQueue = taskQueue;
		this.out = out;
		
	}
	public FileWriterTask( OutputStream out) {
			this.out = out;
	}
	
	@Override
	public Long call() throws Exception {
		long totalTime = 0;
		TaskObject object;

		while ((object = taskQueue.take()) != null) 
		{
			if (object.isPoison()){
				System.out.println("poison.......");
				break;
			}
			totalTime+=call(object);			
		}
		return totalTime;

	}

	public Long call(TaskObject object) throws IOException {
		long start = System.currentTimeMillis();
		try{
		if(object!=null && object.getRows()!=null && object.getRows().size()>0) {
			StringBuilder sb = new StringBuilder(1000);
			for (Object[] row : object.getRows()) {
		       String stringKeys = "";
				for (Object colValue : row) {
					stringKeys+= getColValue(colValue) + ANZConstants.COMMA_SEPARATOR;
				}
		        	sb.append(stringKeys).deleteCharAt(sb.length()-1).append("\n");	
			}
			out.write(sb.toString().getBytes());
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return System.currentTimeMillis() - start;
	}

	private Object getColValue(Object colValue) {
		try{
		if (colValue instanceof double[]) {
			double[] colValArray = (double[]) colValue;
			StringBuilder sb = new StringBuilder(colValArray.length * 5);
			for (double val : colValArray) {
				sb.append(val).append("|");
			}
			return sb.toString();
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return colValue == null ? "" : colValue;
	}
}
