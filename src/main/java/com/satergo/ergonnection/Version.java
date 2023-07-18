package com.satergo.ergonnection;

import java.util.Comparator;

/**
 * The JDK's {@link Runtime.Version} exists, but it disallows 0 as the third field of the version (i.e. 4.0.0 is invalid according to it)
 */
public record Version(int major, int minor, int patch) implements Comparable<Version> {

	public static Version parse(String s) {
		String[] p = s.split("\\.");
		if (p.length != 3) throw new IllegalArgumentException();
		return new Version(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
	}

	@Override
	public String toString() {
		return major + "." + minor + "." + patch;
	}


	private static final Comparator<Version> COMPARATOR = Comparator.comparingInt(Version::major)
			.thenComparingInt(Version::minor)
			.thenComparingInt(Version::patch);

	@Override
	public int compareTo(Version o) {
		return COMPARATOR.compare(this, o);
	}
}
