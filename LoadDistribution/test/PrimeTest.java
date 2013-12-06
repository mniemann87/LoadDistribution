import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


public class PrimeTest implements Runnable {
	private int upToValue;
	private Semaphore barrier;
	public PrimeTest(int upToValue, Semaphore barrier){
		this.upToValue = upToValue;
		this.barrier = barrier;
	}
	@Override
	public void run() {
		List<Integer> list = init(upToValue);
		List<Integer> primes = new ArrayList<Integer>();
		for (int i = 2; i < upToValue; i++){
			if (list.contains(i)){
				primes.add(i);
			}
			for (int j = i * i; j < upToValue; j += i){
				int index = list.indexOf(j);
				if (index >= 0){
					list.remove(index);
				}
			}
		}
		barrier.release();
	}
	private List<Integer> init(int elements){
		List<Integer> result = new ArrayList<Integer>();
		for (int k = 2; k < elements; k++){
			result.add(k);
		}
		return result;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + upToValue;
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
		PrimeTest other = (PrimeTest) obj;
		if (upToValue != other.upToValue)
			return false;
		return true;
	}
	
}
