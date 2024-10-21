package com.satergo.ergonnection.messages;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.protocol.ProtocolMessage;

public record GetPeers() implements ProtocolMessage {

	public static final int CODE = 1;

	public static GetPeers deserialize(VLQInputStream in) {
		return new GetPeers();
	}

	@Override public void serialize(VLQOutputStream out) {}
	@Override public byte[] toByteArray() { return InternalUtils.EMPTY; }

	@Override public int code() { return CODE; }
}
