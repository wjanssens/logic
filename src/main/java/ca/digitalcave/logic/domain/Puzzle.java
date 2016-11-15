package ca.digitalcave.logic.domain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fasterxml.jackson.core.JsonToken.*;

public class Puzzle {
	private static final Pattern PATTERN = Pattern.compile("(.+)(==|<>|!=)(.+)");
	private String name;
	private String description;
	private final LinkedList<String> instructions = new LinkedList<>();
	private final LinkedHashMap<String, List<String>> dimensions = new LinkedHashMap<>();
	private final HashSet<Pair> solutionPairs = new HashSet<>();
	private final HashSet<Pair> expectedPairs = new HashSet<>();
	private final List<List<String>> solutionTuples = new ArrayList<>();
	private final List<List<String>> expectedTuples = new ArrayList<>();
	private final ArrayList<Pair> validPairs = new ArrayList<>();
	private final ISolver solver = SolverFactory.newDefault();

	public Puzzle(JsonParser p) throws IOException, ContradictionException {

		final LinkedList<String> stack = new LinkedList<>();

		while (p.nextToken() != END_OBJECT && p.hasCurrentToken()) {
			if ("name".equals(p.getCurrentName())) {
				name = p.nextTextValue();
			} else if ("description".equals(p.getCurrentName())) {
				description = p.nextTextValue();
			} else if ("instructions".equals(p.getCurrentName())) {
				while (p.nextToken() != END_ARRAY) {
					if (p.getCurrentToken() == VALUE_STRING) {
						instructions.add(p.getText());
					}
				}
			} else if ("dimensions".equals(p.getCurrentName())) {
				LinkedList<String> list = new LinkedList<>();
				while (p.nextToken() != END_OBJECT) {
					if (p.getCurrentToken() == FIELD_NAME) {
						list = new LinkedList<>();
						dimensions.put(p.getCurrentName(), list);
					} else if (p.getCurrentToken() == VALUE_STRING) {
						list.add(p.getText());
					}
				}

				// now that we've seen all of the dimensions an their members
				// create an array of all valid pair combination
				// in this way each valid pair will have a unique index
				for (Map.Entry<String, List<String>> e : dimensions.entrySet()) {
					for (Map.Entry<String, List<String>> f : dimensions.entrySet()) {
						if (e.getKey().equals(f.getKey())) continue; // dimensions from the same group cannot be valid solutionPairs
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
				for (Map.Entry<String, List<String>> A : dimensions.entrySet()) {
					for (String a : A.getValue()) {
						final ArrayList<Pair> d = new ArrayList<>();
						for (Map.Entry<String, List<String>> B : dimensions.entrySet()) {
							if (A.getKey().equals(B.getKey())) continue; // same category
							final ArrayList<Pair> c = new ArrayList<>();
							for (int i = 0; i < B.getValue().size(); i++) {
								final String b = B.getValue().get(i);
								final Pair pair = new Pair(a,b);
								c.add(pair);
								d.add(pair);
							}
							solver.addExactly(toVecInt(c), 1);
						}
						solver.addExactly(toVecInt(d), dimensions.size() - 1);
					}
				}
			} else if ("cnf".equals(p.getCurrentName())) {
				final ArrayList<Integer> indices = new ArrayList<>();
				p.nextToken();
				while (p.nextToken() != END_ARRAY) {
					switch (p.getCurrentToken()) {
						case VALUE_STRING:
							solver.addClause(new VecInt(new int[] { tokenize(p.getText()) }));
							break;
						case START_ARRAY:
							indices.clear();
							while (p.nextToken() != END_ARRAY) {
								if (p.getCurrentToken() == VALUE_STRING) {
									indices.add(tokenize(p.getText()));
								}
							}
							solver.addClause(new VecInt(indices.stream().mapToInt(i -> i).toArray()));
							break;
					}
				}
			} else if ("expectedPairs".equals(p.getCurrentName()) || "pairs".equals(p.getCurrentName())) {
				while (p.nextToken() != END_ARRAY) {
					while (p.nextToken() != END_ARRAY) {
						if (p.getCurrentToken() == VALUE_STRING) {
							stack.add(p.getText());
						}
					}
					expectedPairs.add(new Pair(stack.removeFirst(), stack.removeFirst()));
					stack.clear();
				}
			} else if ("expectedTuples".equals(p.getCurrentName()) || "tuples".equals(p.getCurrentName())) {
				while (p.nextToken() != END_ARRAY) {
					final ArrayList<String> tuple = new ArrayList<>();
					while (p.nextToken() != END_ARRAY) {
						if (p.getCurrentToken() == VALUE_STRING) {
							tuple.add(p.getText());
						}
					}
					expectedTuples.add(tuple);
				}
			}
		}
	}

	public void write(JsonGenerator g) throws IOException {
		g.writeStartObject();

		g.writeStringField("name", name);
		g.writeStringField("description", description);
		g.writeArrayFieldStart("instructions");
		for (String instruction : instructions) {
			g.writeString(instruction);
		}
		g.writeEndArray(); // instructions

		g.writeObjectFieldStart("dimensions");
		for (Map.Entry<String, List<String>> dimension : dimensions.entrySet()) {
			g.writeArrayFieldStart(dimension.getKey());
			for (String term : dimension.getValue()) {
				g.writeString(term);
			}
			g.writeEndArray();
		}
		g.writeEndObject(); // dimensions

		if (!solutionPairs.isEmpty()) {
			g.writeArrayFieldStart("expectedPairs");
			for (Pair pair : expectedPairs) {
				pair.write(g);
			}
			g.writeEndArray(); // expectedPairs
		}

		if (!solutionPairs.isEmpty()) {
			g.writeArrayFieldStart("solutionPairs");
			for (Pair pair : solutionPairs) {
				pair.write(g);
			}
			g.writeEndArray(); // solutionPairs
		}

		if (!expectedTuples.isEmpty()) {
			g.writeArrayFieldStart("expectedTuples");
			for (List<String> tuple : expectedTuples) {
				g.writeStartArray();
				for (String term : tuple) {
					g.writeString(term);
				}
				g.writeEndArray();
			}
			g.writeEndArray(); // expectedTuples
		}

		if (!solutionTuples.isEmpty()) {
			g.writeArrayFieldStart("solutionTuples");
			for (List<String> tuple : solutionTuples) {
				g.writeStartArray();
				for (String term : tuple) {
					g.writeString(term);
				}
				g.writeEndArray();
			}
			g.writeEndArray(); // expectedTuples
		}

		g.writeEndObject();
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
		// This rule says that if ab and ac then bc must also be true
		// (a,b)&(a,c)=>(b,c) where a,b,c are from different dimensions
		for (Map.Entry<String, List<String>> A : dimensions.entrySet()) {
			for (String a : A.getValue()) {
				for (Map.Entry<String, List<String>> B : dimensions.entrySet()) {
					if (A.getKey().equals(B.getKey())) continue; // same category
					for (String b : B.getValue()) {
						for (Map.Entry<String, List<String>> C : dimensions.entrySet()) {
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

			// populate solution pairs
			solutionPairs.clear();
			for (int i : model) {
				if (i > 0) {
					solutionPairs.add(validPairs.get(i - 1));
				}
			}

			// populate solution tuples
			solutionTuples.clear();

			final Map.Entry<String, List<String>> A = dimensions.entrySet().iterator().next();
			solutionTuples.add(new ArrayList<>(dimensions.keySet()));

			for (String a : A.getValue()) {
				// seed the result with a set for each value in the first category
				final ArrayList<String> set = new ArrayList<>();
				set.add(a);
				solutionTuples.add(set);

				for (Map.Entry<String, List<String>> B : dimensions.entrySet()) {
					if (A.getKey().equals(B.getKey())) continue; // same category
					for (String b : B.getValue()) {
						final Pair pair = new Pair(a, b);

						if (solutionPairs.contains(pair)){
							set.add(b);
						}
					}
				}
			}
		}
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

	public List<List<String>> getSolutionTuples() {
		return solutionTuples;
	}

	public Set<Pair> getExpectedPairs() {
		return expectedPairs;
	}

	public List<List<String>> getExpectedTuples() {
		return expectedTuples;
	}
}
