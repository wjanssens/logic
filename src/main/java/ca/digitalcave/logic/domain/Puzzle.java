package ca.digitalcave.logic.domain;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Puzzle {
	private final HashMap<String, List<String>> terms = new HashMap<>();
	private final LinkedList<Clause> clauses = new LinkedList<>();
	private final HashSet<Pair> solutionPairs = new HashSet<>();

	public Puzzle(JsonParser p) throws IOException {
		final LinkedList<String> stack = new LinkedList<>();

		while (p.nextToken() != JsonToken.END_OBJECT && p.hasCurrentToken()) {
			if ("dimensions".equals(p.getCurrentName())) {
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
							stack.add(p.getText());
						}
						else if (p.getCurrentToken() == JsonToken.VALUE_TRUE) {
							truth = true;
						}
						else if (p.getCurrentToken() == JsonToken.VALUE_FALSE) {
							truth = false;
						}
					}
					clauses.add(new Clause(new Pair(stack.removeFirst(), stack.removeFirst()), truth, stack.isEmpty() ? null : stack.removeFirst()));
					stack.clear();
				}
			} else if ("pairs".equals(p.getCurrentName())) {
				while (p.nextToken() != JsonToken.END_ARRAY) {
					while (p.nextToken() != JsonToken.END_ARRAY) {
						if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
							stack.add(p.getText());
						}
					}
					solutionPairs.add(new Pair(stack.removeFirst(), stack.removeFirst()));
					stack.clear();
				}
			}
		}
	}
	
	public void solve() throws ContradictionException, TimeoutException {

		System.out.println(terms);
		// create a List of all valid solutionPairs
		final ArrayList<Pair> validPairs = new ArrayList<>();
		for (Map.Entry<String, List<String>> e : terms.entrySet()) {
			for (Map.Entry<String, List<String>> f : terms.entrySet()) {
				if (e.getKey().equals(f.getKey())) continue; // terms from the same group cannot be valid solutionPairs
				for (String a : e.getValue()) {
					for (String b : f.getValue()) {
						if (a.equals(b)) {
							continue; // can this even happen?
						}
						final Pair pair = new Pair(a, b);
						if (validPairs.contains(pair)) continue; // ignore reflexive combinations
						validPairs.add(pair);
					}
				}
			}
		}

		solutionPairs.clear();

		final ISolver solver = SolverFactory.newDefault();
		solver.newVar(validPairs.size());
		solver.setTimeoutMs(1000);
//		solver.setExpectedNumberOfClauses(clauses.size());
		
		// the clauses from the valid pair combinations
		for (Map.Entry<String, List<String>> e : terms.entrySet()) {
			for (String a : e.getValue()) {
				final ArrayList<Pair> d = new ArrayList<>();
				for (Map.Entry<String, List<String>> f : terms.entrySet()) {
				if (e.getKey().equals(f.getKey())) continue; // same category
					final ArrayList<Pair> c = new ArrayList<>();
					for (int i = 0; i < f.getValue().size(); i++) {
						final String b = f.getValue().get(i);
						final Pair p = new Pair(a,b);
						c.add(p);
						d.add(p);
					}
					solver.addExactly(toVecInt(c, validPairs), 1);
				}
				solver.addExactly(toVecInt(d, validPairs), terms.size() - 1);
			}
		}

		// (a,b)&(a,c)=>(b,c) where a,b,c are from different term categories
		for (Map.Entry<String, List<String>> A : terms.entrySet()) {
			for (String a : A.getValue()) {
				for (Map.Entry<String, List<String>> B : terms.entrySet()) {
					if (A.getKey().equals(B.getKey())) continue; // same category
					for (String b : B.getValue()) {
						for (Map.Entry<String, List<String>> C : terms.entrySet()) {
							if (A.getKey().equals(C.getKey())) continue; // same category
							if (B.getKey().equals(C.getKey())) continue; // same category;
							for (String c : C.getValue()) {
								final int p = 1 + validPairs.indexOf(new Pair(a,b));
								final int q = 1 + validPairs.indexOf(new Pair(a,c));
								final int r = 1 + validPairs.indexOf(new Pair(b,c));
								solver.addClause(new VecInt(new int[] { -p, -q, r }));
							}
						}
					}

				}
			}
		}

		// the clauses from the problem
		for (Clause clause : clauses) {
			solver.addClause(new VecInt(new int[] { clause.getFactor() * (1 + validPairs.indexOf(clause.getPair())) }));
		}

		if (solver.isSatisfiable()) {
			final int[] model = solver.findModel();
			for (int i : model) {
				if (i > 0) {
					solutionPairs.add(validPairs.get(i - 1));
				}
			}
		}
	}
	
	public void write(JsonGenerator g) throws IOException {
		g.writeStartObject();
		g.writeObjectFieldStart("dimensions");
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
		g.writeArrayFieldStart("pairs");
		for (Pair pair : solutionPairs) {
			g.writeStartArray();
			g.writeString(pair.getA());
			g.writeString(pair.getB());
			g.writeEndArray();
		}
		g.writeEndArray();
		g.writeEndObject();
		g.close();
	}
	
	private VecInt toVecInt(List<Pair> l, List<Pair> validPairs) {
		final int[] result = new int[l.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = 1 + validPairs.indexOf(l.get(i));
		}
		return new VecInt(result);
	}

	public Set<Pair> getSolutionPairs() {
		return solutionPairs;
	}

	public List<List<String>> getTuples() {
		final List<List<String>> result = new ArrayList<>();
		// TODO
		return result;
	}
}
