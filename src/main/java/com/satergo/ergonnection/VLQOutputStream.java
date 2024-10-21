package com.satergo.ergonnection;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Variable-length_quantity">Variable-length quantity on Wikipedia</a>
 * @see <a href="https://github.com/protocolbuffers/protobuf/blob/a85bbad37905e882f89973e9fa7836ff79d02024/java/core/src/main/java/com/google/protobuf/CodedOutputStream.java">protobuf/CodedOutputStream.java</a>
 */
public class VLQOutputStream extends FilterOutputStream {
	/**
	 * Creates a VLQInputStream that uses the specified
	 * underlying InputStream.
	 *
	 * @param out the specified input stream
	 */
	public VLQOutputStream(OutputStream out) {
		super(out);
	}

	public void writeBoolean(boolean b) throws IOException {
		out.write(b ? 1 : 0);
	}

	/**
	 * Writes a signed short encoded with ZigZag and VLQ.
	 * Both negative and positive values are supported, but due to ZigZag, encoding positive
	 * values is done less efficiently than by {@link #writeUnsignedShort}.
	 * Use {@link #writeUnsignedShort} to encode values that are always positive.
	 * <p>
	 * The resulting varint uses ZigZag encoding as well, which is more efficient at
	 *       encoding negative values than pure VLQ.
	 * @param v signed int
	 */
	public void writeShort(short v) throws IOException {
		writeUnsignedLong(encodeZigZagInt(v));
	}

	/**
	 * @param v Unsigned short (0-0xFFFF) represented as int
	 * @throws IllegalArgumentException for values not in unsigned short range
	 */
	public void writeUnsignedShort(int v) throws IOException {
		if (v < 0 || v > 0xFFFF)
			throw new IllegalArgumentException(v + " is out of unsigned short range");
		writeUnsignedInt(v);
	}

	/**
	 * Writes a signed int encoded with ZigZag and VLQ.
	 * Both negative and positive values are supported, but due to ZigZag, encoding positive
	 * values is done less efficiently than by {@link #writeUnsignedInt}.
	 * Use {@link #writeUnsignedInt} to encode values that are positive.
	 * <p>
	 * The resulting varint uses ZigZag encoding as well, which is more efficient at
	 *       encoding negative values than pure VLQ.
	 */
	public void writeInt(int v) throws IOException {
		writeUnsignedLong(encodeZigZagInt(v));
	}

	/**
	 * Writes an unsigned int encoded with VLQ.
	 * Only positive values are supported. Use {@link #writeInt}
	 * to encode negative and positive values.
	 *
	 * @param v Unsigned int (0-0xFFFFFFFF) represented as long
	 * @throws IllegalArgumentException for values not in unsigned int range
	 */
	public void writeUnsignedInt(long v) throws IOException {
		if (v < 0 || v > 0xFFFFFFFFL)
			throw new IllegalArgumentException(v + " is out of unsigned int range");
		writeUnsignedLong(v);
	}

	/**
	 * Writes a signed long encoded with VLQ and ZigZag.
	 * Both negative and positive values are supported, but due to ZigZag, encoding positive
	 * values is done less efficiently than by {@link #writeUnsignedLong}.
	 * Use {@link #writeUnsignedLong} to encode values that are positive.
	 * <p>
	 * The resulting varint uses ZigZag encoding as well, which is more efficient at
	 *       encoding negative values than pure VLQ.
	 */
	public void writeLong(long v) throws IOException {
		writeUnsignedLong(encodeZigZagLong(v));
	}

	/**
	 * Writes an unsigned long value encoded with VLQ.
	 * Both negative and positive values are supported, but only positive values are encoded
	 * efficiently, negative values are taking a toll and use six bytes. Use {@link #writeLong}
	 * to encode negative and positive values.
	 * <p>
	 * Do not use it for negative values, the resulting varint is always ten
	 *       bytes long – it is, effectively, treated like a very large unsigned integer.
	 *       If you use {@link #writeLong}, the resulting varint uses ZigZag encoding,
	 *       which is much more efficient.
	 */
	public void writeUnsignedLong(long v) throws IOException {
		byte[] buffer = new byte[10];
		int position = 0;
		long value = v;
		while (true) {
			if ((value & ~0x7FL) == 0) {
				buffer[position++] = (byte) value;
				out.write(buffer, 0, position);
				return;
			} else {
				buffer[position++] = (byte) (((int) value & 0x7F) | 0x80);
				value >>>= 7;
			}
		}
	}

	public static int encodeZigZagInt(final int n) {
		// Note: the right-shift must be arithmetic
		return (n << 1) ^ (n >> 31);
	}

	public static long encodeZigZagLong(final long n) {
		// Note: the right-shift must be arithmetic
		return (n << 1) ^ (n >> 63);
	}
}
