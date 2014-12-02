import java.util.ArrayList;
import java.util.List;


public class AppLauncher {

	public static void main(String[] args) {

		String c = "chris";
		
		c = c.replace("h", "H");
		
		System.out.println(c);
		
		
		List<String> list = new ArrayList<String>();
		
					 list.add(null);
					 list.add(null);
					 list.add(null);
					 list.add(null);
			
			for (String string : list) {
					System.out.println(string);
			} 
			
			if(list.contains(null)){
				System.out.println("NULL");
			}		 
		
			
		
		
	}

}
