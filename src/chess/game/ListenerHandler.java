package chess.game;

import java.io.*;

import chess.Server;

import com.sun.net.httpserver.*;

/**
 * 
 * @author Graham
 *
 * Handles an incoming HTTP connection
 */
public class ListenerHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {

		// Check handle
		String handle = Server.getCookie(t,"handle");
		if (handle == null) {
			Server.redirect(t, "login");
			return;
		}
		//ClientInfo clientInfo = Server.getInstance().lookupClient(handle);
		
		// Get table id from GET query
		String query = t.getRequestURI().getQuery();		
		if (query == null || !query.matches("^table=[0-9]+$")) {
			Server.redirect(t, "lobby?message=Strange error, invalid query");
			return;
		}

		// lookup table
		String[] parts = query.split("&");
		int tid = Integer.parseInt(parts[0].split("=")[1]);
		ChessGame game = Server.getInstance().findTable(tid);	
		if (game == null) {
			Server.redirect(t, "lobby?message=That table doesn't exist");
			return;
		}
		
		StringBuilder response = new StringBuilder();

		response.append(new String(Server.readFile("parts/listener-head.html")));
		
		response.append("<script type=\"text/javascript\">");
		response.append("Table={id:" + tid + "};");
		response.append("</script>");
		
		response.append(new String(Server.readFile("parts/listener-body.html")));
		
		Server.sendResponse(t, 200, response.toString());
	}

}
