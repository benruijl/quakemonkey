package diff;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.jme3.network.serializing.Serializer;

/**
 * Serializes a delta message efficiently.
 * 
 * @author Ben Ruijl
 * 
 */
@SuppressWarnings("unchecked")
public class DiffMessageSerializer extends Serializer {

	@Override
	public <T> T readObject(ByteBuffer data, Class<T> c) throws IOException {
		short messageID = data.getShort();
		int flagSize = data.getShort();

		byte[] flags = new byte[flagSize];
		data.get(flags, 0, flagSize);

		int intCount = 0;
		for (int i = 0; i < 8 * flagSize; i++) {
			if ((flags[i / 8] & (1 << (i % 8))) != 0) {
				intCount++;
			}
		}

		int[] val = new int[intCount];
		data.asIntBuffer().get(val, 0, intCount);

		return (T) new DiffMessage(messageID, flags, val);
	}

	@Override
	public void writeObject(ByteBuffer buffer, Object object)
			throws IOException {
		DiffMessage diff = (DiffMessage) object;
		buffer.putShort(diff.getMessageId());
		buffer.putShort((short) diff.getFlag().length);
		buffer.put(diff.getFlag());
		buffer.asIntBuffer().put(diff.getData());
		buffer.position(buffer.position() + diff.getData().length * 4);
	}

}
