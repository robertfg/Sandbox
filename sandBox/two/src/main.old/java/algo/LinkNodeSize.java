package algo;

/**
 * Read more: http://javarevisited.blogspot.com/2010/10/how-do-you-find-length-of-singly-linked.html#ixzz2x254zLHA
 * @author anaboc
 *
 */
public class LinkNodeSize {

	public static void main(String[] args) {
	
		LinkNodeSize nodeSize = new LinkNodeSize();
	
		LinkNodeSize.Node node5 = nodeSize.new  Node(null);
		LinkNodeSize.Node node4 = nodeSize.new  Node(node5);
		LinkNodeSize.Node node3 = nodeSize.new  Node(node4);
		LinkNodeSize.Node node2 = nodeSize.new  Node(node3);
		LinkNodeSize.Node node1 = nodeSize.new  Node(node2);
		LinkNodeSize.Node node0 = nodeSize.new  Node(node1);
			
		
		
		System.out.println( "Node size:" +  LinkNodeSize.length(node5) );
		System.out.println( "Node size:" +  LinkNodeSize.length(node3) );
		System.out.println( "Node size:" +  LinkNodeSize.length(node0) );
		
	}

	
	public static int length(Node current){
		if(current == null) //base case
		return 0;

		return 1+length(current.next());
		}

	
	public class Node{
		
		Node next;
		
		public Node next() {
			return next;
		}

		Node(Node node){
			
			next = node;
		}
		
		
	}
}
