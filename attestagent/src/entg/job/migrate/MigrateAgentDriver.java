package entg.job.migrate;

import entg.job.AgentDriver;
import entg.job.*;

public class MigrateAgentDriver implements AgentDriver {

	static {
		entg.job.JobManager.registerAgent(new MigrateAgentDriver());
	}
	
	public JobAgent getAgent(String programId, String jobId, String parameters) {
		// TODO Auto-generated method stub 
		return (JobAgent) (new MigrateAgent(jobId,programId,parameters));
	}    

	public boolean isForProgram(String name) {
		// TODO Auto-generated method stub
		if (name.equalsIgnoreCase("MIGRATE AGENT"))
			return true;
		return false;
	} 

}
