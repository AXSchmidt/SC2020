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
	public static int logic1OwnQueenSurround(Board board, Field field) {
		if (Lib.fieldContainsPiece(field, PieceType.BEE)) {
			int beeNeighbors = 0;
			List<Field> beeList = Lib.getNeighbours(board, field);
			for (Field beeGuard : beeList) {
				if (beeGuard.getFieldState() != FieldState.EMPTY) {
					beeNeighbors++;
				}
			}
			if (beeNeighbors > 2) {
				return (int) Math.pow(2, beeNeighbors);
			}
		}	
		return 0;
	}
	
	/* LOGIC 2:
	 * Die gegenerische BEE zu umzingeln ist super 
	 */
	public static int logic2OpponentQueenSurround(Board board, Field field) {
		if (Lib.fieldContainsPiece(field, PieceType.BEE)) {
			int beeNeighbors = 0;
			List<Field> beeList = Lib.getNeighbours(board, field);
			for (Field beeGuard : beeList) {
				if (beeGuard.getFieldState() != FieldState.EMPTY) {
					beeNeighbors++;
				}
			}
			if (beeNeighbors > 2) {
				return (int) Math.pow(2, beeNeighbors + 1);
			}
		}	
		return 0;
	}
	
	/* LOGIC 3:
	 * Eigene ANT frueh im Spiel zu haben ist von Vorteil, da diese schnell
	 * gegenrische Steine blocken können
	 */
	public static int logic3CountOwnAnts(Field field) {
		List<Piece> pieces = field.getPieces();
		if (field.getPieces().get(pieces.size()-1).getType() == PieceType.ANT) {
			return 3;
		}
		return 0;
	}
	
	/* LOGIC 4:
	 * Wenn unser BUG auf der gegnerische BEE sitzt, ist das super, dann koennen
	 * wir schneller surrounden
	 */
	public static int logic4StepOnQueen(Board board, Field field) {
		List<Piece> pieces = field.getPieces();
		if (pieces.size() > 1) {
			for (int i = 0; i < pieces.size() - 1; i++) {
				if (pieces.get(i).getType() == PieceType.BEE) {
					if (pieces.get(i).getOwner() != pieces.get(pieces.size() - 1).getOwner()) {
						return 10;
					}
				}
			}			
		}
		return 0;		
	}
	
	/* LOGIC 5:
	 * Wenn unsere ANT fremde Viecher blockiert, ist cool!
	 * Je mehr blockiert werden, desto besser...
	 */
}
