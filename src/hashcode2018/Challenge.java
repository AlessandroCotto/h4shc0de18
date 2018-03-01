package hashcode2018;

import java.util.ArrayList;
import java.util.List;

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
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		Commons.initLog();
		long initTS= System.currentTimeMillis();
		System.out.println("Start!");
		List<String> l= Commons.readFile("/Users/federicoballarini/Downloads/a_example.in");
		
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
		
		
		
		for(int i=1; i<list.size(); i++) {
			String s= list.get(i);
			Ride r= createRide(s);
			listRides.add(r);
		}
		
		System.out.println("size rides:"+ listRides.size());
		return result;
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
		
		System.out.println("ride from ["+r.startPosition.x+", "+r.startPosition.y+"] to ["+r.finishPosition.x+", "+r.finishPosition.y+"], earliest start "+r.earliestStart+", latest finish "+r.latestFinish);
		
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
	
	
}
