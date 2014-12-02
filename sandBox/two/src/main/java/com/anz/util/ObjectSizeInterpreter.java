package com.anz.util;

import java.text.MessageFormat;
import java.util.List;

public class ObjectSizeInterpreter {

	  /**
	  * @param aLine is a non-null, package-qualified name of a class.
	  * @param aResult is a non-null, empty List which acts as an "out"
	  * parameter; when returned, aResult must contain a non-null, non-empty
	  * List containing a description of the size of the object.
	  *
	  * @return true only if the user has requested to quit the Interpreter.
	  * @exception IllegalArgumentException if a param does not comply.
	  */
	  public boolean parseInput (String  aLine, final List aResult) {
	    if ( aResult == null ) {
	      throw new IllegalArgumentException("Result param cannot be null.");
	    }
	    if ( !aResult.isEmpty() ){
	      throw new IllegalArgumentException("Result param must be empty.");
	    }
	    if ( aLine == null ) {
	      throw new IllegalArgumentException("Line must not be null.");
	    }

	    boolean hasRequestedQuit = aLine.trim().equalsIgnoreCase(fQUIT) ||
	                               aLine.trim().equalsIgnoreCase(fEXIT);
	    if ( hasRequestedQuit ) {
	      //display only a blank line
	      aResult.add(fNEW_LINE);
	    }
	    else {
	      try {
	        Class theClass = Class.forName(aLine);
	        long size = ObjectSizer.getObjectSize( theClass );
	        if ( size > 0 ){
	          Object[] insertedData = {theClass, new Long(size) };
	          MessageFormat sizeMessage = new MessageFormat(fPATTERN);
	          String message = sizeMessage.format( insertedData );
	          aResult.add( message );
	          aResult.add( fNEW_LINE );
	        }
	        aResult.add( fDEFAULT_PROMPT ); 
	      }
	      catch (ClassNotFoundException ex){
	        //recover by asking the user for corrected input
	        aResult.clear();
	        aResult.add(fERROR_PROMPT);
	      }
	    }

	    if ( aResult.isEmpty() ) {
	      throw new IllegalStateException("Result must be non-empty.");
	    }
	    return hasRequestedQuit;
	  }

	  /**
	  * Return the text to be displayed upon start-up of the Interpreter.
	  */
	  public String getHelloPrompt() {
	    return fHELLO_PROMPT;
	  }

	  /// PRIVATE /////
	  private static final String fHELLO_PROMPT = "Please enter a class name>";
	  private static final String fDEFAULT_PROMPT = "Please enter a class name>";
	  private static final String fERROR_PROMPT = "Invalid.  Example:\"java.lang.String\">";
	  private static final String fPATTERN = "Approximate size of {0} objects in bytes: {1}";
	  private static final String fQUIT = "quit";
	  private static final String fEXIT = "exit";
	  private static final String fNEW_LINE = System.getProperty("line.separator");
}
