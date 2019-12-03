package sc.player2020.logic;

import java.util.ArrayList;
import java.util.List;

import sc.plugin2020.Board;
import sc.plugin2020.DragMove;
import sc.plugin2020.Field;
import sc.plugin2020.FieldState;
import sc.plugin2020.GameState;
import sc.plugin2020.Move;
import sc.plugin2020.Piece;
import sc.plugin2020.PieceType;
import sc.plugin2020.SetMove;
import sc.plugin2020.util.CubeCoordinates;
import sc.plugin2020.util.GameRuleLogic;
import sc.shared.PlayerColor;;

public class Lib {
	
	public static void pln(String str, Boolean print) {
		if (print) {
			System.out.println(str);
		}
	}
	
	public static void printHeader(int turn, boolean print) {
	    pln("", print);
	    pln("", print);
	    pln("* * * * * * * * * * * * * *", print);
	    pln("* N e u e   R u n d e   " + turn + " *", print);
	    pln("* * * * * * * * * * * * * *", print);
	    pln("", print);
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
		// TODO: Distanz zum Rand = 1 (wenn direkt daneben), ABER:
		//       Distanz zur Beere = 0 (wenn direkt daneben)
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
        	pln("DEBUG; wrong turn: " + turn, Helper.PRINT_ERROR);
            return null;
        } 		
	}
	
	public static boolean isCoordinatesOnBoard(CubeCoordinates coordinates) {
		int x = coordinates.getX();
		int y = coordinates.getY();
		int z = coordinates.getZ();
		return ((x >= -5) && (x <= 5) && (y >= -5) && (y <= 5) && (z >= -5) && (z <= 5));
	}
	
	public static List<Field> getAllFields(Board board) {
		List<Field> outPut = new ArrayList<Field>();
		for (int row = 0; row < 11; row++) { // Alle Zeilen durchlaufen
			for (int col = 0; col < 11; col++) { // Spalten durchrattern
				int x = (int) (col - 2 - Math.round((row + 1) / 2)); 
				int z = row - 5;
				int y = -1 * (x + z);
				// Nur suchen, wenn Field auf Feld liegt
				if ((x >= -5) && (x <= 5) && (y >= -5) && (y <= 5)) {
					outPut.add(board.getField(x, y, z));
				} // possible Field
			} // of for row
		} // of for col
		return outPut;
		
	}
	
	public static List<Field> getNeighbours(Board board, Field field) {
		List<Field> outPut = new ArrayList<Field>();
		CubeCoordinates coordinates = field.getCoordinates();
		int x = coordinates.getX();
		int y = coordinates.getY();
		int z = coordinates.getZ();
		
		CubeCoordinates topleft = new CubeCoordinates(x, y+1, z-1);
		if (isCoordinatesOnBoard(topleft)) {
			outPut.add(board.getField(topleft));
		}
		CubeCoordinates topright = new CubeCoordinates(x+1, y, z-1);
		if (isCoordinatesOnBoard(topright)) {
			outPut.add(board.getField(topright));
		}
		CubeCoordinates right = new CubeCoordinates(x+1, y-1, z);
		if (isCoordinatesOnBoard(right)) {
			outPut.add(board.getField(right));
		}
		CubeCoordinates bottomright = new CubeCoordinates(x, y-1, z+1);
		if (isCoordinatesOnBoard(bottomright)) {
			outPut.add(board.getField(bottomright));
		}
		CubeCoordinates bottomleft = new CubeCoordinates(x-1, y, z+1);
		if (isCoordinatesOnBoard(bottomleft)) {
			outPut.add(board.getField(bottomleft));
		}
		CubeCoordinates left = new CubeCoordinates(x-1, y+1, z);
		if (isCoordinatesOnBoard(left)) {
			outPut.add(board.getField(left));
		}
		return outPut;
	}
	
	public static void printLongBoard(Board board, boolean print) {
		if (!print) {
			return;
		}
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
					//String type = " " + board.getField(x, y, z).getFieldState().toString().substring(0, 1);
					
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
	
	public static List<String> printBoard(Board board) {
		List<String> outPut = new ArrayList<String>();
		// Alle Zeilen durchlaufen...
		for (int row = 0; row < 11; row++) {
			String strRow = "";
			// Alle Spalten durchrattern...
			for (int col = 0; col < 11; col++) {
				// x, y, z berechnen
				int x = (int) (col - 2 - Math.round((row + 1) / 2)); 
				int z = row - 5;
				int y = -1 * (x + z);
				// Nur zweichnen, wenn Field im Hexagon liegt
				if ((x >= -5) && (x <= 5) && (y >= -5) && (y <= 5)) {
					String type = "";
					Field field = board.getField(x, y, z);
					switch(field.getFieldState().toString().substring(0, 1)) {
						case "B":
							type = getPieceType(field).toLowerCase();
							break;
						case "R": 
							type = getPieceType(field);
							break;
						case "O":
							type = "*";
							break;
						case "E":
							type = "_";
							break;
					}
					//type = field.getFieldState().toString().substring(0, 1);

					if (row %2 == 0) { // gerade Zeile
						strRow += " " + type; 
					} else { // ungerade
						strRow += type + " ";
					}
				} else { // Auﬂen-/Randfelder
					strRow += "  "; 				
				}
			} // of for row
			outPut.add(strRow);
		} // of for col
		return outPut;
	}
	
	private static String getPieceType(Field field) {
		List<Piece> pieces = field.getPieces();
		/* Auf dem field können mehrere pieces liegen.
		 * .get(0) ist immer der unterste
		 * .get(size-1) ist immer der oberste
		 */
		if (field.getPieces().get(pieces.size()-1).getType() == PieceType.BEE) {
			return "Q"; // BEE != "B" wegen BEETLE
		} else {
			return field.getPieces().get(pieces.size()-1).getType().toString().substring(0, 1);
		}
	}
	
	public static boolean fieldContainsPiece(Field field, PieceType pieceType) {
		List<Piece> pieces = field.getPieces();
		for (Piece piece: pieces) {
			if (piece.getType() == pieceType) {
				return true;
			}
		}
		return false;
	}
	
	public static FieldState opponentFieldState(FieldState fieldState) {
		switch(fieldState) {
		case BLUE:
			return FieldState.RED;
		case RED:
			return FieldState.BLUE;
		default:
			return fieldState;
		}
	}
	
	public static Move copyMove(Move move) {
		if (move.toString().substring(0, 1).equals("S")) { // SetMove
			SetMove setMove = (SetMove) move;
			return new SetMove(setMove.getPiece(), move.getDestination());
		} else { // DragMove
			DragMove dragMove = (DragMove) move;
			return new DragMove(dragMove.getStart(), move.getDestination());
		}
	}
	
	public static boolean isBeePlaced(List<Field> fieldList, PlayerColor playerColor) {
		for (Field field : fieldList) {
			if (field.getFieldState().toString() == playerColor.toString()) {
				List<Piece> pieces = field.getPieces();
				if (pieces.size() > 1) {
					for (int i = 0; i < pieces.size() - 1; i++) {
						if (pieces.get(i).getType() == PieceType.BEE) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
