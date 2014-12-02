package divideAndCounquer;

public class ThreadAggregator  implements Runnable {
		private int[] a;
		private int min, max;
		private int sum;
		
		public ThreadAggregator(int[] a, int min, int max) {
			this.a = a;
			this.min = min;
			this.max = max;
		}
		
		public int getSum() {
			return sum;
		}
		
		public void run() {
			this.sum =  ThreadedNumberAggregator.sumRange(a, min, max);
		}
	
}
