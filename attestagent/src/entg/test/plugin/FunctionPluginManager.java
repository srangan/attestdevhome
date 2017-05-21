package entg.test.plugin;

import java.util.Enumeration;
import java.util.Vector;

import entg.test.AppSignInfo;
import entg.test.FunctionInfo;
import entg.test.TestRunInfo;
import entg.test.TestcaseInfo;


public class FunctionPluginManager {

	public static Vector pluginDrivers;
	
	static {
		pluginDrivers = new Vector();
	}
	public static void registerPlugin(FunctionPluginDriver fp) {
		if (!pluginDrivers.contains(fp)) 
			pluginDrivers.addElement(fp);
	}	
	
	static boolean loaded = false;
	public static void loadAllPlugins() throws Exception {
		if (!loaded) {
			loaded = true;
			Class.forName("entg.test.plugin.qtp.QtpFunctionPluginDriver");
			Class.forName("entg.test.plugin.plsql.PlsqlFunctionPluginDriver");
			Class.forName("entg.test.plugin.selenium.SeleniumFunctionPluginDriver");
			Class.forName("entg.test.plugin.rft.RFTFunctionPluginDriver");
			Class.forName("entg.test.plugin.sahi.SahiFunctionPluginDriver");
			Class.forName("entg.test.plugin.oats.OatsFunctionPluginDriver");
		}
	}
	
	public static FunctionPluginInterface initPlugin(TestRunInfo testrun,String functionType) throws Exception {
		

		Enumeration e = pluginDrivers.elements();
		while (e.hasMoreElements()) {
			FunctionPluginDriver fp = (FunctionPluginDriver) e.nextElement();
			if (fp.isForType(functionType)) {
				return fp.initPlugin(testrun);
			}  
		}
		throw new Exception("No function plugins Found");
	}
	
}
