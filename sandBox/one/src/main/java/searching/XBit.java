package searching;

import java.util.BitSet;

public class XBit extends BitSet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5988236682468591359L;
	private boolean map;

	

	public XBit(int i) {
		super(i);
	}

	public boolean isMap() {
		return map;
	}

	public void setMap(boolean map) {
		this.map = map;
	}
	
	
}
