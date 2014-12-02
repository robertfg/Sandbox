package lamda.reallife;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lamda.stream.Employee;

//Read more: http://javarevisited.blogspot.com/2014/03/2-examples-of-streams-with-Java8-collections.html#ixzz3JgVq5MkV
public class StreamDemo {


	public static void main(String args[]) {

		
		// Initialization of Collection
		
		
				Order buyGoogle = new Order("GOOG.NS", 300, 900.30, Order.Side.BUY);
				Order sellGoogle = new Order("GOOG.NS", 600, 890.30, Order.Side.SELL);
				Order buyApple = new Order("APPL.NS", 400, 552, Order.Side.BUY);
				Order sellApple = new Order("APPL.NS", 200, 550, Order.Side.SELL);
				Order buyGS = new Order("GS.NS", 300, 130, Order.Side.BUY);
				
				
				List<Order> orderBook = new ArrayList<>();
							
							orderBook.add(buyGoogle);
							orderBook.add(sellGoogle);
							orderBook.add(buyApple);
							orderBook.add(sellApple);
							orderBook.add(buyGS); // Java 8 Streams Example 1 : Filtering Collection elements
				
				// Filtering buy and sell order using filter() method of java.util.Stream class 
				
				///Stream<Order> stream = orderBook.stream();
				
				Stream<Order> stream = orderBook.parallelStream();
				
				 Predicate<Order> filterPredicate = o ->o.side().equals(Order.Side.BUY);
			        
				Stream<Order> buyOrders = stream.filter(o -> o.side().equals(Order.Side.BUY) );
										 
				System.out.println("No of Buy Order Placed :" + buyOrders.count()); 
				Stream<Order> sellOrders = orderBook.stream().filter(  o -> o.side() == Order.Side.SELL);
										 
				System.out.println("No of Sell Order Placed : " + sellOrders.count()); // Java 8 Streams Example 2 : Reduce or Fold operation //
										 
				//Calculating total value of all orders 
					
				double value = orderBook.stream().mapToDouble( o -> o.price()).sum();
										 
				System.out.println("Total value of all orders : " + value); 
				
				long quantity = orderBook.stream().mapToLong(o -> o.quantity()).sum();
				
				System.out.println("Total quantity of all orders : "+ quantity); 
				} 
	 
	}

