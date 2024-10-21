package com.satergo.ergonnection.messages;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.protocol.Protocol;
import com.satergo.ergonnection.protocol.ProtocolMessage;
import com.satergo.ergonnection.protocol.ProtocolModifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ModifierResponse(int typeId, List<ProtocolModifier> modifiers) implements ProtocolMessage {

	private record UnknownModifier(int typeId, byte[] id, byte[] data) implements ProtocolModifier {

		public UnknownModifier {
			if (id.length != 32) throw new IllegalArgumentException("id must be of length 32");
		}

		@Override
		public void serialize(VLQOutputStream out) throws IOException {
			out.write(data);
		}

		@Override
		public byte[] toByteArray() {
			return data;
		}
	}

	public static final int CODE = 33;

	public static ModifierResponse deserialize(VLQInputStream in) throws IOException {
		int typeId = in.readByte();
		int count = (int) in.readUnsignedInt();
		ArrayList<ProtocolModifier> modifiers = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			byte[] id = in.readNFully(32);
			int dataLength = Math.toIntExact(in.readUnsignedInt());
			byte[] data = in.readNFully(dataLength);
			ProtocolModifier modifier;
			try {
				modifier = Protocol.deserializeModifier(typeId, id, data);
			} catch (UnsupportedOperationException e) {
				modifier = new UnknownModifier(typeId, id, data);
			}
			modifiers.add(modifier);
		}
		return new ModifierResponse(typeId, Collections.unmodifiableList(modifiers));
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		out.write(typeId);
		out.writeUnsignedInt(modifiers.size());
		for (ProtocolModifier modifier : modifiers) {
			out.write(modifier.id());
			byte[] data = modifier.toByteArray();
			out.writeUnsignedInt(data.length);
			out.write(data);
		}
	}

	@Override public int code() { return CODE; }
}
