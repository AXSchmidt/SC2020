package sc.player2020.logic;

import sc.framework.plugins.Player;
import sc.player2020.Starter;
import sc.plugin2020.GameState;
import sc.plugin2020.IGameHandler;
import sc.plugin2020.Board;
import sc.plugin2020.Field;
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
  private String bestValue;
  private int bestNo;
  private List<String> moveRating = new ArrayList<String>();
  private List<String> bestMoveRating = new ArrayList<String>();
  private String[] alphaBetaMoveList = new String[Helper.ALPHABETA_DEPTH];
  private List<String> outPut = new ArrayList<String>();
  private long timeAlphaBeta;
  private boolean timeOut = false;

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
    // time for TimeOut
	final long timeStart = System.currentTimeMillis();
	this.timeAlphaBeta = timeStart;
	
    outPut.clear();

    Lib.printHeader(gameState.getTurn(), Helper.PRINT_HEADER);

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
			alphaBeta(Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, Helper.ALPHABETA_DEPTH);
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
		// timeOut
		if (this.timeOut) {
			return alpha;
		}
		// Abbruchkriterium
		if ((tiefe == 0) || endOfGame()) {
			int value = rateAlphaBeta();
			if (Helper.PRINT_ALPHABETA_HEADER) {
				outPut.add("");
				outPut.add("***N*E*W***M*O*V*E***");
				outPut.add("Value: " + value + " - Tiefe: " + Helper.ALPHABETA_DEPTH + " - Aufrufe: " + aufrufe + " - Turn: "
						+ gameState.getTurn() + " - Round: " + gameState.getRound());
			}
			if (Helper.PRINT_ALPHABETA_SHOWMOVES) {
				for (String moveStr : alphaBetaMoveList) {
					outPut.add(moveStr);
				}
			}
			return value;
		}
		boolean PVgefunden = false;
		int best = Integer.MIN_VALUE + 1;
		List<Move> moves = GameRuleLogic.getPossibleMoves(gameState);
		if (moves.size() == 0) {
			//bestMove = new SkipMove(); // TODO Constructor is private?
		}
		
		// timeOut
		if (System.currentTimeMillis() - this.timeAlphaBeta >= Helper.TIMEOUTTIME) {
			System.out.println(System.currentTimeMillis() - this.timeAlphaBeta + " Time Out");
			System.out.println(this.bestMove.toString());
			this.timeOut = true;
			return 0;
		}

		for (Move move : moves) {
			alphaBetaMoveList[Helper.ALPHABETA_DEPTH - tiefe] = move.toString();
			GameState g = this.gameState.clone();
			GameRuleLogic.performMove(this.gameState, move);
			int wert;
			if (PVgefunden) {
				wert = -alphaBeta(-alpha - 1, -alpha, tiefe - 1);
				if ((wert > alpha) && (wert < beta)) {
					wert = -alphaBeta(-beta, -wert, tiefe - 1);
				}
			} else {
				wert = -alphaBeta(-beta, -alpha, tiefe - 1);
			}
			this.gameState = g; // ?
			if (wert > best) {
				if (wert >= beta) {
					return wert;
				}
				best = wert;
				if (tiefe == Helper.ALPHABETA_DEPTH) {
					bestMove = Lib.copyMove(move);
					bestValue = String.valueOf(wert);
					bestMoveRating.clear();
					bestMoveRating.addAll(moveRating);
					bestNo = aufrufe;
					outPut.add("NEW BEST MOVE: " + bestMove.toString() + " Value: " + best);
				}

				if (wert > alpha) {
					alpha = wert;
					PVgefunden = true;
				}
			}
			// timeOut
			if (this.timeOut) {
				return alpha;
			}
		}
		return best;
	}

	private int rateAlphaBeta() {
		printAlphaBetaBoard();
		
		RateHelper rateHelper = new RateHelper();

		PlayerColor current = this.gameState.getCurrentPlayer().getColor();
		if (Helper.ALPHABETA_DEPTH % 2 != 0) {
			current = this.gameState.getCurrentPlayer().getColor().opponent();
		}
		PlayerColor opponent = current.opponent();
		outPut.add("You: "+current.toString());

		List<Field> fieldList = Lib.getAllFields(this.gameState.getBoard());
		for (Field field : fieldList) {
			
			// Eigene Insekten
			if (field.getFieldState().toString() == current.toString()) {
				rateHelper.current = true;
				rateHelper = Helper.logic1OwnQueenSurround(rateHelper, this.gameState.getBoard(), field);
				rateHelper = Helper.logic3CountOwnAnts(rateHelper, field);
				rateHelper = Helper.logic4StepOnQueen(rateHelper, field);
				rateHelper = Helper.logic5BlockBugs(rateHelper, this.gameState.getBoard(), field);
				}
			
			// Gegnerische Mückenplage	
			if (field.getFieldState().toString() == opponent.toString()) {
				rateHelper.current = false;
				rateHelper = Helper.logic2OpponentQueenSurround(rateHelper, this.gameState.getBoard(), field);
				rateHelper = Helper.logic4StepOnQueen(rateHelper, field);
				rateHelper = Helper.logic5BlockBugs(rateHelper, this.gameState.getBoard(), field);
				}
		} // end of fieldList
		
		if (Helper.ALPHABETA_DEPTH % 2 != 0) {
			rateHelper.value = -rateHelper.value;
		}
		moveRating.clear();
		moveRating.addAll(rateHelper.rate);
		return rateHelper.value;
	}

	private boolean endOfGame() {
		// TODO Es muss noch abgefragt werden, ob ein Spieler gewonnen hat.
		// (Bienenkoenigin umzingelt)
		return (this.gameState.getRound() == Constants.ROUND_LIMIT);
	}
	
	private void printAlphaBetaBoard() {
		if (Helper.PRINT_APLHABETA_SHOWBOARD) {
			List<String> boardList = Lib.printBoard(this.gameState.getBoard());
			for (String s : boardList) {
				outPut.add(s);
			}
		}
	}
	

	private void printSummary(long timeStart) {
		// Gesamtausgabe der AlphaBeta Logic ausgeben
		if (Helper.PRINT_ALPHABETA) {
			for (String s : outPut) {
				System.out.println(s);
			}
		}
		// Zugzusammenfassung ausgeben
		if (Helper.PRINT_FOOTER) {
			final long timeEnd = System.currentTimeMillis();
			Lib.pln("", true);
			Lib.pln("***S*U*M*M*A*R*Y***", true);
			Lib.pln("  Best Move: " + bestMove.toString() + " - Value: " + bestValue, true);
			for (String moveRating: bestMoveRating) {
				Lib.pln(moveRating, true);
			}
			Lib.pln("  Aufruf: " + bestNo, true);
			Lib.pln("  Punkte Rot: " + this.gameState.getPointsForPlayer(PlayerColor.RED), true);
			Lib.pln("  Punkte Blau: " + this.gameState.getPointsForPlayer(PlayerColor.BLUE	), true);
			Lib.pln("  Lauftzeit: " + (timeEnd - timeStart) + "ms. Suchtiefe " + Helper.ALPHABETA_DEPTH + " Aufrufe " + aufrufe, true);
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
