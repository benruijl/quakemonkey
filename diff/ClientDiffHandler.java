package diff;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.jme3.network.AbstractMessage;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.base.MessageListenerRegistry;
import com.jme3.network.base.MessageProtocol;
import com.jme3.network.serializing.Serializer;

/**
 * Handles the client-side job of receiving either messages of type {@code T} or
 * delta messages. If a delta message is received, it is merged with a cached
 * old message. When the message is processed, an acknowledgment is sent to the
 * server.
 * <p>
 * Client can register message listeners for type {@code T} by calling
 * {@link #addListener()}. It is very important that the client does not listen
 * to message type {@code T} through other methods (for example directly from
 * the server).
 * <p>
 * * Important: make sure that you call
 * {@link DiffClassRegistration#registerClasses()} before starting the client.
 * 
 * @author Ben Ruijl
 * 
 * @param <T>
 *            Message type
 */
@SuppressWarnings("unchecked")
public class ClientDiffHandler<T extends AbstractMessage> implements
		MessageListener<Client> {
	private final Class<T> cls;
	private T currentMessage = null;
	private final MessageListenerRegistry<Client> listenerRegistry;

	public ClientDiffHandler(Client client, Class<T> cls) {
		this.cls = cls;
		listenerRegistry = new MessageListenerRegistry<>();

		client.addMessageListener(this, LabeledMessage.class);
	}

	public void addListener(MessageListener<? super Client> listener) {
		listenerRegistry.addMessageListener(listener);
	}

	public void removeListener(MessageListener<? super Client> listener) {
		listenerRegistry.removeMessageListener(listener);
	}

	/**
	 * Applies the delta message to the old message to generate a new message of
	 * type {@code T}.
	 * 
	 * @param oldMessage
	 *            The old message
	 * @param diffMessage
	 *            The delta message
	 * @return A new message of type {@code T}
	 */
	public T mergeMessage(T oldMessage, DiffMessage diffMessage) {
		ByteBuffer oldBuffer = MessageProtocol
				.messageToBuffer(oldMessage, null);

		/* Copy old message */
		ByteBuffer newBuffer = ByteBuffer.allocate(32767);
		newBuffer.put(oldBuffer);
		newBuffer.position(0);

		int index = 0;
		for (int i = 0; i < 8 * diffMessage.getFlag().length; i++) {
			if ((diffMessage.getFlag()[i / 8] & (1 << (i % 8))) != 0) {
				newBuffer.putInt(i * 4, diffMessage.getData()[index]);
				index++;
			}
		}

		try {
			newBuffer.position(2); // skip size
			return (T) Serializer.readClassAndObject(newBuffer);
		} catch (IOException e) {
			System.out.println("Could not merge messages");
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Process the arrival of either a message of type {@code T} or a delta
	 * message. Sends an acknowledgment to the server when a message is
	 * received.
	 */
	// TODO: fix a jumpback issue that may occur when currentMessage gets set,
	// but ACK does not arrive at server
	@Override
	public void messageReceived(Client source, Message m) {
		if (m instanceof LabeledMessage) {
			LabeledMessage lm = (LabeledMessage) m;

			if (cls.isInstance(lm.getMessage())) {
				currentMessage = (T) lm.getMessage();
			} else {
				if (lm.getMessage() instanceof DiffMessage) {
					System.out.println("Received diff of size "
							+ MessageProtocol.messageToBuffer(lm.getMessage(),
									null).limit());
					T newMessage = mergeMessage(currentMessage,
							(DiffMessage) lm.getMessage());

					currentMessage = newMessage;

				}
			}

			/* Send an ACK back */
			source.send(new AckMessage(lm.getLabel()));

			/* Broadcast changes */
			listenerRegistry.messageReceived(source, currentMessage);

		}
	}
}
