package entg.job.testplan;

import entg.job.AgentDriver;
import entg.job.*;

public class PlanAgentDriver implements AgentDriver {

	static {
		entg.job.JobManager.registerAgent(new PlanAgentDriver());
	}
	
	public JobAgent getAgent(String programId, String jobId, String parameters) {
		// TODO Auto-generated method stub 
		return (JobAgent) (new PlanAgent(jobId,programId,parameters));
	}    

	public boolean isForProgram(String name) {
		// TODO Auto-generated method stub
		if (name.equalsIgnoreCase("PLAN AGENT"))
			return true;
		return false;
	} 

}
