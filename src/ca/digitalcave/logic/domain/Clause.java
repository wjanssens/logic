package ca.digitalcave.logic.domain;


public class Clause {
	private final Pair pair;
	private final boolean truth;
	
	public Clause(Pair pair, boolean truth) {
		this.pair = pair;
		this.truth = truth;
	}
	
	public Pair getPair() {
		return pair;
	}
	
	public boolean isTruth() {
		return truth;
	}
	
	public int getFactor() {
		return truth ? 1 : -1;
	}
	
	@Override
	public String toString() {
		return (truth ? "" : "-") + pair.toString();
	}
}
