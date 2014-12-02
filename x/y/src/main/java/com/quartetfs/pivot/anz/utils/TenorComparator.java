package com.quartetfs.pivot.anz.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TenorComparator implements Comparator<String> 
{

	private static final int DAY=4;
	private static final int DAYOFFSET=3;
	private static final int WEEKOFFSET=2;
	private static final int MONTHOFFSET=1;
	private static final int YEAROFFSET=0;
	private static final String NET="NET";
	private final Pattern pattern=Pattern.compile("(\\d*Y)?(\\d*M)?(\\d*W)?(\\d*D)?");
	private final Pattern wordPattern=Pattern.compile(".*(JAN-\\d{2}|FEB-\\d{2}|MAR-\\d{2}|APR-\\d{2}|MAY-\\d{2}|JUN-\\d{2}|JUL-\\d{2}|AUG-\\d{2}|SEP-\\d{2}|OCT-\\d{2}|NOV-\\d{2}|DEC-\\d{2}).*");
	//private final Pattern wordPattern=Pattern.compile(".*([a-zA-Z]{3}-\\d{2}).*"); //matches first 3 character with dash and 2 digit number
	private static final Map<String,String> monthMap = new HashMap<String,String>();
	static{
/*		monthMap.put("JAN", 400);
		monthMap.put("FEB", 450);
		monthMap.put("MAR", 500);
		monthMap.put("APR", 550);
		monthMap.put("MAY", 600);
		monthMap.put("JUN", 650);
		monthMap.put("JUL", 700);
		monthMap.put("AUG", 750);
		monthMap.put("SEP", 800);
		monthMap.put("OCT", 850);
		monthMap.put("NOV", 900);
		monthMap.put("DEC", 950);
*/
		monthMap.put("JAN", "01");
		monthMap.put("FEB", "02");
		monthMap.put("MAR", "03");
		monthMap.put("APR", "04");
		monthMap.put("MAY", "05");
		monthMap.put("JUN", "06");
		monthMap.put("JUL", "07");
		monthMap.put("AUG", "08");
		monthMap.put("SEP", "09");
		monthMap.put("OCT", "10");
		monthMap.put("NOV", "11");
		monthMap.put("DEC", "12");

	
	}
	
		
	private double extractNumber(String input){
		if (input==null)
			return 0;
		double retValue=0; 
		try
		{
			retValue=Integer.parseInt(input.substring(0,input.length()-1));
		}catch(NumberFormatException ne){}
		
		return retValue;
	}
	
	public int compare(String o1, String o2) 
	{  
		double[] offset2=getOffsets(o2.toUpperCase());
		double[] offset1=getOffsets(o1.toUpperCase());		
		double ret = compareTo(offset1, offset2);
		
		if(ret<0){
			return -1;
		} else if(ret==0){
			return 0;
		} else if(ret>0){
			return 1;
		}
		return 0;
		
	}
	private double[] getOffsets(String input){//
		double offsets[]=new double[4];
		 input = input.replace(NET, "");
		
		 Matcher matcher=pattern.matcher(input.trim());
		 if (matcher.matches()){
			 offsets[YEAROFFSET]=extractNumber(matcher.group(1));
			 offsets[MONTHOFFSET]=extractNumber(matcher.group(2));
			 offsets[WEEKOFFSET]=extractNumber(matcher.group(3));
			 offsets[DAYOFFSET]=extractNumber(matcher.group(4));
			 //offsets[DAY]=0;
			 
			 
		 }
		 else
		 {
		// 106327
			 
			 Matcher monthWordMatcher=wordPattern.matcher(input.trim());
			 if(monthWordMatcher.matches()){
				// offsets[YEAROFFSET]=0;
				 
				 offsets[YEAROFFSET]=     Integer.valueOf( "20" + Integer.valueOf(input.substring( input.indexOf("-") + 1, input.length())) +  monthMap.get( input.substring(0,3)) )  ;
				 offsets[MONTHOFFSET]= 0; //monthMap.get( input.substring(0,3));
				 offsets[WEEKOFFSET] = 0;
				 //offsets[WEEKOFFSET] = monthMap.get( input.substring(0,3)) + (Integer.valueOf(input.substring( input.indexOf("-") + 1, input.length())) * 1);
				 offsets[DAYOFFSET]  = 0;  
				
					 
			 } else {	     
			 // it means some thing new which doesnt  identified by us, we are giving high value to offsets so that these values always appear at end
			 offsets[YEAROFFSET] = 9999+Math.abs(input.hashCode());
			 offsets[MONTHOFFSET]= 9999;
			 offsets[WEEKOFFSET] = 9999;
			 offsets[DAYOFFSET]  = 9999;
			// offsets[DAY]=999;
			 
			 }
		 }
		 return offsets;
	}  
	
	
	private double compareTo(double offset1[],double offset2[] ) {
		double yearDiff=offset1[YEAROFFSET]-offset2[YEAROFFSET];
		
		if (yearDiff!=0){
			return yearDiff;
		}
		
		double monthDiff=offset1[MONTHOFFSET]-offset2[MONTHOFFSET];
		if(monthDiff!=0){
			return monthDiff;
		}
		
		
		if(offset1[WEEKOFFSET]!=0) {
			offset1[DAYOFFSET] = (offset1[WEEKOFFSET]*7);	
		}
		
		if(offset2[WEEKOFFSET]!=0) {  
			offset2[DAYOFFSET] = (offset2[WEEKOFFSET]*7);
		}
		
		
		double dayDiff=offset1[DAYOFFSET]-offset2[DAYOFFSET];
		return dayDiff;	
		
	}
	
}

