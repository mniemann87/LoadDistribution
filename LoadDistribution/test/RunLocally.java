import java.util.concurrent.Semaphore;


public class RunLocally {
	private static int THREADS = 4;
	public static void main(String[] args) {
		Semaphore barrier = new Semaphore(THREADS);
		long before = System.currentTimeMillis();
		for (int i = 0; i < THREADS; i++){
			barrier.acquireUninterruptibly();
			PrimeTest p = new PrimeTest(10000000, barrier);
			Thread t = new Thread(p);
			t.start();
		}
		barrier.acquireUninterruptibly(THREADS);
		System.out.println("finished in " + (System.currentTimeMillis() - before) + " ms");
	}
}
