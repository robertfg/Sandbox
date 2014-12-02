package memory;

public class MemString {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		MemString m = new MemString();
		m.generateString();
		//m  = null;
		while(true){
			
		}
	}
	
	public void generateString(){
		for(int x=0;x<10000000;x++){
			String x1 = new String( "abcdefghijklmnopqrstuvwxyz:" +String.valueOf(x));
		}
		System.out.println("done");
	}

}
