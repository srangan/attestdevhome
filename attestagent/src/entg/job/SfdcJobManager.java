package entg.job;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
//import com.sforce.soap.ApplicationMethods.Connector;
//import com.sforce.soap.ApplicationMethods.SoapConnection;
//import com.sforce.soap.ApplicationMethods.AttestV2__Job__c;
//import com.sforce.soap.RemoteManagerMethods.CreateJobManagerClass;


//import com.sforce.ws.ConnectionException;
//import com.sforce.ws.ConnectorConfig;
//import com.sforce.soap.enterprise.*; 



import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import entg.job.AuthResponse;
import entg.job.PollResponse;
//import entg.test.PollResponse;
//import entg.test.Scenario;
import entg.test.TCAgentMain;
//import entg.test.TCase;
//import entg.test.TestCaseWithParameters;
//import entg.test.TestRunModel;
import entg.util.*;


public class SfdcJobManager {
	
	static Properties jobMgrProps;
	static AuthResponse authResponse;
	private static String agentId;
	private static AuthResponse authResp;
	private static PollResponse pollResp;
	private static String testRunId;


	public static void loadProperties()  throws Exception {
		jobMgrProps = new Properties();
		FileInputStream fin = new FileInputStream(System.getProperty("attest.path")+File.separator+"attest.properties");
		jobMgrProps.load(fin);
	}
	
	public static String getProperty(String name) {
		return jobMgrProps.getProperty(name);
	}
	
	public static void setMgrStatus (String stat, String mgrId) throws Exception {
		
		// SFDC Call to set job status
	}
	static String mgrType;
	
//	 static SoapConnection MyWebserviceWSconnection;
//	  static EnterpriseConnection enterpriseConnection;
//	  static ConnectorConfig config = null;

/**
	public static void initSFDCConn() throws Exception {

		String USERNAME = jobMgrProps.getProperty("SFDC_USERNAME");//"attestdev@45demo.com";
        String PASSWORD = jobMgrProps.getProperty("SFDC_PASSWORD");//"attest@123Othi3MSL6zS4iUBRVdYPYsxD7";

        String password = "";
		if (PASSWORD != null && !PASSWORD.equals("")) {
			   
			  entg.util.PasswordEncryption pwp = new entg.util.PasswordEncryption();
			  password = pwp.decrypt(PASSWORD);
		    }
			
	    config = new ConnectorConfig();  
	    config.setUsername(USERNAME);
	    config.setPassword(password);
	    
	    System.out.println("Pass: "+password);
	    
        enterpriseConnection = com.sforce.soap.enterprise.Connector.newConnection(config);    
	      
	}
*/
/**
	public static String initMgr() throws Exception {

		
		com.sforce.soap.RemoteManagerMethods.SoapConnection remoteManagerWebserviceConn = 
				com.sforce.soap.RemoteManagerMethods.Connector.newConnection("","");
		remoteManagerWebserviceConn.setSessionHeader(config.getSessionId());
		// SFDC Call to create manager
		mgrType = jobMgrProps.getProperty("MANAGER_TYPE");
        String mgrName = jobMgrProps.getProperty("MANAGER_NAME");
        if (mgrType==null)
        	mgrType="SERVER";
        
        
        CreateJobManagerClass jobMgr = new CreateJobManagerClass();
        jobMgr.setManagerName(mgrName);
        jobMgr.setType(mgrType);
        jobMgr.setNodeName(java.net.InetAddress.getLocalHost().getHostName());
        jobMgr.setDescription("Job Manager - "+mgrName+" - running on "+
        		java.net.InetAddress.getLocalHost().getHostName()+", handling program ID: "+
        		jobMgrProps.getProperty("MANAGER_PROGRAMS"));
        jobMgr.setStatus("Alive"); 
		
        String resp = remoteManagerWebserviceConn.CreateJobManager(jobMgr);  
        System.out.println("resp="+resp);
        String mgrId = resp.substring(47);
        System.out.println(mgrId);
		return mgrId;
	}
*/
	/**
	public static Job getEligibleJob(String mgrId) throws Exception {
		//new Job(jobid,programid,programname,programClass,parameters);
	//	if (!h.containsKey(""+prgId)) 
		//	continue;
				
		com.sforce.soap.ApplicationMethods.SoapConnection appMethodsWebserviceConn = 
				com.sforce.soap.ApplicationMethods.Connector.newConnection("","");
		appMethodsWebserviceConn.setSessionHeader(config.getSessionId());
		
		String jobId = appMethodsWebserviceConn.getEligibleJob(mgrId);
		
		if (jobId.equalsIgnoreCase("no jobs found")) return null;
		AttestV2__Job__c jobD = appMethodsWebserviceConn.getJobDetails(jobId);
		Job j = new Job(jobD.getId(),jobD.getAttestV2__Job_Program__r().getAttestV2__Program_Id__c(),
				jobD.getAttestV2__Job_Program__r().getName(),jobD.getAttestV2__Job_Program__r().getAttestV2__Program_Implementation_Class__c(),
				jobD.getAttestV2__Parameters__c());
		
	    // Job = appMethodsWebserviceConn.getJobDetails();
		
		
		return j;
	}
*/
/**
	public static void updateJobStatus(String jobId,String status,String logFile) throws Exception  {
		com.sforce.soap.ApplicationMethods.SoapConnection appMethodsWebserviceConn = 
				com.sforce.soap.ApplicationMethods.Connector.newConnection("","");
		
		appMethodsWebserviceConn.updateJobStatus(jobId, status, logFile, null, null);
	}
	
*/ 
	public static void launch(String args[]) throws Exception {
		
		System.out.println("====> Launching SFDC Job Manager...");
		loadProperties();
		
//		initSFDCConn();
		doAuth();
//		String mgrId = initMgr();

		doRegister();

		String mgrProgs = jobMgrProps.getProperty("MANAGER_PROGRAMS");
		StringTokenizer st = new StringTokenizer(mgrProgs,",");
		java.util.Hashtable h = new java.util.Hashtable();
		while (st.hasMoreTokens()) {
		  	h.put(st.nextToken(), "");
		}
	
		//String retrievalMethod = args[0];
		while (true) {
	        System.out.println((new java.util.Date()).toString() + ": Polling ...");
	            
	        // get eligibile job
//	        Job j = getEligibleJob(mgrId); 
	        Job j = doPoll();
	        if (j!=null) {

	        	System.out.println("Got Job :"+j.jobId);
	        	j.run();
	        }
			
			if (!j.inited) continue;
					
//			setMgrStatus("Busy",mgrId);
			
//			j.run();
			
//			System.out.println("Completed Job:"+j.jobId);
	        Thread.sleep(20000);
	        }
			
//			setMgrStatus("Alive",mgrId);
		
			
			
		}

	
/**
 * OAuth authentication with SFDC cloud. Gets an access token to be used with
 * subsequent API calls to SFDC
 */
	public static void doAuth() {
		
		System.out.println("====> OAuth authentication with Sales Force...");
		
		// get required paramenetrs from properties
		String SFDC_OAUTH_TOKEN_URL = jobMgrProps.getProperty("SFDC_OAUTH_TOKEN_URL");
		String CLIENT_ID = jobMgrProps.getProperty("CLIENT_ID");
		String CLIENT_SECRET = jobMgrProps.getProperty("CLIENT_SECRET");
		String USER_NAME = jobMgrProps.getProperty("SFDC_USERNAME");
		String PASSWORD = jobMgrProps.getProperty("SFDC_PASSWORD");
		
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(SFDC_OAUTH_TOKEN_URL);
		postMethod.addParameter("client_id", CLIENT_ID);
		postMethod.addParameter("client_secret", CLIENT_SECRET);
		postMethod.addParameter("grant_type", "password");
		postMethod.addParameter("username", USER_NAME);
		postMethod.addParameter("password", PASSWORD);
		//postMethod.setParams(params);
		
	    String status="";
	    try{
	  
	      //execute the methd
	      int statusCode = httpClient.executeMethod(postMethod);
	      System.out.println("==Status Code: "+statusCode);
	      String resp = postMethod.getResponseBodyAsString();
	      System.out.println(resp);
	      
	      Gson gson = new Gson();
	      authResponse = gson.fromJson(resp, AuthResponse.class);
	      System.out.println("Access Token: " + authResponse.getAccess_token());
	      System.setProperty("sfdc.oauth.token", authResponse.getAccess_token());
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	    }
	}
	

	
	public static Job doPoll() {
		String SFDC_JOBPOLL_URL = jobMgrProps.getProperty("SFDC_JOBPOLL_URL");
		
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(SFDC_JOBPOLL_URL);
		getMethod.setRequestHeader("Authorization", "Bearer "+authResponse.getAccess_token());
		getMethod.setRequestHeader("Accept", "application/json");
		getMethod.addRequestHeader("jobManagerId", agentId);
		System.out.println("Poll URL: "+SFDC_JOBPOLL_URL);
		try {
			int statusCode = httpClient.executeMethod(getMethod);
		      System.out.println("Poll request: Status Code: "+statusCode);
		      String resp = getMethod.getResponseBodyAsString();
		      System.out.println(resp);

		      Gson gson = new Gson();
		      pollResp = gson.fromJson(resp, PollResponse.class);
		      testRunId = pollResp.getJob().getAttestV2__Test_Run__c();//pollResp.getJob().getId();
		      System.out.println("Test Run ID: " + testRunId);

		      // TO DO instantiate Job from the response
		      String progName = "Test Run";//pollResp.getJobProgram().getName();
		      System.out.println("Program Name: "+progName);
		      String progId = "0003";// pollResp.getJobProgram().getId();
		      System.out.println("Program Id: "+progId);
		      String params = pollResp.getParams();
		      String progClass = "entg.test.AttestTestAgentDriver";//pollResp.getJobProgram().getAttestV2__Program_Implementation_Class__c();
		      System.out.println("Params: "+params);
		      String jobId = pollResp.getJob().getId();
		      Job job = new Job(jobId,progId,progName,progClass,params);
		      return job;

		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;

	}
	


	

	/**
	 * Agent registers itself with the platform on first attempt
	 */
		public static void doRegister() {
			String SFDC_AGENT_REGISTER_URL = jobMgrProps.getProperty("SFDC_AGENT_REGISTER_URL");
			String agentName = jobMgrProps.getProperty("REMOTE_AGENT_NAME");
			System.out.println("Remote agent name: "+agentName);
			HttpClient httpClient = new HttpClient();
			PostMethod postMethod = new PostMethod(SFDC_AGENT_REGISTER_URL);
			postMethod.setRequestHeader("Authorization", "Bearer "+authResponse.getAccess_token());
			postMethod.setRequestHeader("Accept", "application/json");
			postMethod.addParameter("name", agentName);
			try {
				int statusCode = httpClient.executeMethod(postMethod);
			      System.out.println("Register request: Status Code: "+statusCode);
			      String resp = postMethod.getResponseBodyAsString();
			      Gson gson = new Gson();
			      agentId=resp;
			      System.out.println(resp);

			}
			catch(Exception ex) {
				ex.printStackTrace();
			}


		}  
		
		public static void updateJobStatus(String jobId, String status, String log) {
			String SFDC_JOBPOLL_URL = jobMgrProps.getProperty("SFDC_JOBPOLL_URL");
			HttpClient httpClient = new HttpClient();
			PostMethod postMethod = new PostMethod(SFDC_JOBPOLL_URL);
			postMethod.setRequestHeader("Authorization", "Bearer "+authResponse.getAccess_token());
			postMethod.setRequestHeader("Accept", "application/json");
			postMethod.addParameter("jobId", jobId);
			postMethod.addParameter("status", status);
			try {
				int statusCode = httpClient.executeMethod(postMethod);
			      System.out.println("Poll request: Status Code: "+statusCode);
			      String resp = postMethod.getResponseBodyAsString();
			      System.out.println("UPDATE RESPONSE: "+resp);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
/*
		private void parseJson() {
			Gson gson = new Gson();
			Reader reader = null;
			try {
				reader = new BufferedReader(new FileReader(new File("C:\\Temp\\text.json")));
				Type type = new TypeToken<List<TestRunModel>>(){}.getType();
				
				List<TestRunModel> data = gson.fromJson(reader, type);

				System.out.println("Size: "+data.size());
				
				for(TestRunModel model : data) {
					Scenario s = null;//model.getScenario();
					System.out.println("Id: "+s.getId());
					System.out.println("Name: "+s.getName());
					
					List<TestCaseWithParameters> tcs = model.getTestCaseWithParameters();
					System.out.println("TCS size: "+tcs.size());
					
//					for(TestCaseWithParameters tcp : tcs) {
//						TCase tc = tcp.getTCase();
//						Map <String, String> atts = tc.getAttributes();
//						System.out.println("Type: "+atts.get("type").toString());
//						System.out.println("Url: "+atts.get("url").toString());
//					}
					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
*/

}
