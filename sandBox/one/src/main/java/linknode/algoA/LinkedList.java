package linknode.algoA;

/**
 * A simple Linked List class
 * 
 * @author Umair Saeed
 * @version 1.0
 * 
 */
public class LinkedList {
	private IntegerNode head;

	/**
	 * @return the head of the linked list
	 */
	public IntegerNode getHead() {
		return head;
	}

	/**
	 * @return the last element of the linked list
	 */
	public IntegerNode getTail() {
		if (this.getHead() == null) {
			return null;
		}

		IntegerNode current = head;

		while (current.getNext() != null) {
			current = current.getNext();
		}

		return current;
	}

	/**
	 * Searches the linked list, and checks if it contains the given node
	 * 
	 * @param node
	 *            the node to be searched for
	 * @return true if the linked list contains the given node, false otherwise
	 */
	public boolean contains(IntegerNode node) {
		boolean found = false;

		if (this.getHead() == null) {
			return found;
		}
		
		IntegerNode current = head;
		while (current != null) {
			if (current.equals(node)) {
				found = true;
				break;
			}
			current = current.getNext();
		}

		return found;
	}

	/**
	 * Adds a node to the head of the linked list
	 * 
	 * @param newNode
	 *            node to be added to the head of the linked list
	 * 
	 */
	public void addToHead(IntegerNode newNode) {
		IntegerNode current = head;
		if (newNode != null) {
			head = newNode;
			head.setNext(current);
		}
	}

	/**
	 * Adds a node to the tail of the linked list
	 * 
	 * @param newNode
	 *            node to be added to the tail of the list
	 */
	public void addToTail(IntegerNode newNode) {
		if (head == null) {
			head = newNode;
		} else {
			IntegerNode current = head;

			while (current.getNext() != null) {
				current = current.getNext();
			}

			current.setNext(newNode);
		}
	}

	/**
	 * Empties out the linked list
	 */
	public void clear(){
		head = null;
	}
	
	/**
	 * Deletes the first occurrence of the specified node from the the linked
	 * list.
	 * 
	 * @param nodeToDelete
	 *            node to be deleted
	 */
	public void removeNode(IntegerNode nodeToDelete) {
		IntegerNode current = head;

		// If the linked list is empty, nothing to do
		if (head == null) {
			return;
		}

		// If the node to be deleted is the first element, update head
		if (head.equals(nodeToDelete)) {
			IntegerNode old = head;
			head = head.getNext();
			old.setNext(null);
			return;
		}

		while (current.getNext() != null) {
			if (current.getNext().equals(nodeToDelete)) {
				IntegerNode matchedNode = current.getNext();
				current.setNext(matchedNode.getNext());
				matchedNode.setNext(null);
				return;
			} else {
				current = current.getNext();
			}
		}
	}

	/**
	 * Calculates the total number of elements in the linked list
	 * 
	 * @return the total number of elements (nodes) in the linked list
	 */
	public int size() {
		int count = 0;
		IntegerNode current = head;

		while (current != null) {
			current = current.getNext();
			count++;
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		IntegerNode current = head;
		StringBuilder description = new StringBuilder();

		while (current != null) {
			description.append(current.toString());
			description.append(" ");
			current = current.getNext();
		}

		return description.toString();
	}
}
