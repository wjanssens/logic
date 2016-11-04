package ca.digitalcave.logic.domain;


public class Clause {
	private final Pair pair;
	private final boolean truth;
	private final String comment;
	
	public Clause(Pair pair, boolean truth, String comment) {
		this.pair = pair;
		this.truth = truth;
		this.comment = comment;
	}
	
	public Pair getPair() {
		return pair;
	}
	
	public boolean isTruth() {
		return truth;
	}

	public String getComment() {
		return comment;
	}

	public int getFactor() {
		return truth ? 1 : -1;
	}
	
	@Override
	public String toString() {
		return (truth ? "" : "-") + pair.toString();
	}
}
