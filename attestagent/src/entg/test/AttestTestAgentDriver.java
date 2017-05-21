package entg.test;
import entg.job.AgentDriver;
import entg.job.*;

public class AttestTestAgentDriver implements AgentDriver {

	static {
		entg.job.JobManager.registerAgent(new AttestTestAgentDriver());
	}
	
	public JobAgent getAgent(String programId, String jobId, String parameters) {
		// TODO Auto-generated method stub 
		return (JobAgent) (new AttestTestAgent(jobId,programId,parameters));
	}      

	public boolean isForProgram(String name) {
		// TODO Auto-generated method stub
		if (name.equalsIgnoreCase("TEST AGENT"))
			return true;
		return false;
	} 

}
