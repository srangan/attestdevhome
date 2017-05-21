package entg.job.oracm;
import entg.job.*;


	

public class OraCMAgentDriver implements AgentDriver {

		static {
			entg.job.JobManager.registerAgent(new OraCMAgentDriver());
		}
		
		public JobAgent getAgent(String programId, String jobId,String parameters) {
			// TODO Auto-generated method stub
			return new OraCMAgent(programId,  jobId,parameters);
		}

		public boolean isForProgram(String name) {
			// TODO Auto-generated method stub
			if (name.equalsIgnoreCase("ORACMAGENT"))
				return true;
			return false;
		}

	}