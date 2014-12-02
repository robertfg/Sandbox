package lamda.collection;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/*
 * Functions as Paramters

In this section we will show how to pass a function as a parameter to another function.
In this example, we will use the User class which we created in a previous section.
Let's say that we have several different ways to print out a user, different formats that we want to show. 
We can create a function for each of these formats.
We will again show the old and new ways of doing this. 
Each of the ways will use the 3 functions that we created for printing in different formats.

*/

public class UserFormatTest {
	  private static List<User> users = Arrays.asList(
	      new User(1, "Steve", "Vai", 40),
	      new User(4, "Joe", "Smith", 32),
	      new User(3, "Steve", "Johnson", 57),
	      new User(9, "Mike", "Stevens", 18),
	      new User(10, "George", "Armstrong", 24),
	      new User(2, "Jim", "Smith", 40),
	      new User(8, "Chuck", "Schneider", 34),
	      new User(5, "Jorje", "Gonzales", 22),
	      new User(6, "Jane", "Michaels", 47),
	      new User(7, "Kim", "Berlie", 60)
	    );

	  public static void main(String[] args) {
	    oldJavaWay();
	    newJavaWay();
	  }

	  private static void printUserFormat1(User u) {
	    System.out.println("\tUser: " + u.id + " Name: " + u.firstName + " " + u.lastName + " Age: " + u.age);
	  }

	  private static void printUserFormat2(User u) {
	    System.out.println("\tUser: " + u.id + " First Name: " + u.firstName + " Last Name: " + u.lastName + " Age: " + u.age);
	  }

	  private static void printUserFormat3(User u) {
	    System.out.println("\tUser: " + u.lastName + ", " + u.firstName + " Age: " + u.age);
	  }

	  private static void oldWayPrintUsersFormat1() {
	    for (User u : users) {
	      printUserFormat1(u);
	    }
	  }

	  private static void oldWayPrintUsersFormat2() {
	    for (User u : users) {
	      printUserFormat2(u);
	    }
	  }

	  private static void oldWayPrintUsersFormat3() {
	    for (User u : users) {
	      printUserFormat3(u);
	    }
	  }

	  private static void oldJavaWay() {
	    System.out.println("OLDWAY Format 1:");
	    oldWayPrintUsersFormat1();

	    System.out.println("OLDWAY Format 2:");
	    oldWayPrintUsersFormat2();

	    System.out.println("OLDWAY Format 3:");
	    oldWayPrintUsersFormat3();
	  }

	  public static void printUsersNewWay(Consumer<User> func) {
	    users.forEach(u -> func.accept(u));
	  }

	  private static void newJavaWay() {
	    System.out.println("NEWWAY Format 1:");
	    printUsersNewWay(UserFormatTest::printUserFormat1);

	    System.out.println("NEWWAY Format 2:");
	    printUsersNewWay(UserFormatTest::printUserFormat2);

	    System.out.println("NEWWAY Format 3:");
	    printUsersNewWay(UserFormatTest::printUserFormat3);
	  }
	}