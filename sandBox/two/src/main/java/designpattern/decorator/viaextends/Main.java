package designpattern.decorator.viaextends;

public class Main {
	 
	public static void main(String[] args) {
		
		Girl g1 = new AmericanGirl();
		System.out.println(g1.getDescription());
 
		Science g2 = new Science(g1);
		System.out.println(g2.getDescription()); 
 
		Art g3 = new Art(g2);
		System.out.println(g3.getDescription());
		
	}
}