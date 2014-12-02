package com.anz.rer.etl.cache;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LookUpRefresher {

    private LookUp lookUp;
	
	public LookUpRefresher(){
		
	}
	
	
	@Scheduled(cron = "* * 3 * * *")
	public void refreshLookUp(){
		//lookUp.
		 
	}

}
