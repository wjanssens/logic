package ca.digitalcave.logic.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

public class Puzzle {

	public static void main(String[] args) throws Exception {
		final JsonParser p = new JsonFactory().createJsonParser(Puzzle.class.getResourceAsStream("test2.json"));
		final Puzzle puzzle = new Puzzle(p);
		puzzle.solve();
	}
	
	private final HashMap<String, List<String>> terms = new HashMap<String, List<String>>();
	private final LinkedList<Clause> clauses = new LinkedList<Clause>();
	private final ArrayList<Pair> pairs = new ArrayList<Pair>();

	public Puzzle(JsonParser p) throws IOException {
		while (p.nextToken() != JsonToken.END_OBJECT) {
			if ("problem".equals(p.getCurrentName())) {
				LinkedList<String> list = new LinkedList<String>();
				while (p.nextToken() != JsonToken.END_OBJECT) {
					if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
						list = new LinkedList<String>();
						terms.put(p.getCurrentName(), list);
					}
					else if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
						list.add(p.getText());
					}
				}
			}
			else if ("clauses".equals(p.getCurrentName())) {
				boolean truth = false;
				final Stack<String> stack = new Stack<String>();
				while (p.nextToken() != JsonToken.END_ARRAY) {
					if ("pair".equals(p.getCurrentName())) {
						while (p.nextToken() != JsonToken.END_ARRAY) {
							if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
								stack.push(p.getText());
							} 
						}
					}
					else if (p.getCurrentToken() == JsonToken.VALUE_TRUE) {
						truth = true;
					}
					else if (p.getCurrentToken() == JsonToken.VALUE_FALSE) {
						truth = false;
					}
					else if (p.getCurrentToken() == JsonToken.END_OBJECT) {
						clauses.push(new Clause(new Pair(stack.pop(), stack.pop()), truth));
					}
				}
			}
		}
		
		// create a List of all valid pairs;
		for (Map.Entry<String, List<String>> e : terms.entrySet()) {
			for (Map.Entry<String, List<String>> f : terms.entrySet()) {
				if (e.getKey().equals(f.getKey())) continue;
				for (String a : e.getValue()) {
					for (String b : f.getValue()) {
						if (a.equals(b)) continue;
						final Pair pair = new Pair(a, b);
						if (pairs.contains(pair)) continue;
						pairs.add(pair);
					}
				}
			}
		}
	}
	
	public void solve() throws Exception {
		final ISolver solver = SolverFactory.newDefault();
		solver.newVar(pairs.size());
//		solver.setExpectedNumberOfClauses(clauses.size());
		
		// the clauses from the valid pair combinations
		for (Map.Entry<String, List<String>> e : terms.entrySet()) {
			for (String a : e.getValue()) {
//				final ArrayList<Pair> d = new ArrayList<Pair>();
				for (Map.Entry<String, List<String>> f : terms.entrySet()) {
					if (e.getKey().equals(f.getKey())) continue; // same category
					final ArrayList<Pair> c = new ArrayList<Pair>();
					for (int i = 0; i < f.getValue().size(); i++) {
						final String b = f.getValue().get(i);
						final Pair p = new Pair(a,b);
						c.add(p);
//						d.add(p);
					}
					solver.addExactly(toVecInt(c), 1);
				}
//				solver.addExactly(toVecInt(d), terms.size() - 1);
				System.out.println("----");
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
								final int p = 1 + pairs.indexOf(new Pair(a,b));
								final int q = 1 + pairs.indexOf(new Pair(a,c));
								final int r = 1 + pairs.indexOf(new Pair(b,c));
								solver.addClause(new VecInt(new int[] { -p, -q, r }));
							}
						}
					}
					
	 			}
			}
		}
		
		// the clauses from the problem
		for (Clause clause : clauses) {
			solver.addClause(new VecInt(new int[] { clause.getFactor() * (1+pairs.indexOf(clause.getPair())) }));
			System.out.println(clause);
		}

		System.out.println("----");
		
		final IProblem problem = solver;
		if (problem.isSatisfiable()) {
			final int[] model = problem.findModel();
			for (int i : model) {
				if (i > 0) {
					System.out.println(pairs.get(i-1));
				}
				else {
//					System.out.println("-" + pairs.get((i*-1)-1));
				}
			}
		} else {
			System.out.println("not solvable");
		}
	}
	
	private VecInt toVecInt(List<Pair> l) {
		System.out.println(l);
		final int[] result = new int[l.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = 1+pairs.indexOf(l.get(i));
		}
		return new VecInt(result);
	}
}
