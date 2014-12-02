package eye;

public interface OffHeapStr {
	
	   public void setIndex(int index);
	   
	   public int getIndex();
		  
	   public void setValue(byte[] value);
	   
	   public byte[] getValue(int index);
	   
	   //get object index using byte[] value
	   public int getIndex(byte[] value); 
	   
	   public void moveTo(int index);
	   
	   public boolean exist(byte[] value);
	   
	   public int create(byte[] value);
}
