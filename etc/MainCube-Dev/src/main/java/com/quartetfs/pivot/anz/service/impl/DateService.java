package com.quartetfs.pivot.anz.service.impl;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.context.subcube.impl.ASubCubeDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.IAxisMember;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.pivot.anz.service.IDateService;
import com.quartetfs.pivot.anz.staticdata.IStaticData;
import com.quartetfs.pivot.anz.staticdata.impl.VaRDates;

/**
 * This service holds dates used for various post-processor and other part of code
 * @author Quartet Financial Systems.
 *
 */
public class DateService implements IDateService{
		
	final private ConcurrentMap<String, TreeSet<IDate>> allDatesContainer 
	= new ConcurrentHashMap< String, TreeSet<IDate>>();
	
	final private ConcurrentMap<String, ConcurrentHashMap<IDate, Set<IDate>>> varDatesContainer 
	= new ConcurrentHashMap< String, ConcurrentHashMap<IDate,Set<IDate>>>();
	
	

	private Set<IDate> getDatesFromDimension(IActivePivot pivot,int timeDimensionIdx){
		final Set<IDate> dates=new TreeSet<IDate>();
		IDimension dimension = pivot.getDimensions().get(timeDimensionIdx + 1);
		
		//pivot.getAvailableAttributes()
		//pivot.getAvailableOperations()
		//IActivePivotContext x = pivot.getContext();
		
		
		if (dimension instanceof ASubCubeDimension) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			List<IAxisMember> members = (List<IAxisMember>) ((ASubCubeDimension) dimension)
			.getBaseDimension().retrieveMembers(0);
			for (IAxisMember mem : members) {
				dates.add((IDate) mem.getDiscriminator());
			}
		}
		
		
		return dates;
	}


	/**
	 * This method  return previous day w.r.t a date , this is used by Change in M_RESULTV Postprocessor to find previous date
	 * as dates in COB Date dimension are in reverse order. 
	 * @param date
	 * @param pivot
	 * @param timeDimensionIdx
	 * @return
	 */
	public IDate getPreviousDay(IDate date,IActivePivot pivot,int timeDimensionIdx){
		Set<IDate>dates=getDatesFromDimension(pivot,timeDimensionIdx);
		if (!dates.contains(date)){
			return null;
		}else
		{
			IDate prev=null;
			for (IDate dt:dates){
				if (dt.equals(date)){
					return prev;
				}
				prev=dt;
			}
		}
		return null;
	}

	/**
	 * return all dates
	 * @param pivot
	 * @param timeDimensionIdx
	 * @return
	 */

	public Collection<IDate> getAll(IActivePivot pivot, int timeDimensionIdx) {
		return getDatesFromDimension(pivot,timeDimensionIdx);
	}


	/**
	 * Extract first N dates from dates starting from a date which is equal or before StartDate.
	 * Dates should be sorted in reverse order means earlier dates should come first followed by latest dates
	 * @param startDate
	 * @param count
	 * @param dates
	 * @return
	 */
	private List<IDate> extractFirstNDates(int count,Set<IDate> dates) {
		List<IDate> result=new ArrayList<IDate>( );
		if (dates==null){
			return result;
		}
		for(IDate date:dates){
			//if (result.size()<count){
				result.add(date);
			//}
		}
		Collections.sort(result );
		return result;
	}


	@Override
	public void onStaticDataCompleted(IStaticData staticData) {
		if (staticData instanceof VaRDates){
			VaRDates actualDates=(VaRDates)staticData;
			 String varDatesType = actualDates.type().name();
			 
			if( varDatesContainer.get( varDatesType ) == null ){
				varDatesContainer.put(   varDatesType, new  ConcurrentHashMap<IDate,Set<IDate>>() );
			}
			
			if( varDatesContainer.get( varDatesType).get( actualDates.date() )!=null  ){
				varDatesContainer.get( varDatesType ).remove(actualDates.date());
			 	varDatesContainer.get( varDatesType ).put(actualDates.date(),actualDates.getHistoryDates());
				mergeVarDates( varDatesType, actualDates );
				
			}else {
				varDatesContainer.get( varDatesType ).put( actualDates.date(),actualDates.getHistoryDates());
		        mergeVarDates( varDatesType, actualDates );
			}
			
			if( allDatesContainer.get(varDatesType) == null  ) {
				allDatesContainer.put( varDatesType , new TreeSet<IDate>());
			}
			
			allDatesContainer.get(varDatesType).addAll(actualDates.getHistoryDates());
		}
	}

	private void mergeVarDates(String varDatesType,VaRDates actualDates ){
		
		if( varDatesType.equals("VARDATESUAT")   ) {
		  	if(varDatesContainer.get( "VARDATES")==null){
		  		varDatesContainer.put("VARDATES", new  ConcurrentHashMap<IDate,Set<IDate>>() );
		  	}
		  	
			if( varDatesContainer.get( "VARDATES").get( actualDates.date())!= null ){
		  		varDatesContainer.get( "VARDATES").get( actualDates.date()).addAll(actualDates.getHistoryDates());
		  	} else {
		  		varDatesContainer.get( "VARDATES").put( actualDates.date(), actualDates.getHistoryDates());
		  	}
			
			if( allDatesContainer.get("VARDATES") == null  ) {
				allDatesContainer.put( "VARDATES" , new TreeSet<IDate>());
			}
			allDatesContainer.get("VARDATES").addAll(actualDates.getHistoryDates());
		}
		
		if( varDatesType.equals("VARSIXYEARDATES")  ) {
		  	
			if( allDatesContainer.get("VARDATES") == null  ) {
				allDatesContainer.put( "VARDATES" , new TreeSet<IDate>());
			}
			allDatesContainer.get("VARDATES").addAll(actualDates.getHistoryDates());
		}
		
		if( varDatesType.equals("VARSTRESSDATESUAT") ){ 
			if(varDatesContainer.get( "VARSTRESSDATES")==null){
		  		varDatesContainer.put("VARSTRESSDATES", new  ConcurrentHashMap<IDate,Set<IDate>>() );
		  	}
			
			if( varDatesContainer.get( "VARSTRESSDATES").get( actualDates.date())!= null ){
		  		varDatesContainer.get( "VARSTRESSDATES").get( actualDates.date()).addAll(actualDates.getHistoryDates());
		  	}else {
		  		varDatesContainer.get( "VARSTRESSDATES").put( actualDates.date(), actualDates.getHistoryDates());
		  	}
			
			if( allDatesContainer.get("VARSTRESSDATES") == null  ) {
				allDatesContainer.put( "VARSTRESSDATES" , new TreeSet<IDate>());
			}
			
			allDatesContainer.get("VARSTRESSDATES").addAll(actualDates.getHistoryDates());
		}
		
		
		if( varDatesType.equals("VARDATES") ){
			
			if(varDatesContainer.get(  "VAR")==null || varDatesContainer.get("VAR").isEmpty()  ){
				varDatesContainer.put(  "VAR", new  ConcurrentHashMap<IDate,Set<IDate>>() );
			}
			varDatesContainer.get( "VAR").put( actualDates.date(), cloneDate(actualDates.getHistoryDates()) );
			
			
			if( varDatesContainer.get( "VARDATESUAT")!=null && varDatesContainer.get( "VARDATESUAT").get( actualDates.date())!= null ){
		  		varDatesContainer.get( "VARDATES").get( actualDates.date()).addAll(  varDatesContainer.get( "VARDATESUAT").get( actualDates.date()) );
		  	}
		}
		
		if( varDatesType.equals("VARSTRESSDATES") ) { 
			
			if(varDatesContainer.get(  "VAR_STRESS")==null || varDatesContainer.get("VAR_STRESS").isEmpty()  ){
				varDatesContainer.put(  "VAR_STRESS", new  ConcurrentHashMap<IDate,Set<IDate>>() );
			}
			varDatesContainer.get( "VAR_STRESS").put( actualDates.date(), cloneDate( actualDates.getHistoryDates() ));
			
			if(varDatesContainer.get( "VARSTRESSDATESUAT")!=null&& varDatesContainer.get( "VARSTRESSDATESUAT").get( actualDates.date())!= null ){
				varDatesContainer.get( "VARSTRESSDATES").get( actualDates.date()).addAll(  varDatesContainer.get("VARSTRESSDATESUAT").get( actualDates.date()) );
		  	}
		}
		
	}

	private Set<IDate> cloneDate(Set<IDate> actualDates){
		
		Set<IDate> vDate = new HashSet<IDate>();
		
		for (Iterator iterator = actualDates.iterator(); iterator.hasNext();) {
			IDate iDate = (IDate) iterator.next();
			vDate.add(iDate);
		}
		return vDate;
	    	
		
	}
	
	@Override
	public boolean isVaRDatesLoadedFor(IDate date,String type) {
		
		if(date!=null){
			if(varDatesContainer!=null && varDatesContainer.get(type)!=null){
				return varDatesContainer.get(type).keySet().contains(date);
			}
		}
		return false;
		
	}


	@Override
	public IDate retrieveDateByIndex(IDate cobDate, int index,String type) {
		IDate dateToRetrieve = null;
		Set<IDate> dates = null;
		

		
		
		dates = varDatesContainer.get(type).get(cobDate);
		List<IDate> result=new ArrayList<IDate>( );
		if (dates==null){
		
		}
		for(IDate date:dates){
				result.add(date);
		}
		Collections.sort(result );
		int i = 0;
		for (IDate iDate : result) {
			if (i == index){
				dateToRetrieve = iDate;
				break;
			}
			i++;
		}
		return dateToRetrieve;
	}


	@Override  
	public String[] retrieveDates(IDate cobDate,String type) {
		Set<IDate> dates = null;
		
		dates = varDatesContainer.get(type).get(cobDate);
		if (dates == null || dates.isEmpty()) return null;
		
		Iterator<IDate> it = dates.iterator();
		String[] datesArray = new String[dates.size()];
		for (int i=0;i<datesArray.length;i++){
			datesArray[i]=it.next().toString();
		}
		return datesArray;
		
	}

	private String[] reverseArray(String[] arr){
		String[] ret = new String[arr.length];
		int idx = 0;
		
		for (int i = arr.length -1; i >= 0 ; i--) {
			ret[idx] = arr[i];
			idx++;
		}
		return ret;
	}
	
	@Override
	public Collection<IDate> getHistoryDates(IDate startDate, int count,
			String type) {
		
		startDate=startDate.clone();
		startDate.applyTime(0, 0, 0, 0);
		List<IDate> result=extractFirstNDates(count, varDatesContainer.get(type).get(startDate));
		return result!=null?result:new HashSet<IDate>();
	}

	@Override
	public Collection<IDate> getHistoryDates(String type) {
			return allDatesContainer.get(type);
			
	}
	
	public static void main(String[] args){
		
		
	}
}
