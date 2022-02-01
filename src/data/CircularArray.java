package data;

public class CircularArray {

	private int index = 0;
	private final int[] array;
	private final int size;
	
	public CircularArray(int size) {
		this.size = size;
		array = new int[size];
	}
	
	public void add(int value) {
		array[index % size] = value;
		index++;
	}
	
	public int average() {
		long sum = 0;
		int end = Math.min(index, size);
		for(int i = 0 ; i < end ; i++) {
			sum += array[i];
		}
		return (int) (sum / end);
	}

	public void clear() {
		index = 0;
	}
}
