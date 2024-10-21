package com.satergo.ergonnection.messages;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.protocol.ProtocolMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record SyncInfoOld(List<byte[]> lastHeaderIDs) implements ProtocolMessage {

	public static final int CODE = 65;

	public static SyncInfoOld deserialize(VLQInputStream in) throws IOException {
		int count = in.readUnsignedShort();
		ArrayList<byte[]> lastHeaderIDs = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			lastHeaderIDs.add(in.readNFully(32));
		}
		return new SyncInfoOld(Collections.unmodifiableList(lastHeaderIDs));
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		out.writeUnsignedShort(lastHeaderIDs.size());
		for (byte[] lastHeaderID : lastHeaderIDs) {
			out.write(lastHeaderID);
		}
	}

	@Override public int code() { return CODE; }



	@Override
	public boolean equals(Object obj) {
		return obj instanceof SyncInfoOld s && InternalUtils.byteArrayListEquals(lastHeaderIDs, s.lastHeaderIDs);
	}

	@Override
	public int hashCode() {
		return InternalUtils.byteArrayListHashCode(lastHeaderIDs);
	}
}
