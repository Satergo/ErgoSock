package com.satergo.ergonnection.modifiers.data;

import java.util.Arrays;
import java.util.HexFormat;

public record TokenId(byte[] id) {

	public TokenId {
		if (id.length != 32) throw new IllegalArgumentException("id must be 32 bytes");
	}

	@Override
	public String toString() {
		return HexFormat.of().formatHex(id);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TokenId tokenId && Arrays.equals(id, tokenId.id);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(id);
	}
}
