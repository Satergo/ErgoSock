package com.satergo.ergonnection.modifiers.data;

import com.satergo.ergonnection.VLQOutputStream;
import sigmastate.utils.SigmaByteReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

public record Input(byte[] id, ProverResult spendingProof) {

	public static Input deserialize(SigmaByteReader sbr) throws IOException {
		byte[] id = sbr.getBytes(32);
		byte[] proofBytes = sbr.getBytes(sbr.getUShort());
		ContextExtension contextExtension = ContextExtension.deserialize(sbr);
		return new Input(id, new ProverResult(proofBytes, contextExtension));
	}

	public void serialize(VLQOutputStream out) throws IOException {
		out.write(id);
		out.writeUnsignedShort(spendingProof.proof().length);
		out.write(spendingProof.proof());
		spendingProof.extension().serialize(out);
	}



	@Override
	public String toString() {
		return "Input[" +
				"id=" + HexFormat.of().formatHex(id) +
				", spendingProof=" + spendingProof +
				']';
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Input i && Arrays.equals(id, i.id) && Objects.equals(spendingProof, i.spendingProof);
	}

	@Override
	public int hashCode() {
		return Objects.hash(Arrays.hashCode(id), spendingProof);
	}
}
