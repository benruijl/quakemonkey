package diff;

import java.util.HashMap;
import java.util.Map;

import com.jme3.network.AbstractMessage;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filter;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;

/**
 * Handles the dispatching of messages of type {@code T} to clients, using a
 * protocol of delta messages.
 * <p>
 * Important: make sure that you call {@link DiffClassRegistration#registerClasses()} before starting the
 * server.
 * 
 * @author Ben Ruijl
 * 
 * @param <T>
 *            Message type
 * @see #ClientDiffHandler
 */
public class ServerDiffHandler<T extends AbstractMessage> implements
		MessageListener<HostedConnection>, ConnectionListener {
	private final int numHistory;
	private final Map<HostedConnection, DiffConnection<T>> connectionSnapshots;

	public ServerDiffHandler(Server server, int numHistory) {
		this.numHistory = numHistory;
		connectionSnapshots = new HashMap<>();

		server.addMessageListener(this, AckMessage.class);
	}

	public ServerDiffHandler(Server server) {
		this(server, 20);
	}

	/**
	 * Dispatches a message to all clients in the filter. If it is more
	 * efficient to send a delta message, this is sent instead.
	 * 
	 * @param server
	 *            The server that should send the message
	 * @param message
	 *            The message to be sent
	 */
	public void dispatchMessage(Server server,
			Filter<? super HostedConnection> filter, T message) {
		for (HostedConnection connection : server.getConnections()) {
			if (filter.apply(connection)) {
				if (!connectionSnapshots.containsKey(connection)) {
					connectionSnapshots.put(connection, new DiffConnection<T>(
							numHistory));
				}

				Message newMessage = connectionSnapshots.get(connection)
						.generateSnapshot(message);
				server.broadcast(Filters.in(connection), newMessage);
			}
		}
	}

	@Override
	public void messageReceived(HostedConnection source, Message m) {
		if (m instanceof AckMessage && connectionSnapshots.containsKey(source)) {
			connectionSnapshots.get(source).registerAck(
					((AckMessage) m).getId());
		}

	}

	@Override
	public void connectionAdded(Server server, HostedConnection conn) {

	}

	@Override
	public void connectionRemoved(Server server, HostedConnection conn) {
		connectionSnapshots.remove(conn);
	}

}
