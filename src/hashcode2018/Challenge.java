package hashcode2018;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * main class 
 * 
 * @author federicoballarini
 *
 */
public class Challenge {

	static int numRows;
	static int numColumns;
	static int numVehicles;
	static int numRides;
	static int bonusValue;
	static int numSteps;

	static List<Ride> listRides= new ArrayList<>();
	static List<Vehicle> listVehicles= new ArrayList<>();

	static Map<Integer, ArrayList<Ride>> mapAvailableRides= new HashMap<>();
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		Commons.initLog();
		long initTS= System.currentTimeMillis();
		System.out.println("Start!");
		List<String> l= Commons.readFile("/Users/federicoballarini/Downloads/a_example.in");
		//List<String> l= Commons.readFile("/Users/federicoballarini/Downloads/b_should_be_easy.in");
		//List<String> l= Commons.readFile("/Users/federicoballarini/Downloads/e_high_bonus.in");

		List<String> result= algorithm(l);

		//Commons.saveFile("/Users/federicoballarini/Downloads/giocatore2.csv", result);

		System.out.println("time elapsed: "+(System.currentTimeMillis()-initTS)+ " ms");
		Log.close();

	}

	static List<String> algorithm(List<String> list) {
		List<String> result= new ArrayList<>();

		String firstLine= list.get(0);
		analizeFirstLine(firstLine);
		System.out.println(numRows+" rows, "+numColumns+" columns, "+numVehicles+" vehicles, "+numRides+" rides, "+bonusValue+" bonus and "+numSteps+" steps");


		//init rides
		for(int i=1; i<list.size(); i++) {
			String s= list.get(i);
			Ride r= createRide(s);
			r.id= (i-1);
			System.out.println(r);
			listRides.add(r);
		}


		System.out.println("size rides:"+ listRides.size());
		//init vehicles
		for(int i= 0; i<numVehicles; i++) {
			Vehicle v= new Vehicle();
			Position p= new Position();
			p.x=0;
			p.y=0;
			v.p= p;
			v.id= i;
			v.r= null;
			listVehicles.add(v);
		}
		System.out.println("size vehicles:"+ listVehicles.size());


		for(int t=0; t<numSteps; t++) {
			for(Ride r: listRides) {
				if(t>= r.earliestStart) {
					//start time correct
					if((t+ Position.difference(r.startPosition, r.finishPosition))<= r.latestFinish) {
						addValues(t, r, mapAvailableRides);
						//System.out.println("differenza veicolo-start: "+Position.difference(v.p, r.startPosition)+ " - differenza start-finish:"+ Position.difference(r.startPosition, r.finishPosition));
					}
				}
			}

		}


		//cycle on time
		for(int t=0; t<numSteps; t++) {

			System.out.println("step "+ t);
			for(Vehicle v: listVehicles) {
				if(v.r== null) {
					//free vehicle
					Ride r= bestRide(v, t);

					if(r!=null) {
						listRides.remove(r);
						calculateScore(v, r);
						System.out.println("set ride: "+ r.id +" for vehicle: "+ v.id);
						v.setRide(r);

					}
				}
				else {
					// -1 all vehicles with distance
					v.distanceToMake--;
					if(v.distanceToMake==0)
						v.r= null;
				}
			}
		}


		return result;
	}

	private static void calculateScore(Vehicle v, Ride r) {
		
		
	}

	private static Ride createRide(String s) {
		String tmp[]= s.split(" ");
		Ride r= new Ride();
		Position p1= new Position();
		p1.x= Integer.valueOf(tmp[0]);
		p1.y= Integer.valueOf(tmp[1]);

		Position p2= new Position();
		p2.x= Integer.valueOf(tmp[2]);
		p2.y= Integer.valueOf(tmp[3]);

		r.startPosition= p1;
		r.finishPosition= p2;

		r.earliestStart= Integer.valueOf(tmp[4]);
		r.latestFinish= Integer.valueOf(tmp[5]);

		return r;
	}

	private static void analizeFirstLine(String s) {
		String tmp[] = s.split(" ");
		numRows= Integer.valueOf(tmp[0]);
		numColumns= Integer.valueOf(tmp[1]);
		numVehicles= Integer.valueOf(tmp[2]);
		numRides= Integer.valueOf(tmp[3]);
		bonusValue= Integer.valueOf(tmp[4]);
		numSteps= Integer.valueOf(tmp[5]);

	}

	/**
	 * will find best ride
	 * @param v vehicle
	 * @param t actual time
	 * @return
	 */
	private static Ride bestRide(Vehicle v, int t) {
		List<Ride> availableRides= new ArrayList<>();
		Ride rWin=null;

		if(mapAvailableRides.containsKey(t)) {
			for(Ride r: mapAvailableRides.get(t)) {
				if((Position.difference(v.p, r.startPosition) + t)>= r.earliestStart) {
					//start time correct
					if((Position.difference(v.p, r.startPosition) + t+ Position.difference(r.startPosition, r.finishPosition))<= r.latestFinish) {
						availableRides.add(r);

						//System.out.println("differenza veicolo-start: "+Position.difference(v.p, r.startPosition)+ " - differenza start-finish:"+ Position.difference(r.startPosition, r.finishPosition));
					}
				}
			}

			int maxPoints= Integer.MIN_VALUE;
			for(Ride r: availableRides) {
				//calculate max 
				int distanceRide= Position.difference(r.startPosition, r.finishPosition);
				int distFromRide= Position.difference(v.p, r.startPosition);
				if((distanceRide- distFromRide) >maxPoints) {
					maxPoints= (distanceRide- distFromRide);
					rWin= r;
				}		
			}

		}

		return rWin;

	}


	/**
	 * aggiunge valori a mappa con key che possono avere valori multipli
	 * @param key 
	 * @param value da aggiungere
	 * @param mappadb la mappa
	 */
	public static final void addValues(Integer key, Ride value, Map<Integer,ArrayList<Ride>> mappadb) {
		ArrayList<Ride> tempList = null;
		if (mappadb.containsKey(key)) {
			tempList = mappadb.get(key);
			if(tempList == null)
				tempList = new ArrayList<Ride>();
			tempList.add(value);  
		} else {
			tempList = new ArrayList<Ride>();
			tempList.add(value);               
		}
		mappadb.put(key,tempList);
	}

}

