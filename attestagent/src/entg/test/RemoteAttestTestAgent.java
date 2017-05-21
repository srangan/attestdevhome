package entg.test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.google.gson.Gson;

import entg.job.Job;
import entg.job.JobAgent;
import entg.job.JobManager;
import entg.job.SfdcJobManager;

public class RemoteAttestTestAgent implements JobAgent {
	
	private String jobId;
	private String programId;
	private String runId;
	
	
	public RemoteAttestTestAgent(String jobId, String programId, String parameters) {
		this.jobId = jobId;
		this.programId = programId;
		
		runId =JobManager.getParameter(parameters, "RUNID");
	}


	public void startJob(Job j) throws Exception {
		System.out.println("====> RemoteAttestTestAgent starting...jobId ");
		
		String exmlFile = null;// RAVI SfdcJobManager.downloadFile(runId);
		if (exmlFile!=null) {
			String[] args = {""+exmlFile};  
			TCAgentMain.jobMain(args,j.out);
		} else {
			j.wlog("Error Occured in Test Agent: Error downloading exml file");
			j.out.flush();

			throw new Exception("Test Agent Error: Error downloading exml file");
		}
		j.jobCompleted();

		
		SfdcJobManager.updateJobStatus(jobId, "Completed",null);
	}
	
	public String downloadFile(String oAuthToken) {

		String DOWNLOAD_URL = JobManager.getProperty("SFDC_JOBDOWNLOAD_URL");
		
	    HttpClient httpClient = new HttpClient();
	    GetMethod getMethod = new GetMethod(DOWNLOAD_URL+"?testRunId="+runId+"&returnType=XML");
	    getMethod.setRequestHeader("Authorization", "Bearer "+oAuthToken);
//		getMethod.setRequestHeader("Accept", "application/json");


	    String status="";
	    try{
	  
	      //execute the method
	      int statusCode = httpClient.executeMethod(getMethod);
	      System.out.println("Status Code: "+statusCode);

	      //get the resonse as an InputStream
	      InputStream in = getMethod.getResponseBodyAsStream();

	      byte[] b = new byte[1024];
	      int len;
	      String fileName = "C:\\Temp\\job"+runId+".exml";
	      OutputStream out = new FileOutputStream(fileName);
	      while ((len = in.read(b)) != -1) {
	               //write byte to file
	               out.write(b, 0, len);
	      }

	      out.close();
	      in.close();
	      
	     Gson gson = new Gson();
	     return fileName;
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	    }
	    return null;
	}

}
