package com.satergo.ergonnection.modifiers.data;

import com.satergo.ergonnection.VLQOutputStream;
import sigma.ast.SType;
import sigma.ast.Value;
import sigma.serialization.SigmaByteReader;
import sigma.serialization.SigmaByteWriter;
import sigma.serialization.SigmaSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public record ContextExtension(HashMap<Byte, Value<SType>> map) {

	public static ContextExtension deserialize(SigmaByteReader sbr) {
		byte size = sbr.getByte();
		if (size < 0) throw new IllegalArgumentException();
		HashMap<Byte, Value<SType>> map = new HashMap<>();
		for (int i = 0; i < size; i++) {
			byte b = sbr.getByte();
			map.put(b, sbr.getValue());
		}
		return new ContextExtension(map);
	}

	public void serialize(VLQOutputStream out) throws IOException {
		out.write(map.size());
		SigmaByteWriter sbw = SigmaSerializer.startWriter();
		for (Map.Entry<Byte, Value<SType>> entry : map.entrySet()) {
			sbw.put(entry.getKey());
			sbw.putValue(entry.getValue());
		}
		out.write(sbw.toBytes());
	}
}
