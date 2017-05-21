package entg.job.extract;

import entg.job.AgentDriver;
import entg.job.*;

public class ExtractAgentDriver implements AgentDriver {

	static {
		entg.job.JobManager.registerAgent(new ExtractAgentDriver());
	}
	
	public JobAgent getAgent(String programId, String jobId, String parameters) {
		// TODO Auto-generated method stub
		return new ExtractAgent(programId,jobId,parameters);
	}

	public boolean isForProgram(String name) {
		// TODO Auto-generated method stub
		if (name.equalsIgnoreCase("EXTRACT AGENT"))
			return true;
		return false;
	}

}
