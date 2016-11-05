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
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Puzzle {
	private static final Pattern PATTERN = Pattern.compile("(.+)(==|<>|!=)(.+)");
	private final HashMap<String, List<String>> terms = new HashMap<>();
	private final LinkedList<Clause> clauses = new LinkedList<>();
	private final HashSet<Pair> solutionPairs = new HashSet<>();
	private final ArrayList<Pair> validPairs = new ArrayList<>();
	private final ISolver solver = SolverFactory.newDefault();

	public Puzzle(JsonParser p) throws IOException, ContradictionException {

		final LinkedList<String> stack = new LinkedList<>();

		while (p.nextToken() != JsonToken.END_OBJECT && p.hasCurrentToken()) {
			if ("dimensions".equals(p.getCurrentName())) {
				LinkedList<String> list = new LinkedList<>();
				while (p.nextToken() != JsonToken.END_OBJECT) {
					if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
						list = new LinkedList<>();
						terms.put(p.getCurrentName(), list);
					} else if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
						list.add(p.getText());
					}
				}

				// now that we've seen all of the dimensions an their members
				// create an array of all valid pair combination
				// in this way each valid pair will have a unique index
				for (Map.Entry<String, List<String>> e : terms.entrySet()) {
					for (Map.Entry<String, List<String>> f : terms.entrySet()) {
						if (e.getKey().equals(f.getKey())) continue; // terms from the same group cannot be valid solutionPairs
						for (String a : e.getValue()) {
							for (String b : f.getValue()) {
								if (a.equals(b)) {
									continue; // ignore self combinations (i.e. Monday == Monday)
								}
								final Pair pair = new Pair(a, b);
								if (validPairs.contains(pair)) {
									continue; // ignore reflexive combinations (i.e. Monday == Jodie and Jodie == Monday)
								}
								validPairs.add(pair);
							}
						}
					}
				}

				solver.newVar(validPairs.size());
				solver.setTimeoutMs(1000);

				// add the the valid pair combinations to the solver
				for (Map.Entry<String, List<String>> e : terms.entrySet()) {
					for (String a : e.getValue()) {
						final ArrayList<Pair> d = new ArrayList<>();
						for (Map.Entry<String, List<String>> f : terms.entrySet()) {
							if (e.getKey().equals(f.getKey())) continue; // same category
							final ArrayList<Pair> c = new ArrayList<>();
							for (int i = 0; i < f.getValue().size(); i++) {
								final String b = f.getValue().get(i);
								final Pair pair = new Pair(a,b);
								c.add(pair);
								d.add(pair);
							}
							solver.addExactly(toVecInt(c), 1);
						}
						solver.addExactly(toVecInt(d), terms.size() - 1);
					}
				}
			} else if ("cnf".equals(p.getCurrentName())) {
				final ArrayList<Integer> indices = new ArrayList<>();
				p.nextToken();
				while (p.nextToken() != JsonToken.END_ARRAY) {
					switch (p.getCurrentToken()) {
						case VALUE_STRING:
							solver.addClause(new VecInt(new int[] { tokenize(p.getText()) }));
							break;
						case START_ARRAY:
							indices.clear();
							while (p.nextToken() != JsonToken.END_ARRAY) {
								if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
									indices.add(tokenize(p.getText()));
								}
							}
							solver.addClause(new VecInt(indices.stream().mapToInt(i -> i).toArray()));
							break;
					}
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

	private int tokenize(String string) {
		final Matcher matcher = PATTERN.matcher(string);
		if (matcher.matches()) {
			final String a = matcher.group(1).trim();
			final String b = matcher.group(3).trim();
			final String eq = matcher.group(2);

			final Pair pair = new Pair(a, b);
			final int index = validPairs.indexOf(pair) + 1;
			final int factor = "==".equals(eq) ? 1 : -1;

			if (index == 0) {
				throw new IllegalStateException("Invalid pair " + pair);
			}
			return factor * index;
		} else {
			throw new IllegalStateException("Can't parse expression " + string);
		}
	}
	
	public void solve() throws ContradictionException, TimeoutException {
		solutionPairs.clear();

		// TODO try to document this a bit better
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
	
	private VecInt toVecInt(List<Pair> l) {
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
