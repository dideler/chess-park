package chess;

import java.util.HashMap;
import java.util.Map;

import chess.game.GameEvent;
import chess.lobby.LobbyEvent;
import eventserver.Observer;

public class ClientInfo {
	static private int nextid = 0;
	
	public final int pid;
	public final String handle;
	public Observer<LobbyEvent> lobbyObserver = null;
	public Map<Integer, Observer<GameEvent>> gameObservers;
	
	//public ChessGame game;
	
	public ClientInfo(String handle) {
		this.pid = nextid++;
		this.handle = handle;
		this.gameObservers = java.util.Collections
				.synchronizedMap(new HashMap<Integer, Observer<GameEvent>>());
		// this.gameMessages = new MessageQueue<String>();
		// System.out.println("New Client: " + pid + " - " + handle);
	}
}
