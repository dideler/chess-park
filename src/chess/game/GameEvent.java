package chess.game;

public class GameEvent {
	enum EventType {
		WHITE_JOIN, BLACK_JOIN, MOVE
	}
	
	final public EventType type;
	final public Object data;
	
	private GameEvent(EventType type, Object data) {
		this.type = type;
		this.data = data;
	}
	
	public ChessGame.Move getMove() {
		if (type==EventType.MOVE) {
			return (ChessGame.Move)data;
		}
		return null;
	}

	public static GameEvent NewWhiteJoin() {
		return new GameEvent(EventType.WHITE_JOIN,null);
	}
	public static GameEvent NewBlackJoin() {
		return new GameEvent(EventType.BLACK_JOIN,null);
	}
	public static GameEvent NewMove(ChessGame.Move move) {
		return new GameEvent(EventType.MOVE, move);
	}
}
