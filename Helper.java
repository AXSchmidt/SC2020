package sc.player2020.logic;

import sc.plugin2020.PieceType;

import java.util.List;

import sc.plugin2020.Board;
import sc.plugin2020.Field;
import sc.plugin2020.FieldState;
import sc.plugin2020.Piece;

public class Helper {
	
	// GLOBAL Constants
	public static int ALPHABETA_DEPTH = 1;	
	public static int TIMEOUTTIME = 1860;
	
	// PRINT Constants
	public static boolean PRINT_ERROR = true;
	public static boolean PRINT_HEADER = true;
	public static boolean PRINT_BOARD = false;
	public static boolean PRINT_FOOTER = true;	
	
	public static boolean PRINT_ALPHABETA = true;
	public static boolean PRINT_ALPHABETA_SHOWNEWMOVE = false;
	public static boolean PRINT_ALPHABETA_SHOWMOVES = false;
	public static boolean PRINT_APLHABETA_SHOWBOARD = false;
	
	
	/************************************************
	 *     L * O * G * I * C *                      *
	 *                                              *
	 * L0: Turn 1 wird BEE gesetzt (ohne AlphaBeta) *
	 * L1: Wie doll wird BEE umzingelt?             *
	 * L2: Zaehle eigene Ameisen (Ameisen gut)      *
	 * L3: Trete mit BEETLE auf BEE (super)         *
	 * L4: Blocke gegnerische BUGS                  *
	 *                                              *
	 ************************************************/
	
	/* LOGIC 0:
	 * Ich setze gleich im ersten Zug (egal ob ich anfange oder nicht) meine BEE,
	 * damit ich moeglichst schnell meine ANTs mobil bekomme
	 */
	
	
	/* LOGIC 1:
	 * BEE umzingeln ist super, solange es nicht die eigene ist!
	 */
	public static RateHelper logic1QueenSurround(RateHelper rate, Board board, Field field) {
		RateHelper result = new RateHelper(rate);
		List<Piece> pieces = field.getPieces();
		PieceType blockerBug = pieces.get(pieces.size()-1).getType();
		if (Lib.fieldContainsPiece(field, PieceType.BEE)) {
			int beeNeighbors = 0;
			List<Field> beeList = Lib.getNeighbours(board, field);
			for (Field beeGuard : beeList) {
				if (beeGuard.getFieldState() != FieldState.EMPTY) {
					beeNeighbors++;
				}
			}
			if (beeNeighbors > 2) {
				int rating = -rate.isOwn * (int) Math.pow(2, beeNeighbors + (int) ((rate.isOwn + 1) / 2));
				result.value += rating;
				result.rateStr.add("L1 - Queen Surround: " + rating + " " + field.toString());
			}
		}	
		return result;
	}
	
	/* LOGIC 2:
	 * Eigene ANT frueh im Spiel zu haben ist von Vorteil, da diese schnell
	 * gegenrische Steine blocken können
	 */
	public static RateHelper logic2CountOwnAnts(RateHelper rate, Field field) {
		RateHelper result = new RateHelper(rate);
		List<Piece> pieces = field.getPieces();
		if (pieces.get(pieces.size()-1).getType() == PieceType.ANT) {
			int rating = 3 * rate.isOwn;
			result.value += rating;
			result.rateStr.add("L2 - Count Own Ants: " + rating + " " + field.toString());
		}
		return result;
	}
	
	/* LOGIC 3:
	 * Wenn unser BEETLE auf der gegnerische BEE sitzt, ist das super, dann koennen
	 * wir schneller surrounden
	 */
	public static RateHelper logic3StepOnQueen(RateHelper rate, Field field) {
		RateHelper result = new RateHelper(rate);
		List<Piece> pieces = field.getPieces();
		if (pieces.size() > 1) {
			for (int i = 0; i < pieces.size() - 1; i++) {
				if (pieces.get(i).getType() == PieceType.BEE) {
					if (pieces.get(i).getOwner() != pieces.get(pieces.size() - 1).getOwner()) {
						int rating = 10 * rate.isOwn;
						result.value += rating;
						result.rateStr.add("L3 - Step On Queen: " + rating + " " + field.toString());
					}
				}
			}			
		}
		return result;		
	}
	
	/* LOGIC 4:
	 * Wenn unsere ANT fremde Viecher blockiert, ist cool!
	 * Je mehr blockiert werden, desto besser...
	 */
	public static RateHelper logic4BlockBugs(RateHelper rate, Board board, Field field) {
		// necessary stuff
		RateHelper result = new RateHelper(rate);
		List<Piece> pieces = field.getPieces();
		FieldState own = field.getFieldState();
		int rating = 0;
		String rateStr = "";
		// Wir wollen alles blocken (außer BEE, das ist ja schon Logic 1/2)
		if (pieces.get(pieces.size()-1).getType() != PieceType.BEE) {
			List<Field> neighbors = Lib.getNeighbours(board, field);
			int neighborHood = 0;
			for (Field neighbor : neighbors) {
				// Wenn eigene Kaefer in der Naehe sind, bringt blocken nichts
				if (neighbor.getFieldState() == own) {
					return result;
				}
				if (neighbor.getFieldState() == Lib.opponentFieldState(own)) {
					// Bei mehr als einem zu blockierenden Stein, lohnt es nicht so richtig
					if (neighborHood > 0) {
						return result;
					}
					neighborHood++;
					int multi = 0;
					PieceType oppType = neighbor.getPieces().get(pieces.size()-1).getType();
					switch(oppType) {
					case ANT:
						multi = 16;
						break;
					case BEE:
						multi = 7;
						break;
					default:
						multi = 3;
						break;
					}
				    rating = multi * rate.isOwn;
				    rateStr = "L4 - Block Bug " + oppType.toString() + ": " + rating + " " + field.toString();
				}
			}
		}
		result.value += rating;
		if (rateStr != "") {
			result.rateStr.add(rateStr);
		}
		return result;
	}
}