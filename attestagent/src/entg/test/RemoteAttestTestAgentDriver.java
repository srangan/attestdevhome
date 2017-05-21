package entg.test;

import entg.job.AgentDriver;
import entg.job.JobAgent;

public class RemoteAttestTestAgentDriver implements AgentDriver {

	static {
		entg.job.JobManager.registerAgent(new RemoteAttestTestAgentDriver());
	}
	
	public JobAgent getAgent(String programId, String jobId, String parameters) {
		// TODO Auto-generated method stub 
		return (JobAgent) (new RemoteAttestTestAgent(jobId,programId,parameters));
	}    

	public boolean isForProgram(String name) {
		// TODO Auto-generated method stub
		System.out.println("====> class name: "+name);
		if (name.equalsIgnoreCase("SFDC Test Agent"))
			return true;
		return false;
	} 

}
