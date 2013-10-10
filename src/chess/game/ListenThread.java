package chess.game;

import chess.ClientInfo;
import chess.Server;
import chess.game.ChessGame.Move;

import com.sun.net.httpserver.HttpExchange;

import eventserver.Observer;
import eventserver.Subscriber;

class ListenThread extends Thread {
	private final ClientInfo clientInfo;
	private final HttpExchange t;
	private final ChessGame game;
	//private String message = "No Message";
	
	ListenThread(HttpExchange t, ClientInfo clientInfo, ChessGame game) {
		this.t = t;
		this.clientInfo = clientInfo;
		this.game = game;
	}

	public void run() {
		int tid = game.tid;
		Observer<GameEvent> observer = clientInfo.gameObservers.get(tid);

		if (observer == null || observer.expired()) {
			// no good observer, make a new one, sending current state of game
			clientInfo.gameObservers.put(tid, game.eventList.observe());

			StringBuilder builder = new StringBuilder();
			builder.append("<eventlist>");

			// Send the entire board
			builder.append("<board>[");
			builder.append(game.board[0]);
			for(int i = 1; i < 64; i++) {
				builder.append(",");
				builder.append(game.board[i]);
			}
			builder.append("]</board>");
			
			// send players
			if (game.getWhitePlayer() != null) {
				builder.append("<whitePlayer>");
				builder.append(game.getWhitePlayer().handle);
				builder.append("</whitePlayer>");
			}
			if (game.getBlackPlayer() != null) {
				builder.append("<blackPlayer>");
				builder.append(game.getBlackPlayer().handle);
				builder.append("</blackPlayer>");
			}
			builder.append("</eventlist>");
			
			Server.sendResponse(t, 200, builder.toString());
			return;
		}
		
		// observer exists, check for events
		if (!observer.hasNext()) {
			// wait if no events in queue
			Subscriber subscriber = new Subscriber();
			game.eventList.subscribe(subscriber);
			subscriber.waitForEvent(10000);
			game.eventList.unsubscribe(subscriber);
		}

		// Tell events (if any) to client
		observer = clientInfo.gameObservers.get(tid);
		StringBuilder builder = new StringBuilder();
		builder.append("<eventlist>");
		if (observer != null) {
			while(observer.hasNext()) {
				GameEvent event = observer.next();
				switch(event.type) {
				case WHITE_JOIN:
					builder.append("<whitePlayer>");
					builder.append(game.getWhitePlayer().handle);
					builder.append("</whitePlayer>");
					break;
				case BLACK_JOIN:
					builder.append("<blackPlayer>");
					builder.append(game.getBlackPlayer().handle);
					builder.append("</blackPlayer>");
					break;
				case MOVE:
					Move move = event.getMove();
					builder.append("<move><from>");
					builder.append(move.from);
					builder.append("</from><dest>");
					builder.append(move.dest);
					builder.append("</dest></move>");
					break;
				}
			}
		}
		builder.append("</eventlist>");
		Server.sendResponse(t, 200, builder.toString());
	}
}