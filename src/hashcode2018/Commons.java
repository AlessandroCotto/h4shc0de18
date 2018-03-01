package hashcode2018;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility class
 * 
 * @author federicoballarini
 *
 */
public class Commons {


	/**
	 * Legge il file
	 * @param filename
	 * @return list di stringhe con le righe del file
	 */
	public static final List<String> readFile(String filename) {
		System.out.println("Here's where I read file: "+ filename);
		List<String> arr= new ArrayList<>();
		try {
			try (Stream<String> stream = Files.lines(Paths.get(filename))) {
				stream.forEach(arr::add);	
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return arr;

	}

	/**
	 * Salva il file
	 * @param filename
	 * @param toSave
	 */
	public static final void saveFile(String filename, List<String> toSave) {
		System.out.println("Here's where I save file to: "+ filename);
		
		try {
			Files.write(Paths.get(filename), toSave);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * fa partire le funzioni di log
	 */
	public static final void initLog() {
		Log logger=new Log();
		logger.init("HashCode18", null , false); //nomefile - cartella - offusca
		logger.logStartMessage("HashCode18");

	}

}
