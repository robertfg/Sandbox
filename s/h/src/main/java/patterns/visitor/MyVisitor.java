package patterns.visitor;

import java.util.HashMap;
import java.util.Map;

public class MyVisitor implements GenericVisitor<MyClass> {
	
    public void visit(MyClass myClass) {
    }


    public static void main(String[] args){
    	
    	Map<String,String> map = new HashMap<String,String>();
    	
    	map.put("A", "A");
    	map.put("A","A1");
    	System.out.println(map.get("A"));
    }

}