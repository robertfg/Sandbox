package lamda.collection;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserTest {
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

	  private static void oldJavaWay() {
	    Collections.sort(users, new Comparator<User>() {
	      public int compare(User u1, User u2) {
	        return u1.id.compareTo(u2.id);
	      }
	    });

	    printListOldWay("by ID");

	    Collections.sort(users, new Comparator<User>() {
	      public int compare(User u1, User u2) {
	        return u1.firstName.compareTo(u2.firstName);
	      }
	    });

	    printListOldWay("by FirstName");

	    Collections.sort(users, new Comparator<User>() {
	      public int compare(User u1, User u2) {
	        return u1.lastName.compareTo(u2.lastName);
	      }
	    });

	    printListOldWay("by LastName");

	    Collections.sort(users, new Comparator<User>() {
	      public int compare(User u1, User u2) {
	        return u1.age.compareTo(u2.age);
	      }
	    });

	    printListOldWay("by Age");
	  }

	  private static void printListOldWay(String type) {
	    System.out.println("Old Way " + type + ":");

	    for (User u : users) {
	      System.out.println("\t" + u);
	    }

	    System.out.println();
	  }

	  private static void newJavaWay() {
	    Collections.sort(users, (User u1, User u2) -> u1.id.compareTo(u2.id));
	    printListNewWay("by ID");

	    Collections.sort(users, (User u1, User u2) -> u1.firstName.compareTo(u2.firstName));
	    printListNewWay("by FirstName");

	    Collections.sort(users, (User u1, User u2) -> u1.lastName.compareTo(u2.lastName));
	    printListNewWay("by LastName");

	    Collections.sort(users, (User u1, User u2) -> u1.age.compareTo(u2.age));
	    printListNewWay("by Age");

	  }

	  private static void printListNewWay(String type) {
	    System.out.println("New Way " + type + ":");

	    users.forEach(u -> System.out.println("\t" + u));

	    System.out.println();
	  }
	}