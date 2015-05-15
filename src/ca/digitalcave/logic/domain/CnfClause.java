package ca.digitalcave.logic.domain;

import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.sat4j.core.VecInt;


public class CnfClause {
	private final ArrayList<Object> variables = new ArrayList<Object>();
	
	public CnfClause(JsonParser p) throws IOException {
		boolean not = false;
		String a = null;
		String b = null;
		while (p.nextToken() != JsonToken.END_ARRAY) {
			if (p.getCurrentToken() == JsonToken.START_OBJECT) {
				a = null;
				b = null;
			}
			else if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
				a = p.getCurrentName();
			}
			else if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
				b = p.getText();
			}
			else if (p.getCurrentToken() == JsonToken.VALUE_TRUE) {
				not = true;
			}
			else if (p.getCurrentToken() == JsonToken.VALUE_FALSE) {
				not = false;
			}
			else if (p.getCurrentToken() == JsonToken.END_OBJECT) {
				if (a != null && b != null) { // b will be null when there is a not:true in the object
					final Pair pair = new Pair(a,b);
					variables.add(not ? new Not(pair) : pair);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (Object v : variables) {
			if (sb.length() > 0) sb.append("\u22C1");
			sb.append(v);
		}
		return sb.toString();
	}
	
	public VecInt toVecInt(ArrayList<Pair> pairs) {
		final int[] result = new int[variables.size()];
		for (int i = 0; i < result.length; i++) {
			final Object v = variables.get(i);
			final Pair p;
			final int factor;
			if (v instanceof Not) {
				p = ((Not) v).getPair();
				factor = -1;
			} else {
				p = (Pair) v;
				factor = 1;
			}
			result[i] = factor * (1 + pairs.indexOf(p));
		}
		return new VecInt(result);
	}
}
