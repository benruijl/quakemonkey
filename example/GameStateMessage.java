package example;

import java.util.List;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * Example master gamestate that has to be sent from the server to the client
 * every server tick.
 * 
 * @author Ben Ruijl
 * 
 */
@Serializable
public class GameStateMessage extends AbstractMessage {
	private String name;
	private List<Float> position;
	private List<Float> orientation;
	private byte id;

	public GameStateMessage() {
		super(false); // we want to use UDP
	}

	public GameStateMessage(String name, List<Float> position,
			List<Float> orientation, byte id) {
		super(false); // we want to use UDP
		this.name = name;
		this.position = position;
		this.orientation = orientation;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public List<Float> getPosition() {
		return position;
	}

	public List<Float> getOrientation() {
		return orientation;
	}

	public byte getId() {
		return id;
	}
}
