package diff;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * This message is used to send the byte-level difference of two messages to the
 * client.
 * 
 * @author Ben Ruijl
 * 
 */
@Serializable
public class DiffMessage extends AbstractMessage {
	private byte[] flag;
	private int[] data;

	public DiffMessage() {
		super(false);
	}

	public DiffMessage(byte[] flag, int[] data) {
		super(false);
		this.data = data;
		this.flag = flag;
	}

	public int[] getData() {
		return data;
	}

	public byte[] getFlag() {
		return flag;
	}

}
