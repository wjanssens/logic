package ca.digitalcave.logic.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

public class Puzzle {

	public static void main(String[] args) throws Exception {
		final JsonParser p = new JsonFactory().createJsonParser(Puzzle.class.getResourceAsStream("test.json"));
		final Puzzle puzzle = new Puzzle(p);
		puzzle.solve();
	}
	
	private final HashMap<String, List<String>> terms = new HashMap<String, List<String>>();
	private final LinkedList<CnfClause> clauses = new LinkedList<CnfClause>();
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
			else if ("cnf".equals(p.getCurrentName())) {
				while (p.nextToken() != JsonToken.END_ARRAY) {
					clauses.add(new CnfClause(p));
				}
			}
		}
		
		// create a List of all valid pairs;
		for (Map.Entry<String, List<String>> A : terms.entrySet()) {
			for (Map.Entry<String, List<String>> B : terms.entrySet()) {
				if (A.getKey().equals(B.getKey())) continue; // same category
				for (String a : A.getValue()) {
					for (String b : B.getValue()) {
						if (a.equals(b)) continue; // same term
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
		for (Map.Entry<String, List<String>> A : terms.entrySet()) {
			for (String a : A.getValue()) {
				for (Map.Entry<String, List<String>> B : terms.entrySet()) {
					if (A.getKey().equals(B.getKey())) continue; // same category
					final ArrayList<Pair> pairs = new ArrayList<Pair>();
					for (String b : B.getValue()) {
						pairs.add(new Pair(a,b));
					}
					solver.addExactly(toVecInt(pairs), 1);
				}
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
		for (CnfClause clause : clauses) {
			solver.addClause(clause.toVecInt(pairs));
//			System.out.println(clause);
		}

//		System.out.println("----");
		
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
//		System.out.println(l);
		final int[] result = new int[l.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = 1+pairs.indexOf(l.get(i));
		}
		return new VecInt(result);
	}
}
