package diff;

import com.jme3.network.AbstractMessage;
import com.jme3.network.Message;
import com.jme3.network.serializing.Serializable;

/**
 * A message containing another message with an identifier.
 * 
 * @author Ben Ruijl
 * 
 */
@Serializable
public class LabeledMessage extends AbstractMessage {
	private short label;
	private Message message;

	/* Required */
	public LabeledMessage() {
		label = 0;
		message = null;
	}

	public LabeledMessage(short label, Message message) {
		super(false);
		this.label = label;
		this.message = message;
	}

	public short getLabel() {
		return label;
	}

	public Message getMessage() {
		return message;
	}

}
