package algo;

public class MergeNode {
	
	
	
	
	
	
	  private class Node{
	    int data;
	    Node next;
	    
	    Node(int data){
	    	this.data = data;
	    }
	    
	    @Override
		public String toString() {
			Node cur = this;
			String str = "";
	 
			while(cur != null) {
				str += cur.data+"->";
				cur = cur.next;
			}
	 
			return str;
		}
	}
	
	Node MergeLists(Node list1, Node list2) {
		  if (list1 == null) return list2;
		  if (list2 == null) return list1;

		  if (list1.data < list2.data) {
		    list1.next = MergeLists(list1.next, list2);
		    return list1;
		  } else {
		    list2.next = MergeLists(list2.next, list1);
		    return list2;
		  }
		}

	
	public static void main(String[] args) {
		MergeNode merge = new MergeNode();
		merge.test();
	}
	
	public void test(){
		
		Node n1 = new Node(1);
		Node n3 = new Node(3);
		Node n5 = new Node(5);
		Node n7 = new Node(7);
		Node n9 = new Node(9);
 
		n1.next = n3;
		n3.next = n5;
		n5.next = n7;
		n7.next = n9;
		n9.next = null;
		
		
		Node n2 = new Node(2);
		Node n4 = new Node(4);
		Node n6 = new Node(6);
		Node n8 = new Node(8);
		Node n10 = new Node(10);
 
		n2.next = n4;
		n4.next = n6;
		n6.next = n8;
		n8.next = n10;
		n10.next = null;
		
		Node mSort = MergeLists(n1,n2);
		System.out.println(mSort);
		
		
	}
}
