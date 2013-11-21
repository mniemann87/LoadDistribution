package LoadDistribution.ServiceProvider;

import java.util.concurrent.Semaphore;

public class Runner implements Runnable {
	/** The single job that will run within this class */
	private Runnable job;
	/** Barrier is released after job has finished */
	private Semaphore barrier;
	/** Waiting time between checks */
	private int waitingTime;
	
	private boolean finished = false;
	
	public Runner(Runnable job, Semaphore barrier, int waitingTime) {
		super();
		this.job = job;
		this.barrier = barrier;
		this.waitingTime = waitingTime;
	}


	@Override
	public void run() {
		Thread t = new Thread(job);
		t.start();
		while (t.isAlive()){
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
			}
		}
		finished = true;
		barrier.release();
	}

	public Runnable getJob() {
		return job;
	}
	public boolean isFinished(){
		return finished;
	}

}
