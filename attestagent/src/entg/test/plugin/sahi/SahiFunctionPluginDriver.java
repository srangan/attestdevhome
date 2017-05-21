package entg.test.plugin.sahi;

import entg.test.TestRunInfo;
import entg.test.plugin.FunctionPluginInterface;

public class SahiFunctionPluginDriver implements entg.test.plugin.FunctionPluginDriver {

	static {
		entg.test.plugin.FunctionPluginManager.registerPlugin(new SahiFunctionPluginDriver());
	}
	

	public FunctionPluginInterface initPlugin (TestRunInfo testrun) {
		return new SahiFunctionPlugin(testrun);
	}
	
	public boolean isForType(String name) {
		// TODO Auto-generated method stub
		if (name.equalsIgnoreCase("sahi"))
			return true;
		return false;
	}

}