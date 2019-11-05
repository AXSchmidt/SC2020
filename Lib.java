package sc.player2020.logic;

import java.util.List;

import sc.plugin2020.Board;
import sc.plugin2020.Field;
import sc.plugin2020.FieldState;
import sc.plugin2020.GameState;
import sc.plugin2020.Move;
import sc.plugin2020.Piece;
import sc.plugin2020.PieceType;
import sc.plugin2020.SetMove;
import sc.shared.PlayerColor;
import sc.plugin2020.util.CubeCoordinates;
import sc.plugin2020.util.Direction;
import sc.plugin2020.util.GameRuleLogic;;

public class Lib {
	
	public static void pln(String str, Boolean print) {
		if (print) {
			System.out.println(str);
		}
	}
	
	private static int getDistance(CubeCoordinates p1, CubeCoordinates p2) {
		int distance = 0;
		// P1 lieft weiter "oben"
		if (p1.getZ() < p2.getZ()) {
			distance = p2.getZ() - p1.getZ() + 
					Math.max(
							Math.max(0, p2.getX() - p1.getX()),
							Math.max(0, p2.getY() - p1.getY())
							);
		} else {
			// P2 lieft weiter "oben"
			distance = p1.getZ() - p2.getZ() + 
					Math.max(
							Math.max(0, p1.getX() - p2.getX()),
							Math.max(0, p1.getY() - p2.getY())
							);			
		}
		return distance;
	}
	
	private static int distanceToBerry(Board board, CubeCoordinates field) {
		int distance = 11;
		// Alle Zeilen durchlaufen...
		for (int row = 0; row < 11; row++) {
			// Alle Spalten durchrattern...
			for (int col = 0; col < 11; col++) {
				// x, y, z berechnen
				int x = (int) (col - 2 - Math.round((row + 1) / 2)); 
				int z = row - 5;
				int y = -1 * (x + z);
				// Nur suchen, wenn Field auf Feld liegt
				if ((x >= -5) && (x <= 5) && (y >= -5) && (y <= 5)) {
					// Beeren suchen
					if (board.getField(x, y, z).getFieldState() == FieldState.OBSTRUCTED) {
						int newDistance = getDistance(field, new CubeCoordinates(x, y, z));
						if (newDistance < distance) {
							distance = newDistance;
						}
					}
				} 
			} // of for row
		} // of for col		
		return distance;
	}
	
	private static int distanceToBorder(CubeCoordinates field) {
		int distanceZ = (5 - Math.abs(field.getZ()));
		int distanceY = (5 - Math.abs(field.getY()));
		int distanceX = (5 - Math.abs(field.getX()));
		int distanceLR = Math.min(distanceX, distanceY);
		return Math.min(distanceLR, distanceZ);
	}
	
	private static CubeCoordinates findFirstMove(Board board) {		
		CubeCoordinates freeField = new CubeCoordinates(0, 5, -5);
		int maxDistance = 0;
		// Alle Zeilen durchlaufen...
		for (int row = 0; row < 11; row++) {
			// Alle Spalten durchrattern...
			for (int col = 0; col < 11; col++) {
				// x, y, z berechnen
				int x = (int) (col - 2 - Math.round((row + 1) / 2)); 
				int z = row - 5;
				int y = -1 * (x + z);
				// Nur suchen, wenn Field auf Feld liegt
				if ((x >= -5) && (x <= 5) && (y >= -5) && (y <= 5)) {
					CubeCoordinates field = new CubeCoordinates(x, y, z);
					int newDistance = Math.min(distanceToBorder(field),
							distanceToBerry(board, field));
					if (newDistance > maxDistance) {
						freeField = new CubeCoordinates(x, y, z);
						maxDistance = newDistance;
					}
				} // possible Field
			} // of for row
		} // of for col
		return freeField;
	}
	
	private static CubeCoordinates findSecondMove(GameState gameState) {
		Board board = gameState.getBoard();
		CubeCoordinates freeField = new CubeCoordinates(0, 5, -5);
		int maxDistance = 0;
		
		List<Move> possibleMoves = GameRuleLogic.getPossibleMoves(gameState);
		for (Move move : possibleMoves) {
			// An dieser Stelle dürfte es nur SetMoves geben
			SetMove setMove = (SetMove) move;
			if (setMove.getPiece().getType() == PieceType.BEE) {
				CubeCoordinates bee = setMove.getDestination();
				int newDistance = Math.min(distanceToBorder(bee),
						distanceToBerry(board, bee)); 
				if (newDistance > maxDistance) {
					freeField = setMove.getDestination();
					maxDistance = newDistance;
				}				
			}
		}
		return freeField;
	}
	
	public static CubeCoordinates findMove(GameState gameState, int turn) {
		switch(turn){
        case 0:
            return findFirstMove(gameState.getBoard());
        case 1:
        	return findSecondMove(gameState);
        default:
        	pln("DEBUG; wrong turn: " + turn, Consts.PRINT_ERROR);
            return null;
        } 		
	}
	
	public static void printBoard(Board board) {
		// Alle Zeilen durchlaufen...
		for (int row = 0; row < 11; row++) {
			String strRow1 = "";
			String strRow2 = "";
			// Alle Spalten durchrattern...
			for (int col = 0; col < 11; col++) {
				// x, y, z berechnen
				int x = (int) (col - 2 - Math.round((row + 1) / 2)); 
				int z = row - 5;
				int y = -1 * (x + z);
				// Nur zweichnen, wenn Field im Hexagon liegt
				if ((x >= -5) && (x <= 5) && (y >= -5) && (y <= 5)) {
					String type = " " + board.getField(x, y, z).getFieldState().toString().substring(0, 1);
					
					CubeCoordinates field = new CubeCoordinates(x, y, z);
					String distance = " " + Math.min(distanceToBorder(field),
							distanceToBerry(board, field));
					
					if (row %2 == 0) { // gerade Zeile
						strRow1 += "    " + String.format("% 2d", x) + String.format("% 2d", z); 
						strRow2 += "    " + String.format("% 2d", y) + distance;
					} else { // ungerade
						strRow1 += String.format("% 2d", x) + String.format("% 2d", z) + "    "; 
						strRow2 += String.format("% 2d", y) + distance + "    ";
					}
				} else { // Auﬂen-/Randfelder
					strRow1 += "        "; 
					strRow2 += "        ";				
				}
			} // of for row
			System.out.println(strRow1);
			System.out.println(strRow2);
		} // of for col
	}
}
