package com.quartetfs.pivot.anz.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CubeEventKeeper {
	private ConcurrentMap<Date, List<CubeEvent>> events=new ConcurrentHashMap<Date, List<CubeEvent>>(); 
	private ConcurrentMap<String, CubeEvent> sourceToEvent=new ConcurrentHashMap<String, CubeEvent>();
	private static final Logger LOGGER = Logger.getLogger(CubeEventKeeper.class.getName());
	
	
	private Calendar calendar;
	
	public void fileParsingStarted(String fileName){
		addToEvents(new CubeEvent(isNull(fileName), EventType.Parse,calendar));
	}
	
	public void fileComittStarted(String fileName){
		addToEvents(new CubeEvent(isNull(fileName), EventType.Commit,calendar));
	}
	
	public void dataDeleteStarted(String fileName){
		addToEvents(new CubeEvent(isNull(fileName), EventType.Delete,calendar));
	}
	
	public boolean fileComittCompleted(String fileName){
		return completeEvent(EventType.Commit.prefixSource(isNull(fileName)));
	}
	
	public boolean fileParsingCompleted(String fileName){
		return completeEvent(EventType.Parse.prefixSource(isNull(fileName)));
	}
	
	public boolean dataDeleteCompleted(String fileName){
		return completeEvent(EventType.Delete.prefixSource(isNull(fileName)));
	}
	
	
	public void setCalendar(Calendar cal){
		this.calendar=cal;
	}
	
	public Calendar getCalendar() {
		return calendar;
	}
	
	
	private boolean completeEvent(String src){
		CubeEvent event= sourceToEvent.remove(src);
		if (event!=null){
			event.complete();
			return true;
		}
		return false;
	}
	/**
	 * Using optimistic locking nature of concurrent hash map to handle multithread issue.
	 * @param event
	 */
	private void addToEvents(CubeEvent event) {
		try {
			List<CubeEvent> existingEvents = events.get(event.getKey());
			if (existingEvents == null) {
				existingEvents = new ArrayList<CubeEvent>();
				List<CubeEvent> old=events.putIfAbsent(event.getKey(), existingEvents);
				if (old !=null){
					existingEvents=old;
				}
			}
			existingEvents.add(event);
			CubeEvent oldEvent=sourceToEvent.putIfAbsent(event.getSourceDetail(), event);
			if (oldEvent!=null){
				throw new IllegalStateException(" an event with this source name already started");
			}
		} catch (ParseException pe) {

		}
	}
	
	
	public List<CubeEvent> getEventsByDate(Date date) {
		return events.get(date);
	}
	

	private String isNull(String str){
		 if(str==null){
			 throw new IllegalStateException("Event source cannot be Null");
		 }
		 return str;
	}
	
	
	private String print(CubeEventCriteria criteria){
		CubeEventFormatter formatter=new CubeEventFormatter(criteria);
		for (Entry<Date, List<CubeEvent>> entry:events.entrySet()){
			formatter.format(entry.getKey(), entry.getValue());
		}
		
		
		String returnValue=formatter.complete();
		LOGGER.log(Level.INFO,returnValue);
		return returnValue;
	}
	
	public String printAll(){
		return print(new CubeEventCriteria());
	}

	public String printParseEvents(){
		return print(new CubeEventCriteria(EventType.Parse));
	}
	
	public String printCommitEvents(){
		return print(new CubeEventCriteria(EventType.Commit));
		
	}
	public String printDeleteEvents(){
		return print(new CubeEventCriteria(EventType.Delete));
	}

	static class CubeEventCriteria{
		private Set<EventType> criteria=new HashSet<EventType>();
		
		public CubeEventCriteria(EventType... types) {
			for (EventType type : types) {
				criteria.add(type);
			}
		}
		public CubeEventCriteria(){
			
		}
		
		public boolean apply(CubeEvent event){
			return (criteria.size()==0?true:criteria.contains(event.getType()));
		}
	}

	public void clean() {
		events.clear();
		sourceToEvent.clear();
	}
	
}
