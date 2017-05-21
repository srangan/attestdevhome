package entg.test.plugin.oats;


import entg.test.TestRunInfo;
import entg.test.plugin.FunctionPluginInterface;

public class OatsFunctionPluginDriver implements entg.test.plugin.FunctionPluginDriver {

	static {
		entg.test.plugin.FunctionPluginManager.registerPlugin(new OatsFunctionPluginDriver());
	}
	

	public FunctionPluginInterface initPlugin (TestRunInfo testrun) {
		return new OatsFunctionPlugin(testrun);
	}
	
	public boolean isForType(String name) {
		// TODO Auto-generated method stub
		if (name.equalsIgnoreCase("oats") || name.equalsIgnoreCase("openscript") )
			return true;
		return false;
	}

}