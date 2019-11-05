package sc.player2020.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sc.framework.plugins.Player;
import sc.player2020.Starter;
import sc.plugin2020.DragMove;
import sc.plugin2020.GameState;
import sc.plugin2020.IGameHandler;
import sc.plugin2020.Move;
import sc.plugin2020.Piece;
import sc.plugin2020.PieceType;
import sc.plugin2020.SetMove;
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
  private static final Logger log = LoggerFactory.getLogger(BeatMeBot.class);

  private Starter client;
  private GameState gameState;
  private Player currentPlayer;
  
  private Move bestMove;

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
    
    int turn = gameState.getTurn();

    Lib.pln("", Consts.PRINT_NEW_ROUND);
    Lib.pln("* * * * * * * * * * * *", Consts.PRINT_NEW_ROUND);
    Lib.pln("* N e u e   R u n d e *", Consts.PRINT_NEW_ROUND);
    Lib.pln("* * * * * * * * * * * *", Consts.PRINT_NEW_ROUND);
    Lib.pln("", Consts.PRINT_NEW_ROUND);
    Lib.pln("  Turn: " + turn, Consts.PRINT_ROUND_INFO);
    
    // TURN 0 and 1
    if (turn < 2) {
    	CubeCoordinates beeMove = Lib.findMove(gameState, turn);
    	Piece bee = new Piece(gameState.getCurrentPlayerColor(), PieceType.BEE);
    	bestMove = new SetMove(bee, beeMove);
    	
    // ALPHA BETA
    } else {
    	
    	// default Move...
        List<Move> possibleMoves = GameRuleLogic.getPossibleMoves(gameState);
		bestMove = possibleMoves.get((int) (Math.random() * possibleMoves.size()));
		
		try {
			simpleAlphaBeta(Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, Consts.ALPHABETA_DEPTH);
		} catch (InvalidGameStateException e) {
			e.printStackTrace();
		} catch (InvalidMoveException e) {
			e.printStackTrace();
		}
	    
    }
    
    // Board ausgeben
    if (Consts.PRINT_BOARD) {
    	Lib.printBoard(gameState.getBoard());
    }
    
    Lib.pln("  BestMove: ", Consts.PRINT_MOVE);
	sendAction(bestMove);
	
	final long timeEnd = System.currentTimeMillis();
	Lib.pln("  Lauftzeit: " + (timeEnd - timeStart) + "ms.", Consts.PRINT_TIME);
	
  }
  
	private int simpleAlphaBeta(int alpha, int beta, int deep) throws InvalidGameStateException, InvalidMoveException {

		//ArrayList<Move> moves;
		List<Move> moves;
		GameState g = gameState;

		if (deep == 0 || gameEnded()) {
			this.gameState = g;
			return rateSimpleAlphaBeta();
		}

		moves = GameRuleLogic.getPossibleMoves(gameState);
		if (moves.isEmpty()) {
			this.gameState = g;
			return rateSimpleAlphaBeta();
		}

		for (Move move : moves) {
			int value;
			try {
				Lib.pln("Teste Zug: " + move.toString(), Consts.PRINT_ALPHABETA);
				g = this.gameState.clone();
				GameRuleLogic.performMove(gameState, move);
				Lib.pln("Zug performed", Consts.PRINT_ALPHABETA);
				// gameState.prepareNextTurn(move);
				if (gameState.getCurrentPlayerColor() == g.getCurrentPlayerColor()) {
					value = simpleAlphaBeta(alpha, beta, deep - 1);
				} else {
					value = simpleAlphaBeta(-beta, -alpha, deep - 1);
				}
				if (value >= beta) {
					this.gameState = g;
					return beta;
				}
				if (value > alpha) {
					alpha = value;
					if (deep == Consts.ALPHABETA_DEPTH) {
						bestMove = move;
					}
				}
				gameState = g;
				
				
			} catch (InvalidGameStateException | InvalidMoveException e) {
				e.printStackTrace();
				System.out.println(e.getClass().getSimpleName() + move);
				gameState = g;			
			} catch (IndexOutOfBoundsException e) {
				throw e;
			} finally {
				gameState = g;
			}

		}
		this.gameState = g;
		return alpha;
	}

	private int rateSimpleAlphaBeta() {

		// da spielertausch bei prepareNextTurn
		PlayerColor opponent = this.gameState.getCurrentPlayer().getColor();
		PlayerColor current = this.gameState.getOtherPlayer().getColor();
		int ownPoints = this.gameState.getPointsForPlayer(current);
		int oppPoints = this.gameState.getPointsForPlayer(opponent);

		int value = (2 * ownPoints) - oppPoints;

		return value;
	}

	private boolean gameEnded() {
		return (this.gameState.getTurn() == Constants.ROUND_LIMIT);
	}

  /**
   * {@inheritDoc}
   */
  @Override
  public void onUpdate(Player player, Player otherPlayer) {
    currentPlayer = player;
    // log.info("Spielerwechsel: " + player.getColor());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onUpdate(GameState gameState) {
    this.gameState = gameState;
    currentPlayer = gameState.getCurrentPlayer();
    // log.info("Zug: {} Spieler: {}", gameState.getTurn(), currentPlayer.getColor());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendAction(Move move) {
    client.sendMove(move);
  }

}
