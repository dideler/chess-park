package chess.game;

import java.io.PrintStream;

import chess.ClientInfo;
import eventserver.*;

public class ChessGame {
	public static class Move {
		public final int from, dest;

		public Move(int from, int dest) {
			this.from = from;
			this.dest = dest;
		}
	}

	public final int board[];
	private ClientInfo whitePlayer;
	private ClientInfo blackPlayer;
	public boolean isWhiteTurn;
	public final int tid;
	private boolean whiteCastleLeft = true;
	private boolean whiteCastleRight = true;
	private boolean blackCastleLeft = true;
	private boolean blackCastleRight = true;

	public final EventServer<GameEvent> eventList;

	public ClientInfo getWhitePlayer() {
		return whitePlayer;
	}

	public ClientInfo getBlackPlayer() {
		return blackPlayer;
	}

	public void setWhitePlayer(ClientInfo client) {
		whitePlayer = client;
		eventList.addEvent(GameEvent.NewWhiteJoin());
	}

	public void setBlackPlayer(ClientInfo client) {
		blackPlayer = client;
		eventList.addEvent(GameEvent.NewBlackJoin());
	}

	public ChessGame(ClientInfo whitePlayer, ClientInfo blackPlayer, int tid) {
		this.tid = tid;
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;
		board = new int[] { -4, -2, -3, -5, -6, -3, -2, -4, //
				-1, -1, -1, -1, -1, -1, -1, -1, //
				0, 0, 0, 0, 0, 0, 0, 0, //
				0, 0, 0, 0, 0, 0, 0, 0, //
				0, 0, 0, 0, 0, 0, 0, 0, //
				0, 0, 0, 0, 0, 0, 0, 0, //
				+1, +1, +1, +1, +1, +1, +1, +1, //
				+4, +2, +3, +5, +6, +3, +2, +4 //
		};
		isWhiteTurn = true;
		eventList = new EventServer<GameEvent>();
	}

	public Move[] movePiece(int from, int dest) {
		if (from == dest) // can't move to self or off of board
			return null;
		if (from < 0 || dest < 0 || from >= 64 || dest >= 64)
			return null;

		int piece = board[from];
		int target = board[dest];
		if (isWhiteTurn && piece <= 0) // verify turn
			return null;
		if (!isWhiteTurn && piece >= 0)
			return null;
		if (piece < 0 && target < 0) // can't capture own piece
			return null;
		if (piece > 0 && target > 0)
			return null;

		Move[] result = null;

		// useful values
		int fromrow = from / 8;
		int fromcol = from % 8;
		int destrow = dest / 8;
		int destcol = dest % 8;
		int dr = destrow - fromrow;
		int dc = destcol - fromcol;
		int adr = Math.abs(dr);
		int adc = Math.abs(dc);

		// unit direction one of {-1,0,1}
		int udr = dr;
		int udc = dc;
		if (adr != 0)
			udr /= adr;
		if (adc != 0)
			udc /= adc;
		// TODO 50-move rule
		// TODO enpassant
		// TODO checkmate
		// TODO castle
		// TODO pawn promotion

		boolean[] threats = null;

		switch (piece) {
		case -1:// black pawn
			if (dc == 0 && dr == 1 && target == 0) { // forward one
				result = new Move[] { new Move(from, dest) };
			}
			if (dc == 0 && dr == 2 && fromrow == 1 && target == 0
					&& board[from + 8] == 0) { // forward two
				result = new Move[] { new Move(from, dest) };
			}
			if ((dc == 1 || dc == -1) && dr == 1 && target > 0) { // capture
				result = new Move[] { new Move(from, dest) };
			}
		case 1:// white pawn
			if (dc == 0 && dr == -1 && target == 0) { // forward one
				result = new Move[] { new Move(from, dest) };
			}
			if (dc == 0 && dr == -2 && fromrow == 6 && target == 0
					&& board[from - 8] == 0) { // forward two
				result = new Move[] { new Move(from, dest) };
			}
			if ((dc == 1 || dc == -1) && dr == -1 && target < 0) { // capture
				result = new Move[] { new Move(from, dest) };
			}
			break;
		case -2: // Knight
		case 2:
			if ((adr == 1 && adc == 2) || (adr == 2 && adc == 1)) {
				result = new Move[] { new Move(from, dest) };
			}
			break;
		case -3: // Bishop
		case 3:
			if (adr == adc) {
				for (int i = 1; i < adr; i++) { // check for clear path
					if (board[from + i * udr * 8 + i * udc] != 0) {
						return null;
					}
				}
				result = new Move[] { new Move(from, dest) };
			}
			break;
		case -4: // Rook
		case 4:
			if (dr == 0 || dc == 0) {
				for (int i = 1; i < Math.max(adr, adc); i++) { // check for
																// clear path
					if (board[from + i * udr * 8 + i * udc] != 0) {
						return null;
					}
				}
				result = new Move[] { new Move(from, dest) };
			}
			break;
		case -5: // Queen
		case 5:
			if (adr == adc) {
				for (int i = 1; i < adr; i++) { // check for clear path
					if (board[from + i * udr * 8 + i * udc] != 0) {
						return null;
					}
				}
				result = new Move[] { new Move(from, dest) };
			} else if (dr == 0 || dc == 0) {
				for (int i = 1; i < Math.max(adr, adc); i++) { // check for
																// clear path
					if (board[from + i * udr * 8 + i * udc] != 0) {
						return null;
					}
				}
				result = new Move[] { new Move(from, dest) };
			}
			break;
		case -6:// black king
		case 6:// white king
			if (threats == null) {
				threats = makeThreatMap(board, isWhiteTurn);
			}
			if (adr <= 1 && adc <= 1 && !threats[dest]) { // normal movement
				result = new Move[] { new Move(from, dest) };
			} else if (blackCastleLeft && from == 4 && dest == 2 && board[0] == -4
					&& board[1] == 0 && board[2] == 0 && board[3] == 0
					&& !threats[4] && !threats[3] && !threats[2]) {
				// black castle left
				result = new Move[] { new Move(4, 2), new Move(0, 3) };
			} else if (blackCastleRight && from == 4 && dest == 6 && board[7] == -4
					&& board[6] == 0 && board[5] == 0 && !threats[4]
					&& !threats[5] && !threats[6]) {
				// black castle right
				result = new Move[] { new Move(4, 6), new Move(7, 5) };
			} else if (whiteCastleLeft && from == 56 + 4 && dest == 56 + 2 && board[56 + 0] == 4
					&& board[56 + 1] == 0 && board[56 + 2] == 0
					&& board[56 + 3] == 0 && !threats[56 + 4]
					&& !threats[56 + 3] && !threats[56 + 2]) {
				// white castle left
				result = new Move[] { new Move(56 + 4, 56 + 2),
						new Move(56 + 0, 56 + 3) };
			} else if (whiteCastleRight && from == 56 + 4 && dest == 56 + 6 && board[56 + 7] == 4
					&& board[56 + 6] == 0 && board[56 + 5] == 0
					&& !threats[56 + 4] && !threats[56 + 5] && !threats[56 + 6]) {
				// white castle right
				result = new Move[] { new Move(56 + 4, 56 + 6),
						new Move(56 + 7, 56 + 5) };
			}
			/*
			 * if (dr == 0 && dc == -2 && fromcol==4 && (fromrow == 0 || fromrow
			 * == 7)) { if () }
			 */
		}

		if (result != null) {
			// record values before move (in case it puts player in check)
			int[] oldvals = new int[result.length];
			for (int i = 0; i < result.length; i++) {
				Move move = result[i];
				oldvals[i] = move.dest;
				board[move.dest] = board[move.from]; // apply the move
				board[move.from] = 0;
			}
			// check if the player put himself in check
			threats = makeThreatMap(board, isWhiteTurn);
			/*
			 * for(int i = 0; i < 64; i++) { System.out.print(threats[i] ? 1 :
			 * 0); if (i%8==7)System.out.println(); }
			 */
//			for (int s = 0; s < 64; s++) {
//				if ((isWhiteTurn && board[s] == 6)
//						|| (!isWhiteTurn && board[s] == -6)) {
//					if (threats[s]) {
//						// Graaah! all that work and the client put himself in
//						// check!
//						for (int i = result.length - 1; i >= 0; i--) {
//							Move move = result[i];
//							board[move.from] = board[move.dest];
//							board[move.dest] = oldvals[i];
//						}
//						return null;
//					}
//					break;
//				}
//			}
			isWhiteTurn = !isWhiteTurn;
			// castling rules
			if (piece == -6) {
				blackCastleLeft = blackCastleRight = false;
			} else if (piece == 6) {
				whiteCastleLeft = whiteCastleRight = false;
			} else if (piece == -4) {
				if (from == 0)
					blackCastleLeft = false;
				if (from == 7)
					blackCastleRight = false;
			} else if (piece == 4) {
				if (from == 56)
					whiteCastleLeft = false;
				if (from == 63)
					whiteCastleRight = false;
			}
		}

		return result;
	}

	public void print(PrintStream stream) {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				int val = board[r * 8 + c];
				if (val >= 0)
					stream.print(" ");
				stream.print(val);
			}
			stream.println();
		}
		// System.out
	}

	private static boolean[] makeThreatMap(int[] board, boolean isWhiteTurn) {

		// Build a map of which squares the opponent is threatening, for
		// verifying
		// check and castle rules
		// Note: it's okay if the opponent threatens their own square
		boolean[] threats = new boolean[64];
		for (int s = 0; s < 64; s++) {
			threats[s] = false;
		}

		for (int s = 0; s < 64; s++) {
			// ignore own pieces and blanks
			if (isWhiteTurn && board[s] >= 0)
				continue;
			if (!isWhiteTurn && board[s] <= 0)
				continue;
			int srow = s / 8;
			int scol = s % 8;
			switch (board[s]) {
			case -1: // pawn
				if (scol >= 1)
					threats[s + 8 - 1] = true;
				if (scol <= 6)
					threats[s + 8 + 1] = true;
				break;
			case 1:
				if (scol >= 1)
					threats[s - 8 - 1] = true;
				if (scol <= 6)
					threats[s - 8 + 1] = true;
				break;
			case 2: // knight
			case -2:
				if (srow >= 1 && scol >= 2)
					threats[s - 8 - 2] = true;
				if (srow >= 2 && scol >= 1)
					threats[s - 16 - 1] = true;
				if (srow >= 1 && scol <= 5)
					threats[s - 8 + 2] = true;
				if (srow >= 2 && scol <= 6)
					threats[s - 16 + 1] = true;
				if (srow <= 6 && scol >= 2)
					threats[s + 8 - 2] = true;
				if (srow <= 5 && scol >= 1)
					threats[s + 16 - 1] = true;
				if (srow <= 6 && scol <= 5)
					threats[s + 8 + 2] = true;
				if (srow <= 5 && scol <= 6)
					threats[s + 16 + 1] = true;
				break;
			case 3: // bishop
			case -3:
				for (int irow = srow - 1, icol = scol - 1; irow >= 0
						&& icol >= 0; irow--, icol--) {
					threats[irow * 8 + icol] = true;
					if (board[irow * 8 + icol] != 0)
						break;
				}
				for (int irow = srow - 1, icol = scol + 1; irow >= 0
						&& icol < 8; irow--, icol++) {
					threats[irow * 8 + icol] = true;
					if (board[irow * 8 + icol] != 0)
						break;
				}
				for (int irow = srow + 1, icol = scol - 1; irow < 8
						&& icol >= 0; irow++, icol--) {
					threats[irow * 8 + icol] = true;
					if (board[irow * 8 + icol] != 0)
						break;
				}
				for (int irow = srow + 1, icol = scol + 1; irow < 8 && icol < 8; irow++, icol++) {
					threats[irow * 8 + icol] = true;
					if (board[irow * 8 + icol] != 0)
						break;
				}
				break;
			case 4: // rook
			case -4:
				for (int irow = srow - 1; irow >= 0; irow--) {
					threats[irow * 8 + scol] = true;
					if (board[irow * 8 + scol] != 0)
						break;
				}
				for (int irow = srow + 1; irow < 8; irow++) {
					threats[irow * 8 + scol] = true;
					if (board[irow * 8 + scol] != 0)
						break;
				}
				for (int icol = scol - 1; icol >= 0; icol--) {
					threats[srow * 8 + icol] = true;
					if (board[srow * 8 + icol] != 0)
						break;
				}
				for (int icol = scol + 1; icol < 8; icol++) {
					threats[srow * 8 + icol] = true;
					if (board[srow * 8 + icol] != 0)
						break;
				}
				break;
			case 5: // queen
			case -5:
				for (int irow = srow - 1, icol = scol - 1; irow >= 0
						&& icol >= 0; irow--, icol--) {
					threats[irow * 8 + icol] = true;
					if (board[irow * 8 + icol] != 0)
						break;
				}
				for (int irow = srow - 1, icol = scol + 1; irow >= 0
						&& icol < 8; irow--, icol++) {
					threats[irow * 8 + icol] = true;
					if (board[irow * 8 + icol] != 0)
						break;
				}
				for (int irow = srow + 1, icol = scol - 1; irow < 8
						&& icol >= 0; irow++, icol--) {
					threats[irow * 8 + icol] = true;
					if (board[irow * 8 + icol] != 0)
						break;
				}
				for (int irow = srow + 1, icol = scol + 1; irow < 8 && icol < 8; irow++, icol++) {
					threats[irow * 8 + icol] = true;
					if (board[irow * 8 + icol] != 0)
						break;
				}
				for (int irow = srow - 1; irow >= 0; irow--) {
					threats[irow * 8 + scol] = true;
					if (board[irow * 8 + scol] != 0)
						break;
				}
				for (int irow = srow + 1; irow < 8; irow++) {
					threats[irow * 8 + scol] = true;
					if (board[irow * 8 + scol] != 0)
						break;
				}
				for (int icol = scol - 1; icol >= 0; icol--) {
					threats[srow * 8 + icol] = true;
					if (board[srow * 8 + icol] != 0)
						break;
				}
				for (int icol = scol + 1; icol < 8; icol++) {
					threats[srow * 8 + icol] = true;
					if (board[srow * 8 + icol] != 0)
						break;
				}
				break;
			case 6: // king
			case -6:
				if (srow >= 1 && scol >= 1)
					threats[s - 8 - 1] = true;
				if (srow >= 1 && scol <= 6)
					threats[s - 8 + 1] = true;
				if (srow <= 6 && scol >= 1)
					threats[s + 8 - 1] = true;
				if (srow <= 6 && scol <= 6)
					threats[s + 8 + 1] = true;
				if (srow >= 1)
					threats[s - 8] = true;
				if (srow <= 6)
					threats[s + 8] = true;
				if (scol >= 1)
					threats[s - 1] = true;
				if (scol <= 6)
					threats[s + 1] = true;
				break;
			}
		}
		return threats;
	}
}
