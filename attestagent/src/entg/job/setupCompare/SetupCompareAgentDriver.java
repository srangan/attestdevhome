package entg.job.setupCompare;


import entg.job.AgentDriver;
import entg.job.*;

public class SetupCompareAgentDriver  implements AgentDriver {

	static {
		entg.job.JobManager.registerAgent(new SetupCompareAgentDriver());
	}
	
	public JobAgent getAgent(String programId, String jobId, String parameters) {
		// TODO Auto-generated method stub
		return new SetupCompareAgent(programId,jobId,parameters);
	}

	public boolean isForProgram(String name) {
		// TODO Auto-generated method stub
		if (name.equalsIgnoreCase("SETUP COMPARE AGENT"))
			return true;
		return false;
	}
}