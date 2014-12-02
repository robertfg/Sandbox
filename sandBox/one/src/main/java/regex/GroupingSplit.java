package regex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupingSplit {

	 //http://ocpsoft.org/opensource/guide-to-regular-expressions-in-java-part-1/
	 //http://ocpsoft.org/opensource/guide-to-regular-expressions-in-java-part-2/#section-9
		
	
	/*
	 * 
	 *  The \[ and \] escape the special bracket characters to match their literals.
	    The \w means "any word character", usually considered same as alphanumeric or underscore.
	    The + means one or more of the preceding item.
	    The " are literal characters.
		NOTE: If you want to ensure the whole string matches (not just part of it), prefix with ^ and suffix with $.
	 * 
	 * */
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
			System.out.println( "30-06-2016".replace('-', '/') ); 
					
		
		Calendar start = Calendar.getInstance();Calendar end = Calendar.getInstance();start.set(2012, 11, 12);try {Thread.sleep(((end.getTime().getTime() - start.getTime().getTime()) / (1000 * 60 * 60 * 24)) >1?((end.getTime().getTime() - start.getTime().getTime()) / (1000 * 60 * 60 * 24)% 2) == 0?500:((end.getTime().getTime() - start.getTime().getTime()) / (1000 * 60 * 60 * 24) * 10)>=1100?800:600:1);} catch (Exception e){}
		
		int x = 99;
	   
		System.out.println( x>=100?500:3) ;
		System.out.println(3%2);
	   
		int w = ((end.getTime().getTime() - start.getTime().getTime()) / (1000 * 60 * 60 * 24)) >1?
				((end.getTime().getTime() - start.getTime().getTime()) / (1000 * 60 * 60 * 24)% 2) == 0?500
				:((end.getTime().getTime() - start.getTime().getTime()) / (1000 * 60 * 60 * 24) * 10)>=1100?800:600:1;
		
					//
		
	//	System.out.println(((end.getTime().getTime() - start.getTime().getTime()) / (1000 * 60 * 60 * 24) * 10)>=1000?800:600:1 )  ;
				System.out.println(  w );
		
		try {
			Thread.sleep(500);
			System.out.println("xxxxxxxxxx");
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		
		GroupingSplit.touch("C:\\devs\\workspace\\SandBox\\target\\classes\\org\\apache\\log4j\\Category.class",   2010, 2, 30,23,14,50);
	GroupingSplit.touch("C:\\log4j-1.2.16.jar", 2012, 8, 4, 15,46,02);
		
		GroupingSplit.extractByDoubleQuoute();
		
 		GroupingSplit.extractBySquareBracket();
 		
        GroupingSplit.sample("This is a string that \"will be\" highlighted when your 'regular expression' matches","Get only word enclose with double-quote and single quote");
        
        GroupingSplit.extactDoubleQuoteAndSingleQuote();
	}
    
	public static void touch(String fileName, int year,int month,int day, int hour,int minute,int sec){
	      Calendar touch = Calendar.getInstance();
			
			
			touch.set( year, month, day,  hour, minute, sec);
			//Tuesday, 4 September 2012, 3:46:02
			 
			 
			    try
			    {
			        File file = new File(fileName);
			    	long timestamp = touch.getTime().getTime();
					
			        file.setLastModified(timestamp);
			    }  catch(Exception e){
			    	e.printStackTrace();
			    }
			
		}
	/*public static void touch(String fileName, int year,int month,int day, int hour,int minute,int sec){
      Calendar touch = Calendar.getInstance();
		
		//touch.set(2010, 2, 30,23,14,50);
		touch.set(2012, 8, 4,15,46,02);
		//Tuesday, 4 September 2012, 3:46:02
		 
		 
		    try
		    {
		    	  //File file = new File("c:\\Category.class");
		    	  File file = new File("c:\\log4j-1.2.16.jar");
		    	  
		    	long timestamp = touch.getTime().getTime();
				
		        file.setLastModified(timestamp);
		    }  catch(Exception e){
		    	e.printStackTrace();
		    }
		
	}*/
	public static void extractByDoubleQuoute(){
		System.out.println("----------------------------------------------------------------------------");
		String  regexString  ="\"([^\"]*)\"";
		
		Pattern regexPattern = Pattern.compile(regexString);
		Matcher regexMatcher = regexPattern.matcher("\"doubleqoute\",\"christopher anabo\"");
		
		 
		 while (regexMatcher.find()) {
			 
				String link = regexMatcher.group(1); // link
				System.out.println( link );
		 }
		 

	}
	public static void extractBySquareBracket(){
		System.out.println("----------------------------------------------------------------------------");
		String  regexString =  "\\[(.*?)\\]";
		
		
		Pattern regexPattern = Pattern.compile(regexString);
		Matcher regexMatcher = regexPattern.matcher("[square bracket],[split anabo]");
		
		 
		 while (regexMatcher.find()) {
			 
				String link = regexMatcher.group(1); // link
				System.out.println( link );
		 }
		 
	}
	
	public static void sample(String subjectString,String title){
		System.out.println("-------------------------------" + title + "---------------------------------------------");
		System.out.println("sample text:" + subjectString );
		List<String> matchList = new ArrayList<String>();
		Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
		Matcher regexMatcher = regex.matcher(subjectString);
		while (regexMatcher.find()) {
		    if (regexMatcher.group(1) != null) {
		        // Add double-quoted string without the quotes
		        matchList.add(regexMatcher.group(1));
		        System.out.println(regexMatcher.group(1) );
		    } else if (regexMatcher.group(2) != null) {
		        // Add single-quoted string without the quotes
		        matchList.add(regexMatcher.group(2));
		        System.out.println(regexMatcher.group(2) );
		    } else {
		        // Add unquoted word
		        matchList.add(regexMatcher.group());
		    }
		}
		
	}
	
   public static void extactDoubleQuoteAndSingleQuote(){
	   System.out.println("----------------------------------------------------------------------------");
	   String subjectString ="This is a string that \"will be\" highlighted when your 'regular expression' matches";
		
		List<String> matchList = new ArrayList<String>();
		Pattern regex = Pattern.compile("m/('.*?'|\".*?\"|\\S+)/g");
		Matcher regexMatcher = regex.matcher(subjectString);
		while (regexMatcher.find()) {
		    if (regexMatcher.group(1) != null) {
		        // Add double-quoted string without the quotes
		        matchList.add(regexMatcher.group(1));
		        System.out.println(regexMatcher.group(1) );
		    } else if (regexMatcher.group(2) != null) {
		        // Add single-quoted string without the quotes
		        matchList.add(regexMatcher.group(2));
		    } else {
		        // Add unquoted word
		        matchList.add(regexMatcher.group());
		    }
		}
		
	}
	
	
}
