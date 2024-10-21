package com.satergo.ergonnection;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class VLQInputStream extends FilterInputStream {
	/**
	 * Creates a VLQInputStream that uses the specified
	 * underlying InputStream.
	 *
	 * @param in the specified input stream
	 */
	public VLQInputStream(InputStream in) {
		super(in);
	}

	// Classic methods

	/**
	 * @see java.io.DataInput#readFully(byte[])
	 */
	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	/**
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 */
	public void readFully(byte[] b, int off, int len) throws IOException {
		Objects.checkFromIndexSize(off, len, b.length);
		int n = 0;
		while (n < len) {
			int count = in.read(b, off + n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
	}

	/**
	 * Reads exactly {@code length} bytes and throws an exception otherwise.
	 */
	public byte[] readNFully(int length) throws IOException {
		byte[] bytes = new byte[length];
		readFully(bytes);
		return bytes;
	}

	public boolean readBoolean() throws IOException {
		int b = in.read();
		if (b < 0)
			throw new EOFException();
		return b != 0;
	}

	public byte readByte() throws IOException {
		int b = in.read();
		if (b < 0)
			throw new EOFException();
		return (byte) b;
	}

	public int readUnsignedByte() throws IOException {
		int b = in.read();
		if (b < 0)
			throw new EOFException();
		return b;
	}

	// VLQ methods
	/**
	 * Reads using VLQ and then decodes the result using ZigZag.
	 */
	public short readShort() throws IOException {
		return (short) decodeZigZagInt((int) readUnsignedLong());
	}

	public int readUnsignedShort() throws IOException {
		int value = (int) readUnsignedLong();
		if (value < 0 || value > 0xFFFF)
			throw new IllegalArgumentException(value + " is out of unsigned short range");
		return value;
	}

	/**
	 * Reads using VLQ and then decodes the result using ZigZag.
	 */
	public int readInt() throws IOException {
		return decodeZigZagInt((int) readUnsignedLong());
	}

	public long readUnsignedInt() throws IOException {
		long value = readUnsignedLong();
		if (value < 0 || value > 0xFFFFFFFFL)
			throw new IllegalArgumentException(value + " is out of unsigned int range");
		return value;
	}

	/**
	 * Reads using VLQ and then decodes the result using ZigZag.
	 */
	public long readLong() throws IOException {
		return decodeZigZagLong(readUnsignedLong());
	}

	/**
	 * Reads up to (but can be less than) 64 bits of VLQ-encoded value.
	 */
	public long readUnsignedLong() throws IOException {
		long result = 0;
		for (int shift = 0; shift < 64; shift += 7) {
			final byte b = (byte) in.read();
			result |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				return result;
			}
		}
		throw new IllegalStateException("More bytes than needed found");
	}

	/**
	 * Decodes a ZigZag-encoded integer (32 bits)
	 */
	public static int decodeZigZagInt(int n) {
		return (n >>> 1) ^ -(n & 1);
	}

	/**
	 * Decodes a ZigZag-encoded long (64 bits)
	 */
	public static long decodeZigZagLong(long n) {
		return (n >>> 1) ^ -(n & 1);
	}
}
