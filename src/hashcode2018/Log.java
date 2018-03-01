package hashcode2018;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * interfaccia di logging per lib
 * un po' diversa da quella di commons
 * @author federicoballarini
 *
 *
 *	AMBITI
 *	- JAVA
 * 		0: generico e visibile di default per system out e tutti i log non altrimenti specificati
 * 		1: ServerElectronPort, tutti i vari passaggi relativi alla gestione Proton
 * 		2: debug dei messaggi scambiati su socket di serverelectronport
 * 		3: JSONsaveRestore e i messaggi relativi alla gestione salvataggi
 * 		4: GUI e i componenti di gestione della grafica
 * 		5: Desktop e il suo ripristino/gestione
 * 		6: Bar, Drawer, Message, MenuDW e la loro gestione
 * 		
 * 		10: Tile e tutto quello che la contraddistingue
 * 		
 * 		50: ElectronTile e tutto quello che la contraddistingue
 * 
 *	- ELECTRON
 *		101: generico e visibile di default per main.js e tutti quelli senza altro ambito specificato
 *		102: log specifico di listsave (finestra di gestione salvataggi)
 *		103: log specifico di contenitore
 *		104: configuratore e i componenti di gestione della grafica Proton
 *		106: bar, drawer, message, etc*
 *
 */
public class Log implements Logger {

	private static final int ambitoProton= 101;
	private static final char tipoProton= 'P';
	private static final char tipoMotore= 'M';
	/** Filtro ambiti di log */
	public static Set<Integer> elencoAmbiti = new HashSet<Integer>();
	/** Percorso cartella dei LOG files */
	private static String logPath;
	/** usa offuscamento */
	private static boolean offusca = false;
	private static long tsInizio;
	/** Nome del file di LOG attuale */



	private static boolean dettaglio= true;
	private static PrintStream stdout, stderr;
	private static final ByteArrayOutputStream dummyStream = new ByteArrayOutputStream();
	private static final StringBuilder bufferInterno = new StringBuilder(10000);
	private static final int SOGLIA = 1024*10; //capacità del buffer interno prima di scrivere su file

	/** Se true, logga tutto */
	private static boolean logAll=false;

	private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private static ScheduledFuture<?> flushingThread;

	@Override
	public String getLogPath() {
		return logPath;
	}

	/** Abilita la registrazione SOLO di messaggi che vogliamo loggare.
	 * @param massimoDettaglio true=registra anche i "logDettaglio" di tipo D, false=registra solo messaggi M+E. Errori: SEMPRE LOGGATI!
	 * @param ambito elenco di codici "ambito" da registrare. Se vuoto o null, registra tutto...
	 */
	public static void abilitaLivello(boolean massimoDettaglio, int[] ambito) {
		dettaglio = massimoDettaglio;
		if(ambito==null || ambito.length==0)
			elencoAmbiti.clear();
		else {
			for(int i: ambito)
				elencoAmbiti.add(i);
		}
		System.out.println("Ambiti visualizzati: "+elencoAmbiti);
	}

	@Override
	public void logDettaglio(int ambito, String codiceMessaggio, String messaggio, Throwable stack) {
		if(dettaglio==false)
			return;
		registra('D', ambito, codiceMessaggio, messaggio, stack, 4);
	}

	@Override
	public void logMessaggio(int ambito, String codiceMessaggio, String messaggio, Throwable stack) {
		registra('M', ambito, codiceMessaggio, messaggio, stack, 4);
	}

	@Override
	public void logErrore(int ambito, String codiceMessaggio, String messaggio, Throwable stack) {
		registra('E', ambito, codiceMessaggio, messaggio, stack, 4);
		flush();
	}

	@Override
	public void log(String messaggio) {	
		if(dettaglio==false)
			return;
		registra('D', 0, null, messaggio, null, 4);
	}

	@Override
	public void log(int ambito, String messaggio) {
		if(dettaglio==false)
			return;
		registra('D', ambito, null, messaggio, null, 4);
	}


	/**
	 * log messaggio statico
	 * @param ambito l'ambito del msg
	 * @param messaggio il messaggio
	 */
	public static void logmsg(int ambito, String messaggio) {
		if(dettaglio==false)
			return;
		registra('D', ambito, null, messaggio, null, 4);
	}

	@Override
	public boolean inviaDiagnostica(String mittente, String dest, String applet) {
		return false;
	}



	public void init(String prefisso,String nomeCartella,boolean usaCriptazione){
		elencoAmbiti.add(0); //per evitare problemi di log vuoto
		logPath = nomeCartella;
		offusca = usaCriptazione;
		if(logPath!=null) {
			new File(logPath).mkdir();
			logPath += "log/";
			new File(logPath).mkdir();
			creaNuovoLog(prefisso);
		}
		else{
			disabilita();
		}
		stdout = System.out;
		System.setOut(new myPrintStream(false));
		stderr = System.err;
		System.setErr(new myPrintStream(true));

		registra('D', 0, null, "Ciao Logger :) ", null, 4);
		//Il sistema di "flush" a tempo

		int initialDelay = 0;
		int period = 830;
		executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
	}

	Runnable task = () -> {
		flush();
	};


	private static void creaNuovoLog(String prefisso) {
		tsInizio = System.currentTimeMillis();		
		//if(offusca)
		//nome = encoder.encrypt(nome);
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		synchronized (bufferInterno) {
			bufferInterno.setLength(0);
			String s = "================ Log iniziato il "+df.format(new Date());
			bufferInterno.append("\n\n");
			bufferInterno.append(offusca ? s : s);
			bufferInterno.append("\n\n");
		}
	}



	/**
	 * Stampa effettivamente il messaggio  
	 * @param tipo 
	 * @param ambito
	 * @param codiceMessaggio
	 * @param messaggio
	 * @param stack
	 * @param profondita
	 */
	private static synchronized void registra(char tipo, int ambito,String codiceMessaggio,String messaggio,Throwable stack,int profondita) {
		if(tipo=='D' && dettaglio==false)
			return;

		else if(!elencoAmbiti.isEmpty()){
			if(elencoAmbiti.contains(ambito) || tipo=='E' || logAll){
				long now = System.currentTimeMillis();
				if(codiceMessaggio==null || codiceMessaggio.length()==0)
					codiceMessaggio = "";
				else
					codiceMessaggio += " - ";

				String origine ="";
				String s="";
				if(tipo != tipoProton && tipo != tipoMotore ){
					try {
						origine = leggiNomeChiamante(profondita);
					}catch (Exception e) {
						s = String.format("%8d %c [%d] %s%s <%s>", now-tsInizio,tipo,ambito,codiceMessaggio,messaggio,origine);
					}
					s = String.format("%8d %c [%d] %s%s <%s>", now-tsInizio,tipo,ambito,codiceMessaggio,messaggio,origine);
				}
				else{
					s = String.format("%8d %c [%d] %s%s", now-tsInizio,tipo,ambito,codiceMessaggio,messaggio);
				}


				if(stack!=null) {
					s += leggiEccezione(stack);
				}

				boolean flushNecessario = false;
				synchronized (bufferInterno) {
					bufferInterno.append(offusca ? s : s);
					bufferInterno.append('\n');
					flushNecessario = bufferInterno.length() > SOGLIA;
				}
				if(flushNecessario || tipo=='E')
					flush();

				if(tipo=='E'){
					try{
						stderr.println(s);
					}
					catch(Exception e){
						System.out.println(e);
					}
				}
				else {
					try{
						stdout.println(s);
					}
					catch(Exception e){
						System.out.println(e);
					}	
				}

			}
		}



	}

	//Legge nome classe e metodo chiamante
	private static String leggiNomeChiamante(int profondita) {
		Throwable t = new Throwable();
		StackTraceElement[] st = t.getStackTrace();
		if(st.length < profondita)
			return "";
		StackTraceElement e = st[profondita-1];
		String[] cn = e.getClassName().split("\\.");
		return cn[cn.length-1]+":"+e.getMethodName()+"#"+e.getLineNumber();
	}

	//Trasforma una eccezione in formato testo
	private static String leggiEccezione(Throwable e) {
		StringBuilder sb = new StringBuilder(" *** ");
		sb.append(e.getClass().getSimpleName());
		sb.append(" *** ");
		sb.append(e.getMessage());
		for(StackTraceElement se: e.getStackTrace()) {
			sb.append(" @");
			sb.append(se.getClassName());
			sb.append('#');
			sb.append(se.getLineNumber());
		}
		if(e.getCause()!=null) {
			sb.append(" *** causa: ");
			sb.append(leggiEccezione(e.getCause()));
		}
		return sb.toString();
	}

	private static class myPrintStream extends PrintStream {

		private boolean errore = false;

		public myPrintStream(boolean err) {
			super(dummyStream, false);
			errore = err;
		}

		@Override
		public void print(String s) {
			if(errore)
				registra('E', 0, null, s, null, 5);
			else
				registra('D', 0, null, s, null, 5);
		}

		@Override
		public void println(String s) {
			if(errore)
				registra('E', 0, null, s, null, 5);
			else
				registra('D', 0, null, s, null, 5);
		}

		@Override
		public void println() {
		}

		@Override
		public void println(Object x) {
			String s = x.toString();
			if(errore)
				registra('E', 0, null, s, null, 5);
			else
				registra('D', 0, null, s, null, 5);
		}

	}

	/**
	 * Anche se il flush è automatico, male non fa chiamare questo metodo a fine main() ...
	 */
	public static void flush() {
		//		synchronized (bufferInterno) {
		//
		//			if(bufferInterno.length()>0) {
		//				try {
		//					FileWriter fos = new FileWriter(logFilename,true);
		//					fos.write(bufferInterno.toString());
		//					fos.close();
		//				} catch (IOException e) {
		//					e.printStackTrace(stderr);
		//				}
		//				bufferInterno.setLength(0);
		//			}
		//		}
	}


	/**
	 * logga motore nella console java
	 * @param proc il processo con cui ho fatto partire su terminale darwin muta
	 */
	public static void logMotore(Process proc){

		if(proc!=null){
			BufferedReader stdInput = new BufferedReader(new
					InputStreamReader(proc.getInputStream()));

			BufferedReader stdError = new BufferedReader(new
					InputStreamReader(proc.getErrorStream()));

			//Seguono due mini-thread che visualizzano in Eclipse il log di Motore
			new Thread() {
				public void run() {
					String s = null;
					try {
						while ((s = stdInput.readLine()) != null) {
							motoreMsg(s);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				};
			}.start();

			//TODO forse questo è inutile
			new Thread() {
				public void run() {
					String s = null;
					try {
						while ((s = stdError.readLine()) != null) {
							motoreMsg(s);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}.start();		
		}
	}

	/**
	 * per ora è una normale stampa con [M] per motore
	 * @param s la stringa da stampare
	 */
	private static void motoreMsg(String s) {
		s= s.trim();
		if(s.length()!=0){
			if ( Character.isDigit(s.charAt(0)) && Character.isDigit(s.charAt(1)) && Character.isDigit(s.charAt(2)) ){
				//ragionevolmente, inizia con un timestamp
				//1498467864749 M [0] DBG - flussogenerico.debug : loop di lettura patito ora ( 1498467864749 ) <FlussoPrezzi:run#275>
				//System.out.println(s);
				try{
					String tmp[]= s.split(" ");
					//long ts= Long.valueOf(tmp[0]);
					char tipo= tmp[1].charAt(0);
					int ambito= Integer.valueOf(tmp[2].replace("[", "").replace("]", ""));
					String codice= "";
					String messaggio= "";
					int msgstart=0;
					if(tmp[4].equals("-")){
						codice= tmp[3];
						msgstart= 5;
					}
					else{
						msgstart= 3;
					}

					while(msgstart<tmp.length){
						messaggio+= tmp[msgstart]+" ";
						msgstart++;
					}

					//String chiamante= tmp[tmp.length-1];
					registra(tipo, ambito, codice, messaggio, null, 0);
					//System.out.println(ts+ " "+ tipo+ " "+ ambito+ " "+ codice+ " - "+ messaggio+ " >< "+chiamante);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			else{ //vai sereno e pensare a cosa mettere come funzione chiamante
				s+=" <Motore>";
				registra(tipoMotore, 0, null, s, null, 0);
			}
		}
	}





	/**
	 * logga electron nella console java
	 * @param proc il processo con cui ho fatto partire su terminale electron
	 */
	public static void logElectron(Process proc){

		if(proc!=null){
			BufferedReader stdInput = new BufferedReader(new
					InputStreamReader(proc.getInputStream()));

			BufferedReader stdError = new BufferedReader(new
					InputStreamReader(proc.getErrorStream()));

			//Seguono due mini-thread che visualizzano in Eclipse la console di Electron
			new Thread() {
				public void run() {
					String s = null;
					try {
						while ((s = stdInput.readLine()) != null) {
							electronMessage(s);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				};
			}.start();

			//TODO forse questo è inutile
			new Thread() {
				public void run() {
					String s = null;
					try {
						while ((s = stdError.readLine()) != null) {
							electronMessage(s);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}.start();		
		}
	}
	/**
	 * per ora è una normale stampa con [P] per electron
	 * @param s la stringa da stampare
	 */
	private static void electronMessage(String s) {
		if(s.trim().length()!=0){
			s= manipola(s);
			int ambito;
			try{
				ambito=Integer.valueOf(s.substring(0, 3));
				s= s.substring(3);

			}catch(Exception e){
				ambito=ambitoProton;
			}
			registra(tipoProton, ambito, null, s, null, 5);
		}
	}

	/**
	 * manipola la stringa electron per renderla compatibile con il log
	 * @param s
	 * @return la stringa ripulita
	 */
	private static String manipola(String s) {
		s= s.trim();
		try{
			if(s.contains("[info] "))
				s= s.split("\\[info\\] ")[1];
		}catch(Exception e){
			System.out.println("errore split log "+s);
		}
		return s;
	};

	/**
	 * Ripristina i flussi di output
	 */
	public static void disabilita() {
		stderr = System.err;
		stdout = System.out;
		System.out.println("No log");
	}


	/** Thread Elimina i file di log troppo vecchi
	 * @param ngiorni quanti giorni mantenere
	 */
	public static void svecchiamento(long ngiorni) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			if(logPath==null)
				return;
			File fpath = new File(logPath); 
			long piuVecchio = tsInizio - (ngiorni * 3600000 * 24);
			String[] elenco = fpath.list();
			for(String n: elenco) {
				File f = new File(logPath+"/"+n);
				if(f.lastModified() < piuVecchio)
					f.delete();
			}
		});

	}


	/**
	 * setto il log di tutto il creato oppure solo di quello che è stato aggiunto in ambiti
	 * @param b il boolean
	 */
	public static void setLogAll(boolean b){
		Log.logAll=b;
	}

	/**
	 * flusha e chiude il thread di log
	 */
	public static void close() {

		executor.shutdown();
		if(flushingThread!=null)
			flushingThread.cancel(false);
		System.exit(0);
	}


	/**
	 * logga un processo lanciato da command line 
	 * @param proc
	 * @param chiudiAllaFine 
	 */
	public static void logProcess(Process proc, boolean chiudiAllaFine){

		if(proc!=null){
			BufferedReader stdInput = new BufferedReader(new
					InputStreamReader(proc.getInputStream()));

			BufferedReader stdError = new BufferedReader(new
					InputStreamReader(proc.getErrorStream()));

			//Seguono due mini-thread che visualizzano in Eclipse la console di Electron
			new Thread() {
				public void run() {
					String s = null;
					try {
						while ((s = stdInput.readLine()) != null) {
							if(s.trim().length()!=0)
								System.out.println(s);
						}
						//finita l'applicazione faccio che uscire va
						if(chiudiAllaFine)
							System.exit(0);

					} catch (IOException e) {
						e.printStackTrace();
					}
				};
			}.start();

			new Thread() {
				public void run() {
					String s = null;
					try {
						while ((s = stdError.readLine()) != null) {
							if(s.trim().length()!=0)
								System.out.println(s);
						}
						//finita l'applicazione faccio che uscire va
						if(chiudiAllaFine)
							System.exit(0);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}.start();		
		}


	}
	/**
	 * logga darwin da command line su java
	 * e alla fine fa la system exit 0 
	 * @param proc il process
	 */
	public static void logDarwin(Process proc){
		logProcess(proc, true);
	}

	/**
	 * Start del log 
	 * @param txt parametro relativo all'app che stiamo facendo partire
	 */
	public final void logStartMessage(String txt) {
		Log.logmsg(0,"APP - "+txt+ " - session start: " +     (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(new Date()) );
		Log.logmsg(0,"JVM - " + System.getProperty("java.vm.name")+" - " + System.getProperty("java.version")+" - " + System.getProperty("java.vm.version"));
		Log.logmsg(0,"OS - " + System.getProperty("os.name") +" - " + System.getProperty("os.version") + " ( " + System.getProperty("os.arch") + " )");
	}

}
