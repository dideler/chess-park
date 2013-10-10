package chess.lobby;

import java.io.*;

import chess.ClientInfo;
import chess.Server;

import com.sun.net.httpserver.*;

/**
 * 
 * @author Graham S. & Dennis I.
 * 
 * This handler is attached to the server in the Server class.
 * Handles an incoming client requests to /chess/lobbylisten.
 */
public class LobbyListenHandler implements HttpHandler {
	public static LobbyListenThread theThread;
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		// Lookup player
		String handle = Server.getCookie(t,"handle");
		if (handle == null) {
			Server.sendResponse(t, 200, "<error>No handle found</error>");
			return;
		}
		ClientInfo clientInfo = Server.getInstance().lookupClient(handle);
		
		String line = t.getRequestURI().getQuery();		
		if (line != null && line.matches("^reset=1(&.*)?$")) {
			clientInfo.lobbyObserver = null;
		}
		
		theThread = new LobbyListenThread(t,clientInfo);
		theThread.start();
	}
}
