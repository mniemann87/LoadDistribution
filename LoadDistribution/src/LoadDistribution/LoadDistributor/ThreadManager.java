package LoadDistribution.LoadDistributor;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import LoadDistribution.Commons.Credentials;
import LoadDistribution.Commons.MetaCommand;
import LoadDistribution.Commons.StopCommand;
/**
 * A ThreadManager distributes jobs to service providers and waits until all jobs are done.
 * 
 * @author Matthias Niemann
 *
 */
public class ThreadManager implements Runnable{
	/** When released, the manager has finished, */ 
	private Semaphore manager;
	/** Map of jobs that will be distributed to providers. */
	private Set<Runnable> jobs;
	/** Set of connection data for the participating providers. */
	private Set<Credentials> credentials;
	/** When did the job started */
	
	private Map<Runnable, Long> jobStartTime;
	/** Flag forces the manager to stop all distributed tasks */
	private boolean stop = false;
	/** ms until the thread will be reassigned */
	private int timeout = 30000;
	/** Port for receiving finished jobs */
	private int receivingPort;
	/** Port for sending jobs */
	private int sendingPort;
	/** Set of jobs that has finished */
	private Set<Runnable> finishedJobs;
	
	public ThreadManager(Semaphore manager, Set<Runnable> jobs,
			Set<Credentials> credentials, int timeout, int receivingPort,
			int sendingPort) {
		super();
		this.manager = manager;
		this.jobs = jobs;
		this.credentials = credentials;
		this.timeout = timeout;
		this.receivingPort = receivingPort;
		this.sendingPort = sendingPort;
	}
	
	@Override
	public void run() {
		//create and start receiver
		FinishedJobReceiver receiver = new FinishedJobReceiver(receivingPort, timeout, credentials);
		Thread receiverThread = new Thread(receiver);
		receiverThread.start();
		
		//copy set of all jobs
		Set<Runnable> remainingJobs = new HashSet<Runnable>();
		finishedJobs = new HashSet<Runnable>();

		for (Runnable job : jobs){
			remainingJobs.add(job);
		}
		//how many threads are currently processed by a provider
		Map<Credentials, Integer> currentJobs = new HashMap<Credentials, Integer>();
		Map<Integer, Credentials> jobOnProvider = new HashMap<Integer, Credentials>();
		
		boolean finished = false;
		long lastAvailabilityCheck = 0;
		while (!stop && !finished){
			//send broadcast to check availability
			if (System.currentTimeMillis() - lastAvailabilityCheck > timeout){
				sendAvailabilityBroadcast();
				lastAvailabilityCheck = System.currentTimeMillis();
			}
			
			//send as many jobs as possible
			if (!remainingJobs.isEmpty()){
				//iterate over all providers
				for (Credentials currentReceiver : credentials){
					if (receiver.getAvailableReceivers().containsKey(currentReceiver.hashCode())){
						//initialize current number of jobs
						if (!currentJobs.containsKey(currentReceiver)){
							currentJobs.put(currentReceiver, 0);
						}
						//iterate over providers available cores
						while (currentReceiver.getConcurrentCapacity() - currentJobs.get(currentReceiver) > 0){
							//select a job
							Runnable nextJob = returnRandomJob(remainingJobs);
							if (nextJob != null){
								//send it to server
								boolean successful = sendJobToProvider(nextJob, currentReceiver);
								if (successful){
									currentJobs.put(currentReceiver, currentJobs.get(currentReceiver) + 1);
									jobOnProvider.put(nextJob.hashCode(), currentReceiver);
								}
							}
						}
					}
				}
			}
			//receive finished jobs
			finishedJobs.addAll(receiver.getReceived());
			
			//are there timed out jobs?
			for (Runnable r : jobStartTime.keySet()){
				if (System.currentTimeMillis() - jobStartTime.get(r) > timeout){
					//stop job
					stopJobOnProvider(r.hashCode(), jobOnProvider.get(r.hashCode()));
					//put it back to the pool
					remainingJobs.add(r);
				}
			}
			
			//all jobs received?
			finished = finishedJobs.size() == jobs.size();
		}
		manager.release();
	}
	/**
	 * Returns a random job of the given set
	 * @param set pool
	 * @return first item of pool
	 */
	private Runnable returnRandomJob(Set<Runnable> set){
		for (Runnable current : set){
			return current;
		}
		return null;
	}
	/**
	 * Sends a stop command to the server
	 * @param hashValue the jobs hash code
	 */
	private void stopJobOnProvider(Integer hashValue, Credentials provider){
		try {
			Socket socket = new Socket(provider.getServerURL(), sendingPort);
			
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("Stop " + hashValue + " on " + provider);
			oos.writeObject(new StopCommand(hashValue));
			oos.flush();
			oos.close();
			socket.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Sends a job to a service provider
	 * @param job the job
	 * @param receiver the service providers credentials
	 * @return true if it was sent successfully, otherwise false (i.e. could not be reached) 
	 */
	private boolean sendJobToProvider(Runnable job, Credentials receiver){
		System.out.println("Send " + job.hashCode() + " to " + receiver);
		try {
			Socket socket = new Socket(receiver.getServerURL(), sendingPort);
			
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(job);
			oos.flush();
			oos.close();
			socket.close();
			//set time
			jobStartTime.put(job, System.currentTimeMillis());

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void sendAvailabilityBroadcast(){
		for (Credentials cr : credentials){
			try {
				Socket socket = new Socket(cr.getServerURL(), sendingPort);
				
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				System.out.println("Is " + cr + " available?");
				oos.writeObject(MetaCommand.AVAILABLE);
				oos.flush();				
				oos.close();
				socket.close();
	
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public Set<Runnable> getFinishedJobs() {
		return finishedJobs;
	}
	
}
