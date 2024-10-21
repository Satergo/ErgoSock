package com.satergo.ergonnection.protocol;

import com.satergo.ergonnection.VLQOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface ProtocolModifier {

	void serialize(VLQOutputStream out) throws IOException;

	int typeId();

	byte[] id();

	default byte[] toByteArray() throws IOException {
		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			 VLQOutputStream out = new VLQOutputStream(bytes)) {
			serialize(out);
			out.flush();
			return bytes.toByteArray();
		}
	}
}
