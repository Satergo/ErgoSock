package com.satergo.ergonnection.modifiers.data;

import java.util.Arrays;
import java.util.Objects;

public record ProverResult(byte[] proof, ContextExtension extension) {

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ProverResult p && Arrays.equals(proof, p.proof) && Objects.equals(extension, p.extension);
	}

	@Override
	public int hashCode() {
		return Objects.hash(Arrays.hashCode(proof), extension);
	}
}
