package com.satergo.ergonnection.modifiers.data;

import com.satergo.ergonnection.VLQWriter;
import sigmastate.utils.SigmaByteReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HexFormat;

public record Input(byte[] id, ProverResult spendingProof) {

	public static Input deserialize(SigmaByteReader sbr) throws IOException {
		byte[] id = sbr.getBytes(32);
		byte[] proofBytes = sbr.getBytes(sbr.getUShort());
		ContextExtension contextExtension = ContextExtension.deserialize(sbr);
		return new Input(id, new ProverResult(proofBytes, contextExtension));
	}

	@Override
	public String toString() {
		return "Input[" +
				"id=" + HexFormat.of().formatHex(id) +
				", spendingProof=" + spendingProof +
				']';
	}

	public void serialize(DataOutputStream out) throws IOException {
		out.write(id);
		VLQWriter.writeUShort(out, spendingProof.proof().length);
		out.write(spendingProof.proof());
		spendingProof.extension().serialize(out);
	}
}
