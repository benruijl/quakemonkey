package diff;

import com.jme3.network.serializing.Serializer;

/**
 * Registers messages in the serializer that are required for the snapshot
 * protocol, for both the server and the client.
 * 
 * @author Ben Ruijl
 * 
 */
public class DiffClassRegistration {
	/**
	 * Registers the messages that are required for the snapshot protocol, for
	 * both the server and the client. Make <b>absolutely</b> sure that this function
	 * is called before creation of the server and the client and that the
	 * position in the code relative to other {@link Serializer.registerClass}
	 * calls is the same.
	 */
	public static void registerClasses() {
		Serializer
				.registerClass(DiffMessage.class, new DiffMessageSerializer());
		Serializer.registerClass(AckMessage.class);
		Serializer.registerClass(LabeledMessage.class);
	}
}
