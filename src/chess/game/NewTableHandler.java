package chess.game;

import java.io.*;

import chess.ClientInfo;
import chess.Server;

import com.sun.net.httpserver.*;

/**
 * 
 * @author Graham
 *
 * Handles an incoming HTTP connection
 */
public class NewTableHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {

		// Check handle
		String handle = Server.getCookie(t,"handle");
		if (handle == null) {
			Server.redirect(t, "login");
			return;
		}
		ClientInfo clientInfo = Server.getInstance().lookupClient(handle);
		
		ChessGame game = Server.getInstance().createTable(clientInfo);
		//game.whitePlayer = Server.getInstance().lookupClient(handle);
		//Server.getInstance().lookupClient(handle).game = game;
		
		int tid = game.tid;
		
		Server.redirect(t, "game?table=" + tid);
		return;		
	}

}
