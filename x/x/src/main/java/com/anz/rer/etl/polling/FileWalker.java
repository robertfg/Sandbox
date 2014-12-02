package com.anz.rer.etl.polling;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class FileWalker extends ADirectoryPolling {
	 public FileWalker(int interval, long startDelay, String path, String filter) {
		super(interval, startDelay, path, filter);
		// TODO Auto-generated constructor stub
	}

	public FileWalker(int interval, long startDelay) {
		super(interval, startDelay);
	}

	public void walk( String path ) {

	        File root = new File( path );
	        File[] list = root.listFiles();

	        for ( File f : list ) {
	            if ( f.isDirectory() ) {
	                walk( f.getAbsolutePath() );
	                System.out.println( "Dir:" + f.getAbsoluteFile() );
	            }
	            else {
	                System.out.println( "File:" + f.getAbsoluteFile() );
	            }
	        }
	    }
  
	    public static void main(String[] args) {
	    	String filter = ".*.*.ERR|.*.*.DONE"; 
	    	FileWalker fw = new FileWalker(1000,1000, "C:\\devs\\polling", filter );
	    	
	    	//fw.run();
	      //  fw.walk("C:\\devs\\polling" );
	    	
	    	TimerTask task = (TimerTask) fw;
			Timer timer = new Timer();
			timer.schedule(task, 1000,10000);
	   
	    }

		@Override
		public void onChange(File file, String action) {
			System.out.println("..xxxx:" +file.getAbsolutePath());	
		}

		@Override
		public void setPath(String path) {
			// TODO Auto-generated method stub
			
		}
}
