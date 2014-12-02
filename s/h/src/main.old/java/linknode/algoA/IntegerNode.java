package linknode.algoA;

/**
 * Represents a node of a linked list
 * 
 * @author Umair Saeed
 * @version 1.0
 * 
 */
class IntegerNode {
	private IntegerNode next = null;
	private int data;

	/**
	 * @param data
	 */
	public IntegerNode(int data) {
		this.setData(data);
		this.setNext(null);
	}

	/**
	 * Sets the pointer to the next node, when used as a node in a linked list.
	 * 
	 * @see LinkedList
	 * 
	 * @param next
	 *            an instance of LinkedListNode that will be pointed to by the
	 *            this instance
	 */
	public void setNext(IntegerNode next) {
		this.next = next;
	}

	/**
	 * Returns the next node object
	 * 
	 * @return 	the next node
	 */
	public IntegerNode getNext() {
		return next;
	}

	/**
	 * @param data
	 */
	public void setData(int data) {
		this.data = data;
	}

	/**
	 * @return
	 */
	public int getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Integer.toString(this.getData());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + data;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntegerNode other = (IntegerNode) obj;
		if (data != other.getData())
			return false;
		return true;
	}

}