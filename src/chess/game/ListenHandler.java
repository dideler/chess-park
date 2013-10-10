package chess.game;

import java.io.*;

import chess.ClientInfo;
import chess.Server;

import com.sun.net.httpserver.*;

/**
 * 
 * @author Graham S. & Dennis I.
 * 
 *         Handles an incoming HTTP connection
 */
public class ListenHandler implements HttpHandler {
	
	//static ChessGame game = new ChessGame();
	
	public static ListenThread theThread;
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		//System.out.println("ListenHandler: ");
		// Lookup player
		String handle = Server.getCookie(t,"handle");
		//System.out.println("ListenHandler: " + handle);
		if (handle == null) {
			Server.sendResponse(t, 200, "<error>No handle found</error>");
			return;
		}
		ClientInfo clientInfo = Server.getInstance().lookupClient(handle);
		
		// Get table id from GET query
		String line = t.getRequestURI().getQuery();		
		if (line == null || !line.matches("^table=[0-9]+(&.*)?$")) {
			Server.sendResponse(t, 200,
					"<error>Strange error, bad request</error>");
			return;
		}
		
		// get GET values
		String[] parts = line.split("&");
		int tid = Integer.parseInt(parts[0].split("=")[1]); // Destination.

		// Lookup table
		//ChessGame game = clientInfo.game;
		ChessGame game = Server.getInstance().findTable(tid);
		if (game == null) {
			Server.sendResponse(t, 200, "<error>Table not found</error>");
			return;
		}
		
		// Check if client just loaded the page
		if (parts[1] != null && parts[1].matches("reset=1")) {
			clientInfo.gameObservers.remove(tid);
		}
		
		theThread = new ListenThread(t,clientInfo,game);
		theThread.start();
	}
}
