package entg.job;

public interface AgentDriver {
	public boolean isForProgram(String name);
	public JobAgent getAgent(String programId, String jobId,String parameters); 
	
} 