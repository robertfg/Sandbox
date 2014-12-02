package mdx;

import java.util.HashMap;
import java.util.Map;


public class TestMdx {

	public static void main(String[] args){
		
	//	ExecMdxPool mdxPool = new ExecMdxPool(24, 24);
		
		StringBuffer sb = null;
		TestMdx.testNull(sb);
		System.out.println(sb);  
		 
	}
	
	
	
	public static void testNull(StringBuffer sb){
		
		sb = new StringBuffer("christopher");
		
	}
}
