package ca.digitalcave.logic.domain;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Puzzle {
	private final HashMap<String, List<String>> terms = new HashMap<>();
	private final LinkedList<Clause> clauses = new LinkedList<>();
	private final ArrayList<Pair> combinations = new ArrayList<>();
	private final ArrayList<Pair> solution = new ArrayList<>();

	public Puzzle(JsonParser p) throws IOException {
		final Stack<String> stack = new Stack<String>();

		while (p.nextToken() != JsonToken.END_OBJECT) {
			if ("problem".equals(p.getCurrentName())) {
				LinkedList<String> list = new LinkedList<String>();
				while (p.nextToken() != JsonToken.END_OBJECT) {
					if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
						list = new LinkedList<>();
						terms.put(p.getCurrentName(), list);
					}
					else if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
						list.add(p.getText());
					}
				}
			}
			else if ("clauses".equals(p.getCurrentName())) {
				boolean truth = false;
				while (p.nextToken() != JsonToken.END_ARRAY) {
					while (p.nextToken() != JsonToken.END_ARRAY) {
						if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
							stack.push(p.getText());
						}
						else if (p.getCurrentToken() == JsonToken.VALUE_TRUE) {
							truth = true;
						}
						else if (p.getCurrentToken() == JsonToken.VALUE_FALSE) {
							truth = false;
						}
					}
					clauses.push(new Clause(new Pair(stack.pop(), stack.pop()), truth));
				}
			}
		}
		
		// create a List of all valid pairs
		for (Map.Entry<String, List<String>> e : terms.entrySet()) {
			for (Map.Entry<String, List<String>> f : terms.entrySet()) {
				if (e.getKey().equals(f.getKey())) continue; // terms from the same group cannot be valid pairs
				for (String a : e.getValue()) {
					final String fqa = e.getKey() + "::" + a;
					for (String b : f.getValue()) {
						final String fqb = f.getKey() + "::" + b;
						if (fqa.equals(fqb)) {
							continue; // can this even happen?
						}
						final Pair pair = new Pair(fqa, fqb);
						if (combinations.contains(pair)) continue; // ignore reflexive combinations
						combinations.add(pair);
					}
				}
			}
		}
	}
	
	public void solve() throws ContradictionException, TimeoutException {
		final ISolver solver = SolverFactory.newDefault();
		solver.newVar(combinations.size());
//		solver.setExpectedNumberOfClauses(clauses.size());
		
		// the clauses from the valid pair combinations
		for (Map.Entry<String, List<String>> e : terms.entrySet()) {
			for (String a : e.getValue()) {
				final String fqa = e.getKey() + "::" + a;
				final ArrayList<Pair> d = new ArrayList<Pair>();
				for (Map.Entry<String, List<String>> f : terms.entrySet()) {
				if (e.getKey().equals(f.getKey())) continue; // same category
					final ArrayList<Pair> c = new ArrayList<Pair>();
					for (int i = 0; i < f.getValue().size(); i++) {
						final String b = f.getValue().get(i);
						final String fqb = f.getKey() + "::" + b;
						final Pair p = new Pair(fqa,fqb);
						c.add(p);
						d.add(p);
					}
					solver.addExactly(toVecInt(c), 1);
				}
				solver.addExactly(toVecInt(d), terms.size() - 1);
			}
		}
		// the clauses from the problem
		for (Clause clause : clauses) {
			solver.addClause(new VecInt(new int[] { clause.getFactor() * (1+combinations.indexOf(clause.getPair())) }));
		}

		final IProblem problem = solver;
		if (problem.isSatisfiable()) {
			final int[] model = problem.findModel();
			for (int i : model) {
				if (i > 0) {
					solution.add(combinations.get(i-1));
				}
			}
		}
	}
	
	public void write(JsonGenerator g) throws IOException {
		g.writeStartObject();
		g.writeObjectFieldStart("problem");
		for (Map.Entry<String, List<String>> group : terms.entrySet()) {
			g.writeArrayFieldStart(group.getKey());
			for (String term : group.getValue()) {
				g.writeString(term);
			}
			g.writeEndArray();
		}
		g.writeEndObject();
		g.writeArrayFieldStart("clauses");
		for (Clause clause : clauses) {
			g.writeStartArray();
			g.writeString(clause.getPair().getA());
			g.writeString(clause.getPair().getB());
			g.writeBoolean(clause.isTruth());
			g.writeEndArray();
		}
		g.writeEndArray();
		g.writeArrayFieldStart("solution");
		for (Pair pair : solution) {
			g.writeStartArray();
			g.writeString(pair.getA());
			g.writeString(pair.getB());
			g.writeEndArray();
		}
		g.writeEndArray();
		g.writeEndObject();
		g.close();
	}
	
	private VecInt toVecInt(List<Pair> l) {
		final int[] result = new int[l.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = 1+combinations.indexOf(l.get(i));
		}
		return new VecInt(result);
	}

	public ArrayList<Pair> getSolution() {
		return solution;
	}

	public ArrayList<Pair> getCombinations() {
		return combinations;
	}

	public LinkedList<Clause> getClauses() {
		return clauses;
	}

	public HashMap<String, List<String>> getTerms() {
		return terms;
	}
}
