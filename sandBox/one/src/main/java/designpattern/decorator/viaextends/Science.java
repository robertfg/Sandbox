package designpattern.decorator.viaextends;

public class Science extends GirlDecorator {
	 
	private Girl girl;
 
	public Science(Girl g) {
		girl = g;
	} 
 
	@Override
	public String getDescription() {
		return girl.getDescription() + "+Like Science";
	}
 
	public void caltulateStuff() {
		System.out.println("scientific calculation!");
	}
}