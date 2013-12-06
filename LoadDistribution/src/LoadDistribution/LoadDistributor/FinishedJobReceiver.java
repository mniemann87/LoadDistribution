package LoadDistribution.LoadDistributor;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import LoadDistribution.Commons.Credentials;
import LoadDistribution.Commons.MetaCommand;

public class FinishedJobReceiver implements Runnable{
	/** Port that is listened on */
	private int receivingPort;
	
	/** Is finished */
	private boolean finished = false;
	
	/** Yet received jobs */
	private Set<Runnable> received;
	
	/** Available receivers */
	private Map<Credentials, Long> availableReceivers;
	
	/** Set of all registered receivers */
	private Map<Integer, Credentials> allReceivers;
	
	/** Timeout for availability */
	private int timeout;
	
	public FinishedJobReceiver(int receivingPort, int timeout, Set<Credentials> allReceivers) {
		super();
		this.receivingPort = receivingPort;
		availableReceivers = new HashMap<Credentials, Long>();
		this.timeout = timeout;  
		this.allReceivers = new HashMap<Integer, Credentials>();
		for (Credentials cr : allReceivers){
			this.allReceivers.put(cr.hashCode(), cr);
		}
	}

	@Override
	public void run() {
		received = new HashSet<Runnable>();
		ServerSocket socket;
		try {
			socket = new ServerSocket(receivingPort);
			while (!finished) {
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
							received.add((Runnable) input);
						}
						if (input instanceof MetaCommand){
							//save incoming "AVAILABLE" as successfull provider
							if (input.equals(MetaCommand.AVAILABLE)){
								Credentials incoming = new Credentials(socket.getInetAddress().getCanonicalHostName());
								System.out.println(incoming);
								if (allReceivers.containsKey(incoming.hashCode())){
									availableReceivers.put(incoming, System.currentTimeMillis());
								}
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
	}

	public Set<Runnable> getReceived() {
		Set<Runnable> temp = received;
		received = new HashSet<Runnable>();
		return temp;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public Map<Credentials, Long> getAvailableReceivers() {
		//delete timed out receivers
		Set<Credentials> removeBuffer = new HashSet<Credentials>();
		for (Credentials cr : availableReceivers.keySet()){
			if (System.currentTimeMillis() - availableReceivers.get(cr) > timeout){
				removeBuffer.add(cr);
			}
		}
		for (Credentials rem : removeBuffer){
			availableReceivers.remove(rem);
		}
		return availableReceivers;
	}
	
}
