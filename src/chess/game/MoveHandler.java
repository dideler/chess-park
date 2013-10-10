package chess.game;

import java.io.*;

import chess.Server;
import chess.game.ChessGame.Move;

import com.sun.net.httpserver.*;

/**
 * 
 * @author Graham S. & Dennis I.
 * 
 *         Handles an incoming HTTP connection
 */
public class MoveHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {
		// Lookup player
		String handle = Server.getCookie(t, "handle");
		if (handle == null) {
			Server.sendResponse(t, 200, "<error>Handle not found</error>");
			return;
		}
		// ClientInfo clientInfo = Server.getInstance().lookupClient(handle);

		// get POST line
		InputStream is = (InputStream) t.getRequestBody();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// validate POST line
		String line = br.readLine();
		if (line == null
				|| !line.matches("^from=[0-9]{1,2}&dest=[0-9]{1,2}&table=[0-9]+$")) {
			Server.sendResponse(t, 200,
					"<error>Strange error, bad request</error>");
			return;
		}
		
		// get POST values
		String[] parts = line.split("&");
		int from = Integer.parseInt(parts[0].split("=")[1]); // Source.
		int dest = Integer.parseInt(parts[1].split("=")[1]); // Destination.
		int tid = Integer.parseInt(parts[2].split("=")[1]); // Destination.

		// lookup table
		ChessGame game = Server.getInstance().findTable(tid);
		if (game == null) {
			Server.sendResponse(t, 200, "<error>Table not found</error>");
			return;
		}

		// make sure both players are here
		if (game.getWhitePlayer() == null || game.getBlackPlayer() == null) {
			Server.sendResponse(t, 200,
					"<warning>Please wait for other player</warning>");
			return;
		}

		// Try moving a piece
		Move[] moves = game.movePiece(from, dest);
		if (moves == null || moves.length == 0) {
			Server.sendResponse(t, 200,
					"<warning>Illegal Move</warning>");
			return;
		}

		// Piece(s) moved, build response
		GameEvent[] events = new GameEvent[moves.length];
		for(int i = 0; i < moves.length; i++) {
			events[i] = GameEvent.NewMove(moves[i]);
		}
		game.eventList.addAllEvents(events);

		Server.sendResponse(t, 200, "<ok />");
	}
}
