package ca.digitalcave.scheduler.util;

public class PasswordUtil {

	public static String deobfuscate(String s) {
		if (s.startsWith("OBF:"))
			s = s.substring(4);

		final byte[] b = new byte[s.length()/2];
		int l = 0;
		for (int i = 0; i < s.length(); i += 4) {
			final String x = s.substring(i, i+4);
			final int i0 = Integer.parseInt(x, 36);
			final int i1 = (i0 / 256);
			final int i2 = (i0 % 256);
			b[l++] = (byte) ((i1 + i2 - 254) / 2);
		}

		return new String(b, 0, l);
	}
}
