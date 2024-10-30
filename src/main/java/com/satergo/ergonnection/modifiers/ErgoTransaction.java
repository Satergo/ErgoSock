package com.satergo.ergonnection.modifiers;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.modifiers.data.DataInput;
import com.satergo.ergonnection.modifiers.data.ErgoBoxCandidate;
import com.satergo.ergonnection.modifiers.data.Input;
import com.satergo.ergonnection.modifiers.data.TokenId;
import com.satergo.ergonnection.protocol.ProtocolModifier;
import org.jspecify.annotations.Nullable;
import sigma.serialization.SigmaByteReader;
import sigma.serialization.SigmaSerializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @param id optional
 * @param size optional
 */
public record ErgoTransaction(byte @Nullable [] id, List<Input> inputs, List<DataInput> dataInputs, List<ErgoBoxCandidate> outputCandidates, @Nullable Integer size) implements ProtocolModifier {

	public ErgoTransaction {
		if (id != null && id.length != 32) throw new IllegalArgumentException("id must be 32 bytes");
	}

	public static final int TYPE_ID = 2;

	/**
	 * @param id optional (nullable)
	 */
	public static ErgoTransaction deserialize(byte[] id, byte[] data) throws IOException {
		SigmaByteReader sbr = SigmaSerializer.startReader(data, 0);
		int inputCount = sbr.getUShort();
		ArrayList<Input> inputs = new ArrayList<>();
		for (int i = 0; i < inputCount; i++) {
			inputs.add(Input.deserialize(sbr));
		}

		int dataInputCount = sbr.getUShort();
		ArrayList<DataInput> dataInputs = new ArrayList<>();
		for (int i = 0; i < dataInputCount; i++) {
			dataInputs.add(new DataInput(sbr.getBytes(32)));
		}

		// parse distinct ids of tokens in transaction outputs
		int tokensCount = sbr.getUIntExact();
		ArrayList<TokenId> tokens = new ArrayList<>();
		for (int i = 0; i < tokensCount; i++) {
			tokens.add(new TokenId(sbr.getBytes(32)));
		}

		int outputCandidatesCount = sbr.getUShort();
		ArrayList<ErgoBoxCandidate> outputCandidates = new ArrayList<>();
		for (int i = 0; i < outputCandidatesCount; i++) {
			ErgoBoxCandidate ergoBoxCandidate = ErgoBoxCandidate.parseBodyWithIndexedDigests(tokens, sbr);
			outputCandidates.add(ergoBoxCandidate);
		}

		return new ErgoTransaction(
				id,
				Collections.unmodifiableList(inputs),
				Collections.unmodifiableList(dataInputs),
				Collections.unmodifiableList(outputCandidates),
				null);
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		out.writeUnsignedShort(inputs.size());
		for (Input input : inputs) {
			input.serialize(out);
		}
		out.writeUnsignedShort(dataInputs.size());
		for (DataInput dataInput : dataInputs) {
			out.write(dataInput.boxId());
		}
		List<TokenId> distinctTokenIds = outputCandidates.stream().flatMap(oc -> oc.tokens().keySet().stream()).distinct().toList();
		out.writeUnsignedInt(distinctTokenIds.size());
		for (TokenId distinctTokenId : distinctTokenIds) {
			out.write(distinctTokenId.id());
		}
		out.writeUnsignedShort(outputCandidates.size());
		for (ErgoBoxCandidate outputCandidate : outputCandidates) {
			outputCandidate.serializeWithIndexedDigests(out, distinctTokenIds);
		}
	}

	@Override public int typeId() { return TYPE_ID; }



	@Override
	public boolean equals(Object obj) {
		return obj instanceof ErgoTransaction e && Arrays.equals(id, e.id) && inputs.equals(e.inputs)
				&& dataInputs.equals(e.dataInputs) && outputCandidates.equals(e.outputCandidates) && Objects.equals(size, e.size);
	}

	@Override
	public int hashCode() {
		return Objects.hash(Arrays.hashCode(id), inputs, dataInputs, outputCandidates, size);
	}
}
