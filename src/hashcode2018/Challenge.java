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
	static List<Vehicle> listVehicles= new ArrayList<>();

	static int totalScore=0;
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		Commons.initLog();
		long initTS= System.currentTimeMillis();
		System.out.println("Start!");

		//String fname= "a_example.in";
		String fname= "b_should_be_easy.in";
		//String fname= "c_no_hurry.in";
		//String fname= "d_metropolis.in";
		//String fname= "e_high_bonus.in";

		//8+ 75232+ 8483191+6206927 + 9224520
		List<String> l= Commons.readFile("/Users/federicoballarini/Downloads/"+fname);
		//List<String> l= Commons.readFile("/Users/federicoballarini/Downloads/e_high_bonus.in");

		List<String> result= algorithm(l);

		//Commons.saveFile("/Users/federicoballarini/Downloads/res_"+fname+".in", result);

		System.out.println("time elapsed: "+(System.currentTimeMillis()-initTS)+ " ms");
		System.out.println("Final Score: "+ totalScore);
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



		//cycle on time
		for(int t=0; t<numSteps; t++) {
			ArrayList<Ride> arrTimeRides= new ArrayList<>();
			for(Ride r: listRides) {
				if(t>= r.earliestStart) {
					//start time correct
					r.distanceRide= Position.difference(r.startPosition, r.finishPosition);
					if((t+ r.distanceRide)<= r.latestFinish) {
						arrTimeRides.add(r);
					}
				}
			}

			ArrayList<Vehicle> arrFreeVehicles= new ArrayList<>();


			for(Vehicle v: listVehicles) {
				if(v.r== null) {
					arrFreeVehicles.add(v);
				}
				else {
					// -1 all vehicles with distance
					v.distanceToMake--;
					if(v.distanceToMake==0)
						v.r= null;
				}
			}

			bestRide(arrFreeVehicles, t, arrTimeRides);
			/*
			for(Vehicle v: arrFreeVehicles) {
				//free vehicle
				Ride r= bestRide(v, t, arrTimeRides);

				if(r!=null) {
					listRides.remove(r);
					arrTimeRides.remove(r);
					calculateScore(v, r, t);
					v.rideDone.add(r.id);
					System.out.println("set ride: "+ r.id +" for vehicle: "+ v.id);
					v.setRide(r);
				}

			}
			 */
			System.out.println("finish step "+ t+" - free vehicles: "+arrFreeVehicles.size());
		}


		for(Vehicle v: listVehicles) {
			ArrayList<Integer> arr= v.rideDone;
			String s;
			if(arr.size()!=0) {
				s= ""+arr.size();
				for(int i: arr) {
					s+= " "+i;
				}
			}
			else
				s="0";

			result.add(s);

		}


		return result;
	}

	static int calculateScore(Vehicle v, Ride r, int t){
		int score= 0;
		if((t+ v.distanceToRide)== r.earliestStart) {
			score+=bonusValue;
		}
		score+= r.distanceRide;
		return score;
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
	 * @param arrFreeVehicles vehicle
	 * @param t actual time
	 * @return
	 */
	private static void bestRide(ArrayList<Vehicle> arrFreeVehicles, int t, ArrayList<Ride> arrTimeRides) {
		
		if(arrTimeRides!=null && arrFreeVehicles.size()!=0) {
			while(arrTimeRides.size() >0) {
				chooseBetter(arrTimeRides, arrFreeVehicles, t);
			}

		}
	}

	private static void chooseBetter(ArrayList<Ride> arrTimeRides, ArrayList<Vehicle> arrFreeVehicles , int t) {

		int i=0;
		while(arrTimeRides.size()!=0  && i<arrTimeRides.size()) {
			
			Ride r= arrTimeRides.get(i);
			int maxScore= Integer.MIN_VALUE;
			Vehicle vWinner=null;
			for(Vehicle v: arrFreeVehicles) {
				System.out.println("ciao "+i+ " "+v);
				v.distanceToRide= Position.difference(v.p, r.startPosition);
				if((v.distanceToRide + t)>= r.earliestStart) {
					//start time correct
					if((v.distanceToRide + t+ r.distanceRide)<= r.latestFinish) {
						
						int score= calculateScore(v, r, t);
						if(score>maxScore) {
							vWinner= v;
							maxScore= score;
							
						}
					}
				}
			}
			if(true) {
				listRides.remove(r);
				arrTimeRides.remove(i);
				totalScore+=maxScore;
				System.out.println("set ride: "+ r.id +" for vehicle: "+ vWinner.id);
	
				vWinner.rideDone.add(r.id);
				vWinner.setRide(r);
			}



			i++;
		}


	}



}

