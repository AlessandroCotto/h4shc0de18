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
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		Commons.initLog();
		long initTS= System.currentTimeMillis();
		System.out.println("Start!");
		List<String> l= Commons.readFile("/Users/federicoballarini/Downloads/giocatore.csv");
		
		List<String> result= algorithm(l);
		
		Commons.saveFile("/Users/federicoballarini/Downloads/giocatore2.csv", result);
		
		System.out.println("time elapsed: "+(System.currentTimeMillis()-initTS)+ " ms");
		Log.close();
		
	}
	
	static List<String> algorithm(List<String> list) {
		List<String> result= new ArrayList<>();

		for(String s: list) {
			String f= new StringBuilder(s).reverse().toString();
			result.add(f);
		}
		return result;
	}
}
