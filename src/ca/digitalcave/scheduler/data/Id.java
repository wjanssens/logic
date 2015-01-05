package ca.digitalcave.scheduler.data;

public class Id<T> {
	private T value;

	public Id() {
	}
	
	public Id(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
}
