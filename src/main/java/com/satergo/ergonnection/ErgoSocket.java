package com.satergo.ergonnection;

import com.satergo.ergonnection.protocol.Protocol;
import com.satergo.ergonnection.protocol.ProtocolMessage;
import com.satergo.ergonnection.records.Feature;
import com.satergo.ergonnection.records.Peer;
import org.bouncycastle.jcajce.provider.digest.Blake2b.Blake2b256;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ErgoSocket extends Socket {

	public static final byte[] MAINNET_MAGIC = { 1, 0, 2, 4 }, TESTNET_MAGIC = { 2, 0, 2, 3 };

	/**
	 * A basic feature set specifying that the state is "utxo", the client is verifying transactions, has no PoPoW suffix, and 1 block is stored.
	 */
	public static final List<Feature> BASIC_FEATURE_SET = Collections.singletonList(new Feature(16, new byte[] { 0, 1, 0, 1 }));

	private final byte[] networkMagic;

	private final Peer self;

	private @Nullable Peer peer;
	// Note: Using DataInputStream instead of VLQInputStream here is intentional
	private final DataInputStream in;

	/**
	 * Creates a new socket
	 *
	 * @param address      Address to connect to
	 * @param self         A Peer object that represents this client itself (this is the data that will be sent to the target peer)
	 * @param networkMagic The magic bytes of this network, see {@link #MAINNET_MAGIC} and {@link #TESTNET_MAGIC}
	 * @throws IOException socket exception
	 */
	public ErgoSocket(InetSocketAddress address, Peer self, byte[] networkMagic) throws IOException {
		super(address.getAddress(), address.getPort());
		if (networkMagic.length != 4)
			throw new IllegalArgumentException("networkMagic must be 4 bytes");
		this.self = self;
		this.networkMagic = networkMagic;
		this.in = new DataInputStream(getInputStream());
	}

	/**
	 * Creates a new socket for the mainnet
	 *
	 * @param address   Address to connect to
	 * @param self      A Peer object that represents this client itself (this is the data that will be sent to the target peer)
	 * @throws IOException socket exception
	 */
	public ErgoSocket(InetSocketAddress address, Peer self) throws IOException {
		this(address, self, MAINNET_MAGIC);
	}

	private @Nullable MessageListener messageListener;

	/**
	 * Starts a listener thread unless the socket already had one in which case it is returned.
	 * The handshake must already have been finished before this is used.
	 * After calling this method, {@link #acceptMessage()} must not be used.
	 *
	 * @return The listener object, for adding consumers
	 */
	public MessageListener listen() {
		if (messageListener == null) messageListener = new MessageListener(this);
		return messageListener;
	}

	public boolean isBlockingMode() {
		return messageListener == null;
	}

	public void send(ProtocolMessage message) throws IOException {
		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			 // Note: Using DataOutputStream instead of VLQInputStream here is intentional
			 DataOutputStream out = new DataOutputStream(bytes)) {
			out.write(networkMagic);
			out.write(message.code());
			byte[] data = message.toByteArray();
			out.writeInt(data.length);
			byte[] checksum = new Blake2b256().digest(data);
			out.write(checksum, 0, 4);
			out.write(data);
			out.flush();
			getOutputStream().write(bytes.toByteArray());
		}
	}

	/**
	 * Blocks until a message has been received, deserializes it, and returns it.
	 *
	 * @throws SocketException if the socket is closed, or it was closed while the message was being read
	 * @return A message from the peer
	 */
	public ProtocolMessage acceptMessage() throws IOException {
		int code;
		byte[] bytes;
		try {
			byte[] magic = new byte[4];
			in.readFully(magic);
			if (!Arrays.equals(networkMagic, magic)) {
				close();
				throw new IllegalArgumentException("Incorrect magic " + Arrays.toString(magic) + " received (must be " + Arrays.toString(networkMagic) + ")");
			}
			code = in.readUnsignedByte();
			int length = in.readInt();
			in.skipNBytes(4); // checksum
			bytes = new byte[length];
			in.readFully(bytes);
		} catch (EOFException e) {
			// Receiving this exception is due to the socket being closed, but it will not have
			// been marked as such so this method is called
			close();
			SocketException ex = new SocketException("Socket is closed");
			ex.initCause(e);
			throw ex;
		}
		return Protocol.deserializeMessage(code, bytes);
	}

	public void sendHandshake() throws IOException {
		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			 VLQOutputStream out = new VLQOutputStream(bytes)) {
			out.writeUnsignedLong(System.currentTimeMillis());
			self.serialize(out);
			out.flush();
			getOutputStream().write(bytes.toByteArray());
		}
	}

	public void acceptHandshake() throws IOException {
		VLQInputStream vlqIn = new VLQInputStream(in);
		long time = vlqIn.readUnsignedLong();
		peer = Peer.deserialize(vlqIn);
	}

	public boolean isHandshakeReceived() { return peer != null; }

	/**
	 * @return Information about the node that has been connected to.
	 */
	public Peer getPeerInfo() { return peer; }
}
