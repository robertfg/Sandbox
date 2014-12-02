package algo;

public class StringWellFormed {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		System.out.println(  StringWellFormed.test("(abc)")   );
		System.out.println(  StringWellFormed.test(")abc")   );
		System.out.println(  StringWellFormed.test("(abc))")   );
		System.out.println(  StringWellFormed.test("((abc)")   );
		System.out.println(  StringWellFormed.test("(abc)")   );
		
		System.out.println(  StringWellFormed.test("abc")   );
		
		
		 
	}
	
	
	public static boolean test(String str){
		
		int ctrOP = 0;
		int ctrCP = 0;
		for (int i = 0; i < str.length(); i++) {
			
			 
			if(str.charAt(i) == '(' ){
				ctrOP++;
			} else if(str.charAt(i) == ')' ){
				ctrCP++;
			}
			
			if(ctrCP>ctrOP){
				return false;
			} else if( ctrOP == ctrCP && i == (str.length() -1) ) {
				return true;
			}
	
		}
		
		
		return false;
		
		
	}

}
