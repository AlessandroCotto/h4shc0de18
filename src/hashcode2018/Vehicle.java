package hashcode2018;

import java.util.ArrayList;

/**
 * classe che gestisce veicolo
 * @author federicoballarini
 *
 */
public class Vehicle {
	int id;
	Position p;
	
	Ride r;
	int distanceToMake=0;
	
	int distanceToRide;
	
	ArrayList<Integer> rideDone= new ArrayList<>();
	
	/**
	 * setter for ride
	 * @param _r
	 */
	public void setRide(Ride _r) {
		this.r= _r;
		//distance to arrive to start + distance to complete -1 because first turn move 
		this.distanceToMake= Position.difference(_r.startPosition, this.p) + Position.difference(_r.startPosition, _r.finishPosition) -1;
	}
}
