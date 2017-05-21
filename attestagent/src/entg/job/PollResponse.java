package entg.job;

/**
 * 
 * @author ravi
 *
 */

public class PollResponse {
	
	private String message;
	private RemoteJob job;
	private JobProgram jobProgram;
	private String params;
	
	public RemoteJob getJob() {
		return job;
	}
	public void setJob(RemoteJob job) {
		this.job = job;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public JobProgram getJobProgram() {
		return jobProgram;
	}
	public void setJobProgram(JobProgram jobProgram) {
		this.jobProgram = jobProgram;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}
	
}
