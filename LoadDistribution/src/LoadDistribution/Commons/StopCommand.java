package LoadDistribution.Commons;

public class StopCommand {
	/** Hashvalue of job that should be stopped */
	private int job;

	public StopCommand(int job) {
		super();
		this.job = job;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}
	
	
	
}
