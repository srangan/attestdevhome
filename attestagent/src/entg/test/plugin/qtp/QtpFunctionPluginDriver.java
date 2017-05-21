package entg.test.plugin.qtp;

import entg.test.AppSignInfo;
import entg.test.FunctionInfo;
import entg.test.TestRunInfo;
import entg.test.TestcaseInfo;
import entg.test.plugin.FunctionPluginInterface;

public class QtpFunctionPluginDriver implements entg.test.plugin.FunctionPluginDriver {

	static {
		entg.test.plugin.FunctionPluginManager.registerPlugin(new QtpFunctionPluginDriver());
	} 
	
	public FunctionPluginInterface initPlugin (TestRunInfo testrun) {
		return new QtpFunctionPlugin(testrun);
	}
	
	public boolean isForType(String name) {
		// TODO Auto-generated method stub
		if (name.equalsIgnoreCase("qtp"))
			return true;
		return false;
	}

}
