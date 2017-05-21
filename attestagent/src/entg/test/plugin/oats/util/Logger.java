package entg.test.plugin.oats.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {

	
	public static Logger logger;
	public static Logger getLogger (String className) {
		return logger;
	}
	
	PrintWriter log;
	
	public Logger(PrintWriter log) {
		this.log = log;
	}
	
	public static void createLogger(String logFile) throws IOException {
		PrintWriter log = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
		  
		logger = new Logger(log);
	}
	
	public void error (String m)  {
		log.println("ERROR > "+m);
	}
	
	public void error (Exception e)  {
		log.println("ERROR > "+e.getMessage());
		log.println("Exception: ");
		e.printStackTrace(log);
	}
	
	public void error (String m, Exception e)  {
		log.println("ERROR > "+m);
		log.println("Exception: ");
		e.printStackTrace(log);
	}
	
	public void info (String m)  {
		log.println("> "+m);
	}
	
	public static void closeLogger() {
		logger.log.flush();
		logger.log.close();
	}
	
	
}
