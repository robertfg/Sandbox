package memory;

import java.util.LinkedList;
import java.util.List;

public class finalizer {
	protected Object ref;

	public finalizer(Object ref){
		this.ref = ref;
	}

	public static void main(String[] args) {
		while (true) {
			List x = new LinkedList();

			for (int i = 0; i < 100000; i++) {
				x.add(new finalizer(x));
			}

			x = null;

			System.out.println("" + Runtime.getRuntime().freeMemory() + " bytes free!");
		}
	}
}