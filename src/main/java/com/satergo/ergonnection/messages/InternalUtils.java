package com.satergo.ergonnection.messages;

import java.util.Arrays;
import java.util.List;

class InternalUtils {

	static final byte[] EMPTY = new byte[0];

	static int byteArrayListHashCode(List<byte[]> list) {
		return Arrays.hashCode(list.stream().mapToInt(Arrays::hashCode).toArray());
	}

	static boolean byteArrayListEquals(List<byte[]> a, List<byte[]> b) {
		if (a.size() != b.size())
			return false;
		for (int i = 0; i < a.size(); i++) {
			if (!Arrays.equals(a.get(i), b.get(i)))
				return false;
		}
		return true;
	}
}
