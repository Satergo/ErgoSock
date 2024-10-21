package com.satergo.ergonnection.modifiers.data;

import com.satergo.ergonnection.VLQOutputStream;
import org.ergoplatform.ErgoBox;
import org.jspecify.annotations.Nullable;
import sigmastate.SType;
import sigmastate.Values;
import sigmastate.serialization.ErgoTreeSerializer;
import sigmastate.serialization.SigmaSerializer;
import sigmastate.utils.SigmaByteReader;
import sigmastate.utils.SigmaByteWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ErgoBoxCandidate(long value,
							   Values.ErgoTree ergoTree,
							   int creationHeight,
							   Map<TokenId, Long> tokens,
							   LinkedHashMap<ErgoBox.NonMandatoryRegisterId, Values.EvaluatedValue<SType>> additionalRegisters) implements ErgoBoxAssets {

	public static ErgoBoxCandidate parseBodyWithIndexedDigests(@Nullable List<TokenId> digestsInTx, SigmaByteReader sbr) {
		long value = sbr.getULong();

		Values.ErgoTree tree = ErgoTreeSerializer.DefaultSerializer().deserializeErgoTree(
				sbr, SigmaSerializer.MaxPropositionSize(), false);

		int creationHeight = sbr.getUIntExact();

		int tokenCount = sbr.getUByte();
		LinkedHashMap<TokenId, Long> tokens = new LinkedHashMap<>();
		if (digestsInTx != null) {
			for (int i = 0; i < tokenCount; i++) {
				int digestIndex = sbr.getUIntExact();
				if (digestIndex < 0 || digestIndex >= digestsInTx.size()) {
					throw new RuntimeException("failed to find token id with index " + digestIndex);
				}
				long amount = sbr.getULong();
				tokens.put(digestsInTx.get(digestIndex), amount);
			}
		} else {
			for (int i = 0; i < tokenCount; i++) {
				tokens.put(new TokenId(sbr.getBytes(32)), sbr.getULong());
			}
		}
		int registerCount = sbr.getUByte();
		LinkedHashMap<ErgoBox.NonMandatoryRegisterId, Values.EvaluatedValue<SType>> registers = new LinkedHashMap<>();
		for (int i = 0; i < registerCount; i++) {
			Values.EvaluatedValue<SType> constant = (Values.EvaluatedValue<SType>) sbr.getValue();
			registers.put(ErgoBox.nonMandatoryRegisters().apply(i), constant);
		}
		return new ErgoBoxCandidate(value, tree, creationHeight, tokens, registers);
	}

	public void serializeWithIndexedDigests(VLQOutputStream out, @Nullable List<TokenId> tokensInTx) throws IOException {
		out.writeUnsignedLong(value);

		out.write(ErgoTreeSerializer.DefaultSerializer().serializeErgoTree(ergoTree));

		out.writeUnsignedInt(creationHeight);

		out.write(tokens.size());
		if (tokensInTx != null) {
			for (Map.Entry<TokenId, Long> entry : tokens.entrySet()) {
				out.writeUnsignedInt(tokensInTx.indexOf(entry.getKey()));
				out.writeUnsignedLong(entry.getValue());
			}
		} else {
			for (Map.Entry<TokenId, Long> entry : tokens.entrySet()) {
				out.write(entry.getKey().id());
				out.writeUnsignedLong(entry.getValue());
			}
		}

		out.write(additionalRegisters.size());
		SigmaByteWriter sbw = SigmaSerializer.startWriter();
		for (var entry : additionalRegisters.entrySet()) {
			sbw.putUByte(entry.getKey().asIndex());
			sbw.putValue(entry.getValue());
		}
		out.write(sbw.toBytes());
	}

	public void serializeWithoutIndexedDigests(VLQOutputStream out) throws IOException {
		serializeWithIndexedDigests(out, null);
	}
}
