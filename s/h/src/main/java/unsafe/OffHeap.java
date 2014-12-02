package unsafe;

public interface OffHeap {

	
		    public void navigate(int index);
			public void setInt(int value);
			public void setLong(long value);
			public void setByte(byte value);
			public int  getInt();
			public long getLong();
			public byte getByte();
			public boolean contains(int index);


}
