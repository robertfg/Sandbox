package designpattern.decorator.viaextends;

public class Art extends GirlDecorator {
	 
	private Girl girl;
 
	public Art(Girl g) {
		girl = g;
	}
 
	@Override
	public String getDescription() {
		return girl.getDescription() + "+Like Art";
	}
 
	public void draw() {
		System.out.println("draw pictures!");
	}
}