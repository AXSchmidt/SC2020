package sc.player2020.logic;

import sc.plugin2020.PieceType;

import java.util.List;

import sc.plugin2020.Board;
import sc.plugin2020.Field;
import sc.plugin2020.FieldState;
import sc.plugin2020.Piece;

public class Helper {
	
	// GLOBAL Constants
	public static int ALPHABETA_DEPTH = 3;	
	public static int TIMEOUTTIME = 1890;
	
	// PRINT Constants
	public static boolean PRINT_ERROR = true;
	public static boolean PRINT_HEADER = true;
	public static boolean PRINT_BOARD = false;
	public static boolean PRINT_FOOTER = true;	
	
	public static boolean PRINT_ALPHABETA = true;
	public static boolean PRINT_ALPHABETA_HEADER = true;
	public static boolean PRINT_ALPHABETA_SHOWMOVES = true;
	public static boolean PRINT_APLHABETA_SHOWBOARD = true;
	
	
	/*********************
	 * L * O * G * I * C *
	 *********************/
	
	/* LOGIC 0:
	 * Ich setze gleich im ersten Zug (egal ob ich anfange oder nicht) meine BEE,
	 * damit ich moeglichst schnell meine ANTs mobil bekomme
	 */
	
	/* LOGIC 1:
	 * Je mehr Felder um die eigene BEE belegt sind, desto schlechter ist das Ganze!
	 * Sollte evtl exponential steigen!
	 */
	public static RateHelper logic1OwnQueenSurround(RateHelper rate, Board board, Field field) {
		RateHelper result = new RateHelper(rate);
		if (Lib.fieldContainsPiece(field, PieceType.BEE)) {
			int beeNeighbors = 0;
			List<Field> beeList = Lib.getNeighbours(board, field);
			for (Field beeGuard : beeList) {
				if (beeGuard.getFieldState() != FieldState.EMPTY) {
					beeNeighbors++;
				}
			}
			if (beeNeighbors > 2) {
				int rating = (int) Math.pow(2, beeNeighbors);
				result.value += rating;
				result.rate.add("  Own Queen Surround: " + rating + field.toString());
			}
		}	
		return result;
	}
	
	/* LOGIC 2:
	 * Die gegenerische BEE zu umzingeln ist super 
	 */
	public static RateHelper logic2OpponentQueenSurround(RateHelper rate, Board board, Field field) {
		RateHelper result = new RateHelper(rate);
		if (Lib.fieldContainsPiece(field, PieceType.BEE)) {
			int beeNeighbors = 0;
			List<Field> beeList = Lib.getNeighbours(board, field);
			for (Field beeGuard : beeList) {
				if (beeGuard.getFieldState() != FieldState.EMPTY) {
					beeNeighbors++;
				}
			}
			if (beeNeighbors > 2) {
				int rating = (int) Math.pow(2, beeNeighbors + 1);
				result.value += rating;
				result.rate.add("  Opponent Queen Surround: " + rating + field.toString());
			}
		}	
		return result;
	}
	
	/* LOGIC 3:
	 * Eigene ANT frueh im Spiel zu haben ist von Vorteil, da diese schnell
	 * gegenrische Steine blocken können
	 */
	public static RateHelper logic3CountOwnAnts(RateHelper rate, Field field) {
		RateHelper result = new RateHelper(rate);
		List<Piece> pieces = field.getPieces();
		if (field.getPieces().get(pieces.size()-1).getType() == PieceType.ANT) {
			int rating = 3;
			result.value += rating;
			result.rate.add("  Count Own Ants: " + rating + field.toString());
		}
		return result;
	}
	
	/* LOGIC 4:
	 * Wenn unser BUG auf der gegnerische BEE sitzt, ist das super, dann koennen
	 * wir schneller surrounden
	 */
	public static RateHelper logic4StepOnQueen(RateHelper rate, Field field) {
		RateHelper result = new RateHelper(rate);
		List<Piece> pieces = field.getPieces();
		if (pieces.size() > 1) {
			for (int i = 0; i < pieces.size() - 1; i++) {
				if (pieces.get(i).getType() == PieceType.BEE) {
					if (pieces.get(i).getOwner() != pieces.get(pieces.size() - 1).getOwner()) {
						int rating = 10;
						result.value += rating;
						result.rate.add("  Step On Queen: " + rating + field.toString());
					}
				}
			}			
		}
		return result;		
	}
	
	/* LOGIC 5:
	 * Wenn unsere ANT fremde Viecher blockiert, ist cool!
	 * Je mehr blockiert werden, desto besser...
	 */
	public static RateHelper logic5BlockBugs(RateHelper rate, Board board, Field field) {
		RateHelper result = new RateHelper(rate);
		List<Piece> pieces = field.getPieces();
		FieldState own = field.getFieldState();
		if (pieces.get(pieces.size()-1).getType() != PieceType.BEE) {
			List<Field> neighbors = Lib.getNeighbours(board, field);
			for (Field neighbor : neighbors) {
				// Bei eigenen Kaefern, blockieren wir evtl nicht so viel
				if (neighbor.getFieldState() == own) {
					return result;
				}
				if (neighbor.getFieldState() == Lib.opponentFieldState(own)) {
					int rating = 10;
					result.value += rating;
					result.rate.add("  Block Bugs: " + rating + field.toString());
					return result;
				}
			}
		}
		return result;
	}
}