package tradelifecycle;

public class Trade {

	private int id;
	private int version;
	private String secIdentifier;
	private int quantity;
	private Direction direction;
    private String actNumber;
    private Operation operation;
    
    public Trade(){};
    
	public Trade(int id, int version, String secIdentifier, int quantity,
			Direction direction, String actNumber, Operation operation) {
		super();
		this.id = id;
		this.version = version;
		this.secIdentifier = secIdentifier;
		this.quantity = quantity;
		this.direction = direction;
		this.actNumber = actNumber;
		this.operation = operation;
	}
	
	



	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getSecIdentifier() {
		return secIdentifier;
	}
	public void setSecIdentifier(String secIdentifier) {
		this.secIdentifier = secIdentifier;
	}
	public int getQuantity() {
		return quantity;
	}
	
	public void setQuantity(int quatity) {
		this.quantity = quatity;
	}
	public Direction getDirection() {
		return direction;
	}
	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	public String getActNumber() {
		return actNumber;
	}
	public void setActNumber(String actNumber) {
		this.actNumber = actNumber;
	}
	public Operation getOperation() {
		return operation;
	}
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	
	
	

}
