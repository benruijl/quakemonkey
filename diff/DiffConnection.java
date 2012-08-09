package diff;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jme3.network.AbstractMessage;
import com.jme3.network.Message;
import com.jme3.network.base.MessageProtocol;

/**
 * The server-side handler of generating delta messages for one connection. It
 * keeps track of a list of snapshots in a cyclic array and registers the last
 * snapshot that was successfully received by the client.
 * 
 * @author Ben Ruijl
 * @see #ServerDiffHandler
 * @param <T>
 *            Message type
 */
public class DiffConnection<T extends AbstractMessage> {
	private final short numSnapshots;
	private final List<T> snapshots;
	private short curPos; // position in cyclic array
	private short ackPos;

	public DiffConnection(short numSnapshots) {
		this.numSnapshots = numSnapshots;
		snapshots = new ArrayList<>(numSnapshots);

		for (int i = 0; i < numSnapshots; i++) {
			snapshots.add(null);
		}

		curPos = 0;
		ackPos = -1;
	}

	public Message generateSnapshot(T message) {
		short oldPos = curPos;
		snapshots.set((short) (oldPos % numSnapshots), message);
		curPos++;

		// only allow positive positions
		if (curPos < 0) {
			curPos = 0;
		}

		if (ackPos < 0) {
			return new LabeledMessage(oldPos, message);
		}

		T oldMessage = snapshots.get(ackPos % numSnapshots);
		return new LabeledMessage(oldPos, generateDelta(message, oldMessage,
				ackPos));
	}

	public void registerAck(short id) {
		// because the array is cyclic, the ackPos could be in front of the id,
		// so we check if the difference between the two is very large ( > 4
		// minutes at
		// 60 fps). this is how we identify an overflow.
		if (id > ackPos || ackPos - id > Short.MAX_VALUE / 2) {
			System.out
					.println("[Server message] client received message " + id);
			ackPos = id;
			return;
		}

		System.out.println("Client received old message " + id);
	}

	/**
	 * Returns a delta message from message and prevMessage or just message if
	 * that happens to be smaller.
	 * 
	 * @param message
	 *            Message to send
	 * @param prevMessage
	 *            Previous message
	 * @return
	 */
	public Message generateDelta(T message, T prevMessage, short prevID) {
		// TODO: skip size?
		ByteBuffer old = MessageProtocol.messageToBuffer(prevMessage, null);
		ByteBuffer buffer = MessageProtocol.messageToBuffer(message, null);

		int intBound = (int) (Math.ceil(buffer.remaining() / 4)) * 4;
		old.limit(intBound);
		buffer.limit(intBound); // set buffers to be the same size

		IntBuffer diffInts = IntBuffer.allocate(buffer.limit()); // great
																	// overestimation

		// check block of size int
		int numBits = intBound / 4;
		int numBytes = (numBits - 1) / 8 + 1;
		byte[] flag = new byte[numBytes];

		// also works if old and new are not the same size, but less efficiently
		int i = 0;
		while (buffer.remaining() >= 4) {
			int val = buffer.getInt();
			if (old.remaining() < 4 || val != old.getInt()) {
				diffInts.put(val);
				flag[i / 8] |= 1 << (i % 8);
				// System.out.println("Int " + i + " changed.");
			}
			i++;
		}

		diffInts.flip();

		/* Check what is smaller, delta message or original buffer */
		// TODO: fix numbers to be more accurate
		if (diffInts.remaining() * 4 + 8 < buffer.limit()) {
			int[] b = new int[diffInts.remaining()];
			diffInts.get(b, 0, b.length);

			return new DiffMessage(prevID, flag, b);
		} else {
			return message;
		}
	}

}
