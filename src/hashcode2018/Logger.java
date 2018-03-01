package hashcode2018;

/** Metodi di LOG per il programma
 * @author claudio
 */
public interface Logger {

	/** Dove scriviamo i log? Questo può dipendere dal sistema operativo.
	 * @return il path assoluto della cartella di registrazione
	 */
	public String getLogPath();	


	/** Il messaggio di livello "D" più fine, usato per debug personale e poco altro; può essere filtrato via o no
	 * secondo la scelta dell'utente.
	 * @param ambito un numero che identifica il tipo di funzioni o la tessera o ... E' un numero utile per filtrare
	 * i messaggi che riguardano esclusivamente una funzione (p.es. un plugin)
	 * @param codiceMessaggio (opzionale) per facilitare la ricerca o il filtraggio di un evento specifico
	 * @param messaggio testo da registrare -- viene comunque appiattito, non è salvato su + righe
	 * @param stack null oppure una eccezione, che verrà registrata come PrintStackTrace nel file di log
	 */
	public void logDettaglio(int ambito,String codiceMessaggio,String messaggio,Throwable stack);


	/** Messaggio informativo "M" di livello alto, sempre registrato (dovrebbe riguardare eventi importanti, come
	 * p.es. la partenza di una funzione o la sua chiusura)
	 * @param ambito un numero che identifica il tipo di funzioni o la tessera o ... E' un numero utile per filtrare
	 * i messaggi che riguardano esclusivamente una funzione (p.es. un plugin)
	 * @param codiceMessaggio (opzionale) per facilitare la ricerca o il filtraggio di un evento specifico
	 * @param messaggio testo da registrare -- viene comunque appiattito, non è salvato su + righe
	 * @param stack null oppure una eccezione, che verrà registrata come PrintStackTrace nel file di log
	 */
	public void logMessaggio(int ambito,String codiceMessaggio,String messaggio,Throwable stack);


	/** Messaggio comunque registrato con flag ERRORE "E". Il flush dei messaggi su disco avviene immediatamente.
	 * @param ambito un numero che identifica il tipo di funzioni o la tessera o ... E' un numero utile per filtrare
	 * i messaggi che riguardano esclusivamente una funzione (p.es. un plugin)
	 * @param codiceMessaggio (opzionale) per facilitare la ricerca o il filtraggio di un evento specifico
	 * @param messaggio testo da registrare -- viene comunque appiattito, non è salvato su + righe
	 * @param stack null oppure una eccezione, che verrà registrata come PrintStackTrace nel file di log
	 */
	public void logErrore(int ambito,String codiceMessaggio,String messaggio,Throwable stack);


	/** Un log banale, per tracciare velocemente durante lo sviluppo.... Equivale a logDettaglio ambito=0, no codice messaggio
	 * @param messaggio
	 */
	public void log(String messaggio);


	/** Un log banale, per tracciare velocemente durante lo sviluppo....
	 * @param ambito codice funzione o plugin
	 * @param messaggio
	 */
	public void log(int ambito, String messaggio);

	/**
	 * Manda a DIRECTA un resoconto della situazione.
	 * La richiesta di autorizzazione etc. va chiesta PRIMA di chiamare questo metodo
	 * @param mittente 						- NB: ad oggi non sono ammessi blank in questa variabile. 
	 * @param dest all'attenzione di...
	 * @param applet nome applet chiamante se "" considera Darwin
	 * @return true se tutto bene
	 */
	public boolean inviaDiagnostica(String mittente, String dest,String applet);

	/**
	 * inizializza il logger
	 * @param prefisso : nome file
	 * @param nomeCartella  
	 * @param usaCriptazione 
	 */
	public void init(String prefisso,String nomeCartella,boolean usaCriptazione);



}
