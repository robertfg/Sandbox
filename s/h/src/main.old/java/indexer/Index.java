package indexer;

import sun.misc.Unsafe;

public class Index {

	private Unsafe memRegion;

	private static long offset = 0;

	
	private static final long indexOffset     = offset += 0;
	private static final long quantityOffset  = offset += 8;
	private static final long tradeIdOffset   = offset += 8;
	private static final long instrumentCodeOffset = offset += 4;
	 
	private static final long valueCodeOffset = offset += 8;
	

	private static final long objectSize = offset += 2;

	private long objectOffset;

	public static long getObjectSize() {
		return objectSize;
	}

	void setObjectOffset(final long objectOffset) {
		this.objectOffset = objectOffset;
	}

	public long getIndex() {
		return memRegion.getLong(objectOffset + indexOffset);
	}

	public void setIndex(final long i) {
		memRegion.putLong(objectOffset + indexOffset, i);
	}

	public void setValueCode(final int valueCode) {
		memRegion.putInt(objectOffset + valueCodeOffset, valueCode);
	}

	public int getValueCode() {
		return memRegion.getInt(objectOffset + valueCodeOffset);
	}

	public long getQuantity() {
		return memRegion.getLong(objectOffset + quantityOffset);
	}

	public void setQuantity(final long quantity) {
		memRegion.putLong(objectOffset + quantityOffset, quantity);
	}

	public long getTradeId() {
		return memRegion.getInt(objectOffset + tradeIdOffset);
	}

	public void setTradeId(final int tradeId) {
		memRegion.putInt(objectOffset + tradeIdOffset, tradeId);
	}

	 public int getInstrumentCode()
     {
         return memRegion.getInt(objectOffset + instrumentCodeOffset);
     }

     public void setInstrumentCode(final int instrumentCode)
     {
    	 memRegion.putInt(objectOffset + instrumentCodeOffset, instrumentCode);
     }

     
	public Unsafe getMemRegion() {
		return memRegion;
	}

	public void setMemRegion(Unsafe memRegion) {
		this.memRegion = memRegion;
	}

}
