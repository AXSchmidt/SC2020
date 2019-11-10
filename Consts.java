package sc.player2020.logic;

public class Consts {
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
	
	/* LOGIC 2:
	 * Die gegenerische BEE zu umzingeln ist super 
	 */
	
	/* LOGIC 3:
	 * Eigene ANT frueh im Spiel zu haben ist von Vorteil, da diese schnell
	 * gegenrische Steine blocken können
	 */
	public static int OWN_ANT = 3;
	
	/* LOGIC 4:
	 * Wenn unser BUG auf der gegnerische BEE sitzt, ist das super, dann koennen
	 * wir schneller surrounden
	 */
}
