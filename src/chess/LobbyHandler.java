package chess;

import java.io.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 
 * @author Graham & Dennis
 *
 * This handler is attached to the server in the Server class.
 * Handles an incoming client requests to /chess/lobby.
 * If user is logged in, server responds with the lobby page.
 * Otherwise new users are redirected to the login page.
 */
public class LobbyHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {

		// Get handle
		String handle = Server.getCookie(t,"handle");
		if (handle == null) {
			Server.redirect(t, "login");
			return;
		}

		// send response to client
		Server.sendResponse(t, 200, new String(Server.readFile("parts/lobby.html")));
	}

}
