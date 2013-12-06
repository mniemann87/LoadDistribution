package LoadDistribution.ServiceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import LoadDistribution.Commons.Credentials;
import LoadDistribution.Commons.MetaCommand;
import LoadDistribution.Commons.StopCommand;

public class Server implements Runnable{
	/** Port to receive on */
	private int receivePort;
	/** Port to send on */
	private int sendPort;
	/** Commander */
	private Credentials commander;
	
	private int threads = 4;
	
	public Server(int receivePort, int sendPort, Credentials commander,
			int threads) {
		super();
		this.receivePort = receivePort;
		this.sendPort = sendPort;
		this.commander = commander;
		this.threads = threads;
	}
	@Override
	public void run() {		
		// listen on port and manage jobs
		ServerSocket socket;
		JobExecuter jobExec = new JobExecuter(threads);
		Map<Integer, Runnable> jobs = new HashMap<Integer, Runnable>();
		Thread jeThread = new Thread(jobExec);
		jeThread.start();
		try {
			socket = new ServerSocket(receivePort);
			while (true) {
				System.out.println("Listening...");

				InputStream inStream;		
				//Incoming connection
				try {
					Socket currSocket = socket.accept();
					System.out.println("Incoming connection : " + currSocket);
					
					//Receive
					inStream = currSocket.getInputStream();
					
					ObjectInputStream oiStream = new ObjectInputStream(inStream);
					Object input = null;
					try {
						input = oiStream.readObject();
						System.out.println("receiving object: " + input.getClass().getName());
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					
					
					if (input != null){
						if (input instanceof Runnable){
							//put job into the job executer
							Runnable job = (Runnable) input;
							jobs.put(job.hashCode(), job);
							jobExec.putJob(job);
						}
						if (input instanceof MetaCommand){
							//save incoming "AVAILABLE" as successfull provider
							if (input.equals(MetaCommand.AVAILABLE)){
								replyAvailableRequest();
							}
						}
						if (input instanceof StopCommand){
							//stop job with the contained ID
							StopCommand stop = (StopCommand)input;
							if (jobs.containsKey(stop.getJob())){
								jobExec.stopRunnable(jobs.get(stop.getJob()));
							}
						}
					}
					oiStream.close();
					currSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// if a job has finished, send it back
	}
	public void replyAvailableRequest(){
		try {
			Socket socket = new Socket(commander.getServerURL(), sendPort);
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
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
