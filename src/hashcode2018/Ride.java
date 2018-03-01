package hashcode2018;

/**
 * il percorso
 * @author federicoballarini
 *
 */
public class Ride {
	int id;
	Position startPosition;
	Position finishPosition;
	int earliestStart;
	int latestFinish;
	
	int realStart;
	
	int distanceRide;
	
	@Override
	public String toString() {
		return "ride "+id+" from ["+startPosition.x+", "+startPosition.y+"] to ["+finishPosition.x+", "+finishPosition.y+"], earliest start "+earliestStart+", latest finish "+latestFinish;
	}
	
}
