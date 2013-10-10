package chess.lobby;

import chess.ClientInfo;
import chess.Server;
import chess.game.ChessGame;

import com.sun.net.httpserver.HttpExchange;

import eventserver.Subscriber;

public class LobbyListenThread extends Thread {
	public final ClientInfo clientInfo;
	private final HttpExchange t;
	//private String message = "No Message";
	
	LobbyListenThread(HttpExchange t, ClientInfo clientInfo) {
		this.t = t;
		this.clientInfo = clientInfo;
	}
	
	public synchronized void notifyToEvent() {
		notify();
	}
	
	synchronized void waitForEvent(long timeout) {
		try {
			wait(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		if (clientInfo.lobbyObserver == null || clientInfo.lobbyObserver.expired()) {
			// Entered the lobby
			// create new observer
			//clientInfo.lobbyObserver = Server.getInstance().newLobbyListener();
			clientInfo.lobbyObserver = Server.getInstance().lobbyServer.observe();

			// list available tables
			StringBuilder builder = new StringBuilder();
			builder.append("<tablelist>");
			for (ChessGame game : Server.getInstance().getTables()) {
				if (game.getWhitePlayer() == null || game.getBlackPlayer() == null) {
					builder.append("<table><tid>");
					builder.append(game.tid);
					builder.append("</tid><white>");
					if (game.getWhitePlayer() != null) {
						builder.append(game.getWhitePlayer().handle);
					}
					builder.append("</white><black>");
					if (game.getBlackPlayer() != null) {
						builder.append(game.getBlackPlayer().handle);
					}
					builder.append("</black></table>");
				}
			}
			builder.append("</tablelist>");

			Server.sendResponse(t, 200, builder.toString());
			return;
		}
		else {
			if (!clientInfo.lobbyObserver.hasNext()) {
				Server server = Server.getInstance();

				Subscriber subscriber = new Subscriber();
				server.lobbyServer.subscribe(subscriber);
				subscriber.waitForEvent(10000);
				server.lobbyServer.unsubscribe(subscriber);
			}
			
			StringBuilder builder = new StringBuilder();
			builder.append("<eventlist>");
			if (clientInfo.lobbyObserver != null) {
				while(clientInfo.lobbyObserver.hasNext()) {
					LobbyEvent event = clientInfo.lobbyObserver.next();
					switch(event.type) {
					case CLOSE:
					case FILL:
						builder.append("<removetable><tid>");
						builder.append(event.tid);
						builder.append("</tid></removetable>");
						break;
					case NEW:
						ChessGame game = Server.getInstance().findTable(event.tid);
						builder.append("<addtable><tid>");
						builder.append(game.tid);
						builder.append("</tid><white>");
						if (game.getWhitePlayer() != null) {
							builder.append(game.getWhitePlayer().handle);
						}
						builder.append("</white><black>");
						if (game.getBlackPlayer() != null) {
							builder.append(game.getBlackPlayer().handle);
						}
						builder.append("</black></addtable>");
						break;
					}
				}
			}
			builder.append("</eventlist>");
			Server.sendResponse(t, 200, builder.toString());
		}
	}
}