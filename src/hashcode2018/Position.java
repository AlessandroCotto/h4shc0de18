package hashcode2018;

/**
 * gestione posizione
 * @author federicoballarini
 *
 */
public class Position {
	int x;
	int y;
	
	/**
	 * @param p1
	 * @param p2
	 * @return la distanza tra 2 punti
	 */
	public static int difference(Position p1, Position p2) {
		int difference=Math.abs(p2.x - p1.x) + Math.abs(p2.y-p2.y);
		return difference;
	}
}
