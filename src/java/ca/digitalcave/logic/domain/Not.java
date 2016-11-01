package ca.digitalcave.logic.domain;

public class Not {

	private final Pair pair;
	
	public Not(Pair pair) {
		this.pair = pair;
	}
	
	@Override
	public String toString() {
		return "\u00AC" + pair;
	}

	public Pair getPair() {
		return pair;
	}
}
