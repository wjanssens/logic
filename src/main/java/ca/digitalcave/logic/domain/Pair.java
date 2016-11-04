package ca.digitalcave.logic.domain;

import java.util.Objects;

public class Pair {
	private final String a;
	private final String b;
	
	public Pair(String a, String b) {
		this.a = a;
		this.b = b;
	}
	
	public String getA() {
		return a;
	}
	public String getB() {
		return b;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pair == false) return false;
		final Pair that = (Pair) obj;
		return (this.a.equals(that.a) && this.b.equals(that.b)) || (this.a.equals(that.b) && this.b.equals(that.a)); 
	}
	
	@Override
	public String toString() {
		return "(" + a + "," + b + ")";
	}

	@Override
	public int hashCode() {
		return a.compareTo(b) < 0 ? Objects.hash(a, b) : Objects.hash(b, a);
	}
}
