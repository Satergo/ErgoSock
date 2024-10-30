import com.satergo.ergonnection.ErgoSocket;
import com.satergo.ergonnection.Version;
import com.satergo.ergonnection.messages.Inv;
import com.satergo.ergonnection.messages.ModifierRequest;
import com.satergo.ergonnection.messages.ModifierResponse;
import com.satergo.ergonnection.modifiers.ErgoTransaction;
import com.satergo.ergonnection.protocol.ProtocolMessage;
import com.satergo.ergonnection.protocol.ProtocolModifier;
import com.satergo.ergonnection.records.Peer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionLoggerExample {

	public TransactionLoggerExample() throws IOException {

		ErgoSocket ergoSocket = new ErgoSocket(
				new InetSocketAddress("127.0.0.1", 9030),
				new Peer("ergoref", "ergo-mainnet-5.0.24", Version.parse("5.0.24"), ErgoSocket.BASIC_FEATURE_SET));

		ergoSocket.sendHandshake();
		ergoSocket.acceptHandshake();

		ArrayList<byte[]> requestedIds = new ArrayList<>();

		System.out.println("[" + hhmmss() + "] Peer info: " + ergoSocket.getPeerInfo());

		while (true) {
			ProtocolMessage msg = ergoSocket.acceptMessage();
			if (msg instanceof Inv inv) {
				if (inv.typeId() == ErgoTransaction.TYPE_ID) {
					System.out.println("[" + hhmmss() + "] Received ID(s) of transaction(s) in Inv message: " + inv.elements().stream()
							.map(HexFormat.of()::formatHex)
							.collect(Collectors.joining(", ")));
					// request the data of it or them
					requestedIds.addAll(inv.elements());
					ergoSocket.send(new ModifierRequest(ErgoTransaction.TYPE_ID, requestedIds));
				}
			} else if (msg instanceof ModifierResponse mr) {
				for (ProtocolModifier modifier : mr.modifiers()) {
					if (modifier instanceof ErgoTransaction tx) {
						boolean wasRequested = requestedIds.removeIf(id -> Arrays.equals(id, modifier.id()));
						if (wasRequested) {
							System.out.println("[" + hhmmss() + "] Transaction: " + tx);
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new TransactionLoggerExample();
	}

	private static String hhmmss() {
		return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
	}
}
