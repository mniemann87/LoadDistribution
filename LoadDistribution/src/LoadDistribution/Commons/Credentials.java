package LoadDistribution.Commons;

public class Credentials {
	/** URL of the ServiceProvider */
	private String serverURL;
	/** How many threads can be handled by the client. */
	private int concurrentCapacity;
	public Credentials(String serverURL, int concurrentCapacity) {
		super();
		this.serverURL = serverURL;
		this.concurrentCapacity = concurrentCapacity;
	}
	
	public Credentials(String serverURL) {
		super();
		this.serverURL = serverURL;
	}

	public String getServerURL() {
		return serverURL;
	}
	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}
	public int getConcurrentCapacity() {
		return concurrentCapacity;
	}
	public void setConcurrentCapacity(int concurrentCapacity) {
		this.concurrentCapacity = concurrentCapacity;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((serverURL == null) ? 0 : serverURL.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Credentials other = (Credentials) obj;
		if (serverURL == null) {
			if (other.serverURL != null)
				return false;
		} else if (!serverURL.equals(other.serverURL))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Credentials [serverURL=" + serverURL + ", concurrentCapacity="
				+ concurrentCapacity + "]";
	}
	
}
