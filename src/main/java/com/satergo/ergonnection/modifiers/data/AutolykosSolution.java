package com.satergo.ergonnection.modifiers.data;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import sigmastate.basics.CryptoConstants;
import sigmastate.crypto.BigIntegers;
import sigmastate.crypto.Platform;
import sigmastate.serialization.GroupElementSerializer;
import sigmastate.serialization.SigmaSerializer;
import sigmastate.utils.SigmaByteWriter;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

public record AutolykosSolution(Platform.Ecp minerPubKey, Platform.Ecp oneTimePubKey, byte[] nonce, BigInteger distance) {

	private static final int PUBLIC_KEY_LENGTH = 33;

	public static AutolykosSolution deserialize(VLQInputStream in, byte version) throws IOException {
		Platform.Ecp minerPublicKey = groupElemFromBytes(in.readNFully(PUBLIC_KEY_LENGTH));
		if (version == 1) {
			Platform.Ecp w = groupElemFromBytes(in.readNFully(PUBLIC_KEY_LENGTH));
			byte[] nonce = in.readNFully(8);
			int dBytesLength = in.readUnsignedByte();
			BigInteger d = BigIntegers.fromUnsignedByteArray(in.readNFully(dBytesLength));
			return new AutolykosSolution(minerPublicKey, w, nonce, d);
		} else {
			byte[] nonce = in.readNFully(8);
			return new AutolykosSolution(minerPublicKey, CryptoConstants.dlogGroup().generator(), nonce, BigInteger.ZERO);
		}
	}

	public void serialize(VLQOutputStream out) throws IOException {
		SigmaByteWriter sbw = SigmaSerializer.startWriter();
		GroupElementSerializer.serialize(minerPubKey, sbw);
		GroupElementSerializer.serialize(oneTimePubKey, sbw);
		out.write(sbw.toBytes());

		out.write(nonce);
		byte[] dBytes = BigIntegers.asUnsignedByteArray(distance);
		out.write(dBytes.length);
		out.write(dBytes);
	}

	private static Platform.Ecp groupElemFromBytes(byte[] bytes) {
		return GroupElementSerializer.parse(SigmaSerializer.startReader(bytes, 0));
	}



	@Override
	public boolean equals(Object obj) {
		return obj instanceof AutolykosSolution a && Objects.equals(minerPubKey, a.minerPubKey)
				&& Objects.equals(oneTimePubKey, a.oneTimePubKey) && Arrays.equals(nonce, a.nonce) && Objects.equals(distance, a.distance);
	}

	@Override
	public int hashCode() {
		return Objects.hash(minerPubKey, oneTimePubKey, Arrays.hashCode(nonce), distance);
	}
}
