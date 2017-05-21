package entg.test.plugin.rft;


import entg.test.TestRunInfo;
import entg.test.plugin.FunctionPluginInterface;

public class RFTFunctionPluginDriver implements entg.test.plugin.FunctionPluginDriver {

	static {
		entg.test.plugin.FunctionPluginManager.registerPlugin(new RFTFunctionPluginDriver());
	}
	

	public FunctionPluginInterface initPlugin (TestRunInfo testrun) {
		return new RFTFunctionPlugin(testrun);
	}
	
	public boolean isForType(String name) {
		// TODO Auto-generated method stub
		if (name.equalsIgnoreCase("rft"))
			return true;
		return false;
	}

}
