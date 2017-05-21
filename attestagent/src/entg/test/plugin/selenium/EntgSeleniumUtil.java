package entg.test.plugin.selenium;

import java.io.PrintWriter;

public class EntgSeleniumUtil {

	public static void addScreenshot(String file, String message, PrintWriter log) throws Exception {
		String link = entg.test.TestRunInfo.uploadFile (file);
		log.println(message+"<A href="+link+">Screenshot</A>");
	}
}
