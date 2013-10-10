package chess.lobby;

public class LobbyEvent {
	public enum EventType {
		NEW, FILL, CLOSE
	}

	public final int tid;
	public final EventType type;
	
	public LobbyEvent(int tid, EventType type) {
		this.tid = tid;
		this.type = type;
	}
}
