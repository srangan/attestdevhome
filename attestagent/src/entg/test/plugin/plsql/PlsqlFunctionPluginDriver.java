package entg.test.plugin.plsql;


import entg.test.TestRunInfo;
import entg.test.plugin.FunctionPluginInterface;

public class PlsqlFunctionPluginDriver implements entg.test.plugin.FunctionPluginDriver {

	static {
		entg.test.plugin.FunctionPluginManager.registerPlugin(new PlsqlFunctionPluginDriver());
	}
	

	public FunctionPluginInterface initPlugin (TestRunInfo testrun) {
		return new PlsqlFunctionPlugin(testrun);
	}
	
	public boolean isForType(String name) {
		// TODO Auto-generated method stub
		if (name.equalsIgnoreCase("plsql"))
			return true;
		return false;
	}

}
