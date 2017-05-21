package entg.test.plugin.selenium;


import entg.test.TCAgentMain;
import entg.test.TestRunInfo;
import entg.test.plugin.FunctionPluginInterface;

public class SeleniumFunctionPluginDriver implements entg.test.plugin.FunctionPluginDriver {

	static {
		  entg.test.plugin.FunctionPluginManager.registerPlugin(new SeleniumFunctionPluginDriver());
	}
	

	public FunctionPluginInterface initPlugin (TestRunInfo testrun) {
		String selType = entg.test.TCAgentMain.TCAgentproperties("SELENIUM_TYPE");
		if (selType!=null && selType.equals("webdriver")) {
		  return new SeleniumWDFunctionPlugin(testrun);			
		}	else {
		  return new SeleniumFunctionPlugin(testrun);
		}
	}
	
	public boolean isForType(String name) {
		// TODO Auto-generated method stub
		if (name.equalsIgnoreCase("selenium"))
			return true;
		return false;
	}

}
