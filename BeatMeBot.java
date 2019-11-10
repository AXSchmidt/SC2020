package sc.player2020.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sc.framework.plugins.Player;
import sc.player2020.Starter;
import sc.plugin2020.DragMove;
import sc.plugin2020.GameState;
import sc.plugin2020.IGameHandler;
import sc.plugin2020.Field;
import sc.plugin2020.FieldState;
import sc.plugin2020.Move;
import sc.plugin2020.SetMove;
import sc.plugin2020.Piece;
import sc.plugin2020.PieceType;
import sc.plugin2020.util.CubeCoordinates;
import sc.plugin2020.util.GameRuleLogic;
import sc.plugin2020.util.Constants;
import sc.shared.GameResult;
import sc.shared.InvalidGameStateException;
import sc.shared.InvalidMoveException;
import sc.shared.PlayerColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Das Herz des Clients:
 * Eine sehr simple Logik, die ihre Zuege zufaellig waehlt,
 * aber gueltige Zuege macht.
 * Ausserdem werden zum Spielverlauf Konsolenausgaben gemacht.
 */
public class BeatMeBot implements IGameHandler {
  
  private Starter client;
  private GameState gameState;
  private Player currentPlayer;
  
  private int aufrufe;
  private Move bestMove;
  private String[] alphaBetaMoveList = new String[Consts.ALPHABETA_DEPTH];
  private List<String> outPut = new ArrayList<String>();

  /**
   * Erzeugt ein neues Strategieobjekt, das zufaellige Zuege taetigt.
   *
   * @param client Der zugrundeliegende Client, der mit dem Spielserver kommuniziert.
   */
  public BeatMeBot(Starter client) {
    this.client = client;
  }

  /**
   * {@inheritDoc}
   */
  public void gameEnded(GameResult data, PlayerColor color, String errorMessage) {
    //log.info("Das Spiel ist beendet.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onRequestAction() {
    
    final long timeStart = System.currentTimeMillis();
    outPut.clear();

    Lib.printHeader(gameState.getTurn(), Consts.PRINT_HEADER);

    if (gameState.getTurn() < 2) {
    	setBee(gameState.getTurn());
    } else {
    	startAlphaBeta();
    }
    
	sendAction(bestMove);

	printSummary(timeStart);
	
  }
  
  private void setBee(int turn) {
  	CubeCoordinates beeMove = Lib.findMove(gameState, turn);
  	Piece bee = new Piece(gameState.getCurrentPlayerColor(), PieceType.BEE);
  	bestMove = new SetMove(bee, beeMove);	  
  }
  
	private void startAlphaBeta() {
		outPut.add("  Start AlphaBeta_");
		aufrufe = 0;
		boolean error = false;

		try {
			// Eigentlicher Aufruf der Alphabeta
			alphaBeta(Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, Consts.ALPHABETA_DEPTH);
		} catch (InvalidGameStateException e) {
			error = true;
			e.printStackTrace();
		} catch (InvalidMoveException e) {
			error = true;
			e.printStackTrace();
		}
		// ERROR
		if (error) {
			if (bestMove.equals(null) == true) {
				List<Move> possibleMoves = GameRuleLogic.getPossibleMoves(gameState);
				bestMove = possibleMoves.get((int) (Math.random() * possibleMoves.size()));
				outPut.add("  KRRR - AlphaBetaError! rnd Move: " + bestMove.toString());
			}
		}
	}

	private int alphaBeta(int alpha, int beta, int tiefe) throws InvalidGameStateException, InvalidMoveException {
		++aufrufe;
		// Abbruchkriterium
		if ((tiefe == 0) || endOfGame()) {
			int value = rateAlphaBeta();
			if (Consts.PRINT_ALPHABETA_HEADER) {
				outPut.add("");
				outPut.add("***N*E*W***M*O*V*E***");
				outPut.add("Value: " + value + " - Tiefe: " + Consts.ALPHABETA_DEPTH + " - Aufrufe: " + aufrufe + " - Turn: "
						+ gameState.getTurn() + " - Round: " + gameState.getRound());
			}
			if (Consts.PRINT_ALPHABETA_SHOWMOVES) {
				for (String moveStr : alphaBetaMoveList) {
					outPut.add(moveStr);
				}
			}
			return value;
		}
		boolean PVgefunden = false;
		int best = Integer.MIN_VALUE + 1;
		List<Move> moves = GameRuleLogic.getPossibleMoves(gameState);
		// TODO Abbruchkriterium wenn Liste = leer? => SkipMove

		for (Move move : moves) {
			alphaBetaMoveList[Consts.ALPHABETA_DEPTH - tiefe] = move.toString(); //+ " "
					//+ this.gameState.getBoard().getField(move.x, move.y).getState().toString();
			GameState g = this.gameState.clone();
			GameRuleLogic.performMove(this.gameState, move);
			int wert;
			if (PVgefunden) {
				wert = -alphaBeta(-alpha - 1, -alpha, tiefe - 1);
				if (wert > alpha && wert < beta)
					wert = -alphaBeta(-beta, -wert, tiefe - 1);
			} else
				wert = -alphaBeta(-beta, -alpha, tiefe - 1);
			this.gameState = g;
			if (wert > best) {
				if (wert >= beta)
					return wert;
				best = wert;
				if (tiefe == Consts.ALPHABETA_DEPTH) {
					// ZUG KOPIEREN? GEHT DAS SO?
					if (move.toString().substring(0, 1).equals("S")) { // SetMove
						SetMove setMove = (SetMove) move;
						bestMove = new SetMove(setMove.getPiece(), move.getDestination());
					} else {
						DragMove dragMove = (DragMove) move;
						bestMove = new DragMove(dragMove.getStart(), move.getDestination());
					}
					outPut.add("NEW BEST MOVE: " + bestMove.toString() + " Value: " + best);
				}
				if (wert > alpha) {
					alpha = wert;
					PVgefunden = true;
				}
			}
		}
		return best;
	}

	private int rateAlphaBeta() {
		printAlphaBetaBoard();

		int value = 0;
		PlayerColor current = this.gameState.getCurrentPlayer().getColor();
		PlayerColor opponent = current.opponent();

		List<Field> fieldList = Lib.getAllFields(this.gameState.getBoard());
		for (Field field : fieldList) {
			// Eigene Insekten
			if (field.getFieldState().toString() == current.toString()) {
				// Ameisenplage ist immer gut
				if (field.getPieces().get(0).getType() == PieceType.ANT) {
					value++;
				}
				// Bitte eigene Bienenkoenigin nicht surrounden
				if (field.getPieces().get(0).getType() == PieceType.BEE) {
					List<Field> beeList = Lib.getAdjacentFields(this.gameState.getBoard(), field);
					for (Field beeGuard : beeList) {
						if (beeGuard.getFieldState() != FieldState.EMPTY) {
							//value--;
						}
					}
				}
				// Eigener Mistkaefer auf gegnerischer Koenigin is geil!
			}
			// Gegnerische Mückenplage	
			if (field.getFieldState().toString() == opponent.toString()) {
				// Gegnerische Queen anbaggern is ok
				// TODO: Was wenn Queen nicht oben is
				if (field.getPieces().get(0).getType() == PieceType.BEE) {
					List<Field> beeList = Lib.getAdjacentFields(this.gameState.getBoard(), field);
					for (Field beeGuard : beeList) {
						if (beeGuard.getFieldState() != FieldState.EMPTY) {
							System.out.println("BEEGUARD: " + beeGuard.toString());
							value += 5;
						}
					}
				}
			}
		}
		return value;
	}

	private boolean endOfGame() {
		// TODO Es muss noch abgefragt werden, ob ein Spieler gewonnen hat.
		// (Bienenkoenigin umzingelt)
		return (this.gameState.getRound() == Constants.ROUND_LIMIT);
	}
	
	private void printAlphaBetaBoard() {
		if (Consts.PRINT_APLHABETA_SHOWBOARD) {
			List<String> boardList = Lib.printBoard(this.gameState.getBoard());
			for (String s : boardList) {
				outPut.add(s);
			}
		}
	}
	

	private void printSummary(long timeStart) {
		// Gesamtausgabe der AlphaBeta Logic ausgeben
		if (Consts.PRINT_ALPHABETA) {
			for (String s : outPut) {
				System.out.println(s);
			}
		}
		// Zugzusammenfassung ausgeben
		if (Consts.PRINT_FOOTER) {
			final long timeEnd = System.currentTimeMillis();
			Lib.pln("", true);
			Lib.pln("***S*U*M*M*A*R*Y***", true);
			Lib.pln("  Best Move: " + bestMove.toString(), true);
			Lib.pln("  Lauftzeit: " + (timeEnd - timeStart) + "ms. Suchtiefe " + Consts.ALPHABETA_DEPTH + " Aufrufe " + aufrufe, true);
		}
	}

  /**
   * {@inheritDoc}
   */
  @Override
  public void onUpdate(Player player, Player otherPlayer) {
    currentPlayer = player;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onUpdate(GameState gameState) {
    this.gameState = gameState;
    currentPlayer = gameState.getCurrentPlayer();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendAction(Move move) {
    client.sendMove(move);
  }

}
