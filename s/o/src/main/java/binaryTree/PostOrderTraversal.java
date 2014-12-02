package binaryTree;

import java.util.Stack;

/*
 http://www.java2blog.com/2014/07/binary-tree-postorder-traversal-in-java.html

 Iterative solution:
 Steps for iterative solution:

 1. 	Create an empty stack s and set currentNode =root.

 2. while currentNode is not NULL Do following
 Push currentNode 's right child and then currentNode to stack.
 Set currentNode =currentNode .left

 3.Pop an node from stack and set it as root and set it to currentNode 
 If the popped node has a right child and the right child is at top of stack, then remove the right child from stack, push the current node back and set currentNode as currentNode 's right child.
 Else print currentNode 's data and set currentNode as NULL.

 4.Repeat steps 2 and 3 while stack is not empty.

 */
public class PostOrderTraversal {

	public static class TreeNode {
		int data;
		TreeNode left;
		TreeNode right;

		TreeNode(int data) {
			this.data = data;
		}
	}

	// Recursive Solution
	public void postOrder(TreeNode root) {
		if (root != null) {
			postOrder(root.left);
			postOrder(root.right);
			// Visit the node by Printing the node data
			System.out.printf("%d ", root.data);
		}
	}

	// Iterative solution
	public void postorderIter(TreeNode root) {
		if (root == null)
			return;

		Stack<TreeNode> s = new Stack<TreeNode>();
		TreeNode current = root;

		while (true) {

			if (current != null) {
				if (current.right != null)
					s.push(current.right);
				s.push(current);
				current = current.left;
				continue;
			}

			if (s.isEmpty())
				return;
			current = s.pop();

			if (current.right != null && !s.isEmpty()
					&& current.right == s.peek()) {
				s.pop();
				s.push(current);
				current = current.right;
			} else {
				System.out.print(current.data + " ");
				current = null;
			}
		}
	}

	public static void main(String[] args) {
		PostOrderTraversal bi = new PostOrderTraversal();
		// Creating a binary tree
		TreeNode rootNode = createBinaryTree();
		System.out.println("Using Recursive solution:");

		bi.postOrder(rootNode);

		System.out.println();
		System.out.println("-------------------------");
		System.out.println("Using Iterative solution:");

		bi.postorderIter(rootNode);
	}

	public static TreeNode createBinaryTree() {

		TreeNode rootNode = new TreeNode(40);
		TreeNode node20 = new TreeNode(20);
		TreeNode node10 = new TreeNode(10);
		TreeNode node30 = new TreeNode(30);
		TreeNode node60 = new TreeNode(60);
		TreeNode node50 = new TreeNode(50);
		TreeNode node70 = new TreeNode(70);

		rootNode.left = node20;
		rootNode.right = node60;

		node20.left = node10;
		node20.right = node30;

		node60.left = node50;
		node60.right = node70;

		return rootNode;
	}
}
