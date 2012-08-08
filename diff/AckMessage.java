package diff;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * An acknowledgment message that is sent from the client to the server. It
 * contains and identifier of the message that was received.
 * 
 * @author Ben Ruijl
 * 
 */
// TODO: add the class that the ack is for
@Serializable
public class AckMessage extends AbstractMessage {
	private short id;

	public AckMessage() {
		super(false);
	}

	public AckMessage(short id) {
		this.id = id;
	}

	public short getId() {
		return id;
	}

}
