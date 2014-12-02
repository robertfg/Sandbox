package com.quartetfs.pivot.anz.service.export;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import com.quartetfs.pivot.anz.service.export.GetPnlDataFromIndexerTask.TaskPnlObject;
import com.quartetfs.pivot.anz.utils.ANZConstants;

public class FilePnlWriterTask implements Callable<Long> {

	private BlockingQueue<TaskPnlObject> taskQueue;
	private OutputStream out;
    
    
	public FilePnlWriterTask(BlockingQueue<TaskPnlObject> taskQueue, OutputStream out) {
		this.taskQueue = taskQueue;
		this.out = out;
		
	}

	@Override
	public Long call() throws Exception { 
		long totalTime = 0;
		TaskPnlObject object;

		while ((object = taskQueue.take()) != null) 
		{
			if (object.isPoison()){
				break;
			}
			totalTime+=call(object);			
			

		}
		return totalTime;

	}

	private Long call(TaskPnlObject object) throws IOException {
		long start = System.currentTimeMillis();
		
		if(object!=null && object.getRows()!=null && object.getRows().size()>0) {
			StringBuilder sb = new StringBuilder(1000);
			for (Object[] row : object.getRows()) {
		      String stringKeys = "";
		        
			 if(object.isPutHeader()){
				 for (Object colValue : row) {
						stringKeys+= getColValue(colValue) + ANZConstants.COMMA_SEPARATOR;
			       }
				 stringKeys = stringKeys.substring(0,stringKeys.length() -1);
			 } else {	
			     stringKeys = object.getBatchId();//+ ANZConstants.COMMA_SEPARATOR;
		       for (Object colValue : row) {
					stringKeys+= ANZConstants.COMMA_SEPARATOR + getColValue(colValue) ;
			   }
			 }
		    sb.append(stringKeys).append("\n");
		        
			}
			out.write(sb.toString().getBytes());
		}
		return System.currentTimeMillis() - start;
	}

	private Object getColValue(Object colValue) {
		if (colValue instanceof double[]) {
			double[] colValArray = (double[]) colValue;
			StringBuilder sb = new StringBuilder(colValArray.length * 5);
			for (double val : colValArray) {
				sb.append(val).append("|");
			}
			return sb.toString();
		} 
		
		return colValue == null ? "" : colValue;
	}
	
	public static void main(String[] args){
		
		StringBuilder x = new StringBuilder("a,b,c\n");
		x.append("d,e\n");
		x.append("d,e\n");
		x.append("d,e\n");
		x.append("d,e\n");  
		x.append("d,e\n");
		
		OutputStream outputStream;
		try {
			outputStream = new FileOutputStream("c:\\test.txt");
			
			outputStream.write(  x.toString().getBytes() );
			
			((FileOutputStream)outputStream).getChannel()
			.truncate( ((FileOutputStream)outputStream).getChannel().size() - 1);
			
			outputStream.flush();
			outputStream.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		 
		System.out.println( x.append("d,e,,").deleteCharAt( x.length() -1 ));
		 
		
	}
}
