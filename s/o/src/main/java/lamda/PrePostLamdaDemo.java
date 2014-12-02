package lamda;

public class PrePostLamdaDemo {

	public static void main(String[] args) {
		// Need not be declared as final for use within a
		// lambda expression, but has to be eventually final.
		String outsideOfImpl = "Common Value";

		doSomeProcessing("123", (String id, int status) -> {
			System.out.println("Finding some data based on" + id);
			System.out.println(outsideOfImpl);
			System.out.println("Assert that the status is " + status);
		});

		doSomeProcessing("456", (String id, int status) -> {
			System.out.print("Finding data based on id: " + id);
			System.out.println(outsideOfImpl);
			System.out.println("And updating the status: " + status);
		});
		
	}

	static void doSomeProcessing(String id, Performer performer) { 
		System.out.println("Pre-Processing...");
		
		System.out.println("Finding status for given id: " + id);
		int status = 2;
		performer.performTask(id, status);
	
		System.out.println("Post-processing...");
	
	}
}

interface Performer {
	public void performTask(String id, int status);
}