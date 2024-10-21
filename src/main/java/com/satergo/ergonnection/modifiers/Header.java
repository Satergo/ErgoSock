package com.satergo.ergonnection.modifiers;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.modifiers.data.AutolykosSolution;
import com.satergo.ergonnection.protocol.ProtocolModifier;
import org.bouncycastle.jcajce.provider.digest.Blake2b.Blake2b256;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public record Header(byte[] id, byte version, byte[] parentId, byte[] adProofsRoot, byte[] stateRoot, byte[] transactionsRoot,
					 long timestamp, long nBits, int height, byte[] extensionHash, byte[] votes, byte[] unparsedBytes, AutolykosSolution powSolution, byte[] object) implements ProtocolModifier {

	public static final int TYPE_ID = 101;

	public static Header deserialize(byte[] id, byte[] data) throws IOException {
		if (id == null) {
			id = new Blake2b256().digest(data);
		}
		VLQInputStream in = new VLQInputStream(new ByteArrayInputStream(data));
		byte version = in.readByte();
		byte[] parentId = in.readNFully(32);
		byte[] adProofsRoot = in.readNFully(32);
		byte[] transactionsRoot = in.readNFully(32);
		byte[] stateRoot = in.readNFully(33);
		long timestamp = in.readUnsignedLong();
		byte[] extensionHash = in.readNFully(32);
		byte[] nBitsBytes = in.readNFully(4);
		long nBits = ((nBitsBytes[0] & 0xFFL) << 24) | ((nBitsBytes[1] & 0xFFL) << 16) | ((nBitsBytes[2] & 0xFFL) << 8) | (nBitsBytes[3] & 0xFFL);
		int height = Math.toIntExact(in.readUnsignedInt());
		byte[] votes = in.readNFully(3);
		byte[] unparsedBytes;
		if (version > INITIAL_VERSION) {
			int newFieldsLength = in.readUnsignedByte();
			if (newFieldsLength > 0 && version > INTERPRETER_6_0_VERSION) {
				unparsedBytes = in.readNFully(newFieldsLength);
			} else unparsedBytes = new byte[0];
		} else unparsedBytes = new byte[0];
		AutolykosSolution powSolution = AutolykosSolution.deserialize(in, version);
		return new Header(id, version, parentId, adProofsRoot, stateRoot, transactionsRoot, timestamp, nBits, height, extensionHash, votes, unparsedBytes, powSolution, data);
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		out.write(version);
		out.write(parentId);
		out.write(adProofsRoot);
		out.write(transactionsRoot);
		out.write(stateRoot);
		out.writeUnsignedLong(timestamp);
		out.write(extensionHash);
		byte[] nBitsBytes = new byte[4];
		nBitsBytes[0] = (byte)((nBits >> 24) & 0xFF);
		nBitsBytes[1] = (byte)((nBits >> 16) & 0xFF);
		nBitsBytes[2] = (byte)((nBits >> 8) & 0xFF);
		nBitsBytes[3] = (byte)(nBits & 0xFF);
		out.write(nBitsBytes);
		out.writeUnsignedInt(height);
		out.write(votes);
		if (version > INITIAL_VERSION) {
			out.write(unparsedBytes.length);
			out.write(unparsedBytes);
		}
		powSolution.serialize(out);
	}

	/**
	 * Block version at mainnet launch
	 */
	public static final byte INITIAL_VERSION = 1;
	/**
	 * Block version after the Hardening hard-fork
	 * Autolykos v2 PoW, witnesses in transactions Merkle tree
	 */
	public static final byte HARDENING_VERSION = 2;
	/**
	 * Block version after the 5.0 soft-fork
	 * 5.0 interpreter with JITC, monotonic height rule (EIP-39)
	 */
	public static final byte INTERPRETER_5_0_VERSION = 3;
	/**
	 * Block version after the 6.0 soft-fork
	 * 6.0 interpreter (EIP-50)
	 */
	public static final byte INTERPRETER_6_0_VERSION = 3;

	@Override
	public int typeId() {
		return TYPE_ID;
	}



	@Override
	public boolean equals(Object obj) {
		return obj instanceof Header h && Arrays.equals(id, h.id) && version == h.version && Arrays.equals(parentId, h.parentId)
				&& Arrays.equals(adProofsRoot, h.adProofsRoot) && Arrays.equals(stateRoot, h.stateRoot) && Arrays.equals(transactionsRoot, h.transactionsRoot)
				&& timestamp == h.timestamp && nBits == h.nBits && height == h.height && Arrays.equals(extensionHash, h.extensionHash) && Arrays.equals(votes, h.votes)
				&& Arrays.equals(unparsedBytes, h.unparsedBytes) && powSolution.equals(h.powSolution);
	}

	@Override
	public int hashCode() {
		return Objects.hash(Arrays.hashCode(id), version, Arrays.hashCode(parentId), Arrays.hashCode(adProofsRoot), Arrays.hashCode(stateRoot), Arrays.hashCode(transactionsRoot),
				timestamp, nBits, height, Arrays.hashCode(extensionHash), Arrays.hashCode(votes), Arrays.hashCode(unparsedBytes), powSolution);
	}
}
