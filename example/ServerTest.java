package example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;

import diff.DiffClassRegistration;
import diff.ServerDiffHandler;

/**
 * An example server that shows how the snapshot network code works.
 * 
 * @author Ben Ruijl
 * 
 */
public class ServerTest {
	int serverTicks = 0;
	final Server myServer;
	final ServerDiffHandler<GameStateMessage> diffHandler;

	public ServerTest() throws IOException, InterruptedException {
		DiffClassRegistration.registerClasses();
		Serializer.registerClass(GameStateMessage.class);

		myServer = Network.createServer(6143);
		diffHandler = new ServerDiffHandler<GameStateMessage>(myServer);
		myServer.start();

		myServer.addConnectionListener(new ConnectionListener() {

			@Override
			public void connectionRemoved(Server server, HostedConnection conn) {
				System.out.println("Connection closed from "
						+ conn.getAddress());

			}

			@Override
			public void connectionAdded(Server server, HostedConnection conn) {
				System.out.println("New connection from " + conn.getAddress());
			}
		});

		List<Float> or = Arrays.asList(new Float[] { 0.5f, 0.6f, 0.7f });

		for (int i = 0; i < 10000; i++) {
			if (myServer.hasConnections()) {
				List<Float> newPos = new ArrayList<Float>(
						Arrays.asList(new Float[] { (float) serverTicks, 8.0f,
								3.0f }));

				GameStateMessage newMessage = new GameStateMessage("test",
						newPos, or, (byte) 0);

				/* Dispatch same message to all clients */
				diffHandler.dispatchMessage(myServer,
						Filters.in(myServer.getConnections()), newMessage);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			serverTicks++;
		}

		myServer.close();

	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		new ServerTest();
	}

}
