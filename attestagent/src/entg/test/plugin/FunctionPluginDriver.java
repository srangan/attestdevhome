package entg.test.plugin;

import entg.test.*;

public interface FunctionPluginDriver {

	public boolean isForType(String name);
	
	public FunctionPluginInterface initPlugin(TestRunInfo testrun);
	
	   
}
