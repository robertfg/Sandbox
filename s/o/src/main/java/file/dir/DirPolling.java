package file.dir;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import locks.LockingUtil;


public class DirPolling extends ADirectoryWatcher {

	private LockingUtil lockingUtil = new LockingUtil(null);
	private  boolean cacheStarted = false;
	
	private  AtomicBoolean atmCacheStarted = new AtomicBoolean(false);
	
	public DirPolling(String path) {
		super(path);
	}

	public DirPolling(String path, String filter) { 
		super(path, filter);
	}
	
	
	

	
	@Override
	public void onChange(File file, String action) {
		if(!cacheStarted){
			setStartCache();
			System.out.println(file + " started cache");	
		} else {
			System.out.println(file + " cached already started");
		}
		
		//lockingUtil.cacheData();
	}
	
	private synchronized void setStartCache(){
		cacheStarted = true;
	}
	
	
	public static void main(String[] args){
		DirPolling b =new DirPolling("C:\\devs\\projects\\", ".*.apx"); //".*2010.*\\.txt"
		b.run();
		TimerTask task =  (TimerTask) b; //.*ter\..*
		Timer timer = new Timer();
		timer.schedule(task, new Date(), 10000); 
			
		
	}
	
}
