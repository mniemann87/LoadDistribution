package LoadDistribution.ServiceProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class JobExecuter implements Runnable {
	/** Temporary set of finished jobs */
	private Set<Runnable> finished;
	/** Pending jobs */
	private Set<Runnable> remaining;
	/** Concurrent jobs */
	private int threads;
	
	private Set<Runner> currentRunners;

	private int WAITING_TIME = 1000;
	private Semaphore barrier;
	
	
	public JobExecuter(int threads) {
		super();
		this.threads = threads;
		currentRunners = new HashSet<Runner>();
		finished = new HashSet<Runnable>();
		remaining = new HashSet<Runnable>();
	}
	@Override
	public void run() {
		barrier = new Semaphore(threads);
		while (true){
			Runnable job;
			while ((job = getRandomRunnable(remaining)) != null){
				barrier.acquireUninterruptibly();
				Runner r = new Runner(job, barrier, WAITING_TIME);
				currentRunners.add(r);
			}
			for (Runner r : currentRunners){
				if (r.isFinished()){
					finished.add(r);
					currentRunners.remove(r);
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public void stopRunnable(Runnable r){
		currentRunners.remove(r);
		remaining.remove(r);
	}
	private Runnable getRandomRunnable(Set<Runnable> set){
		for (Runnable r : set){
			return r;
		}
		return null;
	}
	public Set<Runnable> getFinished(){
		Set<Runnable> temp = new HashSet<Runnable>();
		temp.addAll(finished);
		finished.clear();
		return temp;
	}
	public void putJob(Runnable job){
		remaining.add(job);
	}
}
