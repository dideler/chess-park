package chess.lobby;

import java.io.*;

import chess.Server;

import com.sun.net.httpserver.*;

/**
 * 
 * @author Graham
 *
 * This handler is attached to the server in the Server class.
 * Handles an incoming client requests to /chess/lobbylistener.
 */
public class LobbyListenerHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {

		// Check handle
		String handle = Server.getCookie(t,"handle");
		if (handle == null) {
			Server.redirect(t, "login");
			return;
		}
		Server.getInstance().lookupClient(handle).lobbyObserver = null;
		
		StringBuilder response = new StringBuilder();

		response.append(new String(Server.readFile("parts/lobbylistener-head.html")));
		response.append(new String(Server.readFile("parts/lobbylistener-body.html")));
		
		Server.sendResponse(t, 200, response.toString());
	}

}
