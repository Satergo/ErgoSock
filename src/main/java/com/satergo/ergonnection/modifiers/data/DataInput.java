package com.satergo.ergonnection.modifiers.data;

import java.util.Arrays;

/**
 * Inputs that are used to enrich script context, but won't be spent by the transaction
 */
public record DataInput(byte[] boxId) {

	public DataInput {
		if (boxId.length != 32) throw new IllegalArgumentException("must be length 32");
	}



	@Override
	public boolean equals(Object obj) {
		return obj instanceof DataInput d && Arrays.equals(boxId, d.boxId);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(boxId);
	}
}
