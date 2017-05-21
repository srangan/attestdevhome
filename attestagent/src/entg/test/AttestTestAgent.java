package entg.test;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.MultipartPostMethod;

import com.google.gson.Gson;

import entg.job.Job;
import entg.job.JobAgent;
import entg.job.JobManager;

public class AttestTestAgent implements JobAgent {
	String jobId, programId;
	
    String errorFlag;
	public AttestTestAgent(String jobId, String programId, String parameters) {
		this.programId = programId;
		this.jobId = jobId;
		runId = Integer.parseInt(JobManager.getParameter(parameters, "RUNID"));
		errorFlag = JobManager.getParameter(parameters,"ERRORFLAG");
		if (errorFlag==null)
			errorFlag="N";
		
	}

	int runId;

	
	public static String downloadFile(String url, String fileName) throws Exception {
	    HttpClient httpClient = new HttpClient();
		
	    TCAgentMain.loadProps();
    	String proxyHost = TCAgentMain.TCProperties.getProperty("HTTP_PROXY_HOST");
    	if (proxyHost != null && !proxyHost.equals("")) {
    		int proxyPort = 80;
    		
    		String p = TCAgentMain.TCProperties.getProperty("HTTP_PROXY_PORT");
    		if (p!= null && !p.equals("")) 
    		   proxyPort = Integer.parseInt(p);
    		
    		httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);    	
    		httpClient.getParams().setParameter("http.useragent", "Test Client");    		
    	}
    		  
	    //create a method instance
	    GetMethod getMethod = new GetMethod(url);

	    String status="";
	    try{
	  
	      //execute the methd
	      int statusCode =
	             httpClient.executeMethod(getMethod);

	      //get the resonse as an InputStream
	      InputStream in =
	             getMethod.getResponseBodyAsStream();

	      byte[] b = new byte[1024];
	      int len;
	      OutputStream out = new FileOutputStream(fileName);
	      while ((len = in.read(b)) != -1) {
	               //write byte to file
	               out.write(b, 0, len);
	      }

	      out.close();
	      in.close();

	      status = "SUCCESS";
	     }catch(HttpException e){
	       //do something
	       e.printStackTrace();
	       status="ERROR";
	     }catch(IOException e){
	       //do something else
	       e.printStackTrace();
	       fileName="ERROR";
	     }finally{
	       //release the connection
	       getMethod.releaseConnection();
	     }
	     return status;

	}
		
	public void downloadFileFromSfdc(String runId) {

		String DOWNLOAD_URL = JobManager.getProperty("SFDC_JOBDOWNLOAD_URL");

	    HttpClient httpClient = new HttpClient();
	    GetMethod getMethod = new GetMethod(DOWNLOAD_URL+"?testRunId="+runId+"&returnType=XML");
	    
	    // get the SFDC oauth token
	    String accessToken = System.getProperty("sfdc.oauth.token");
	    
	    getMethod.setRequestHeader("Authorization", "Bearer "+accessToken);
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
	      OutputStream out = new FileOutputStream("C:\\Temp\\text.xml");
	      while ((len = in.read(b)) != -1) {
	               //write byte to file
	               out.write(b, 0, len);
	      }

	      out.close();
	      in.close();
	      
	     Gson gson = new Gson();
	     
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	    }
	}

		
		public static String strip( final String s )
		   {
		     String res = "";
		     for ( int i=0; i<s.length(); i++ )
		     {
		       if ( s.charAt(i)!=' ' )
		         res = res +  s.charAt(i);
		     }
		     return res;
		   }

		public static void exmlFix(String inExmlFile, String outExmlFile) throws IOException {
		      
		      BufferedReader br = new BufferedReader(new FileReader(inExmlFile)); 
		     PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(outExmlFile))); 
		      String lin = br.readLine();
		      while (lin != null)  {
		        
		        if (!strip(lin).equals("")) 
		          bw.println(lin);
		        lin = br.readLine();
		      }
		      br.close();
		      bw.close();
		   
		    }	
		public static String downloadXmlFile(int runId, String errorFlag) throws Exception {

			
			
			String url=JobManager.getProperty("ATTEST_URL")+"/servlets/testrun.jsp?RUN_ID="+runId;

			if (errorFlag.equals("Y")) 
			  url=JobManager.getProperty("ATTEST_URL")+"/servlets/testrunError.jsp?RUN_ID="+runId;
			
			String fileName1 = JobManager.getProperty("TMPDIR")+java.io.File.separator+
			       "run"+runId+System.currentTimeMillis()+".exml";

			
			String status = downloadFile(url,fileName1);
			String fileName = JobManager.getProperty("TMPDIR")+java.io.File.separator+
		       "run"+runId+System.currentTimeMillis()+".exml";
			exmlFix(fileName1,fileName);
			if (status.equals("ERROR"))
				  return null;
			else
	              return fileName;
		}

		public void startJob(Job j) throws Exception {
			
			String sfdcJob = JobManager.getProperty("SFDC_CONSOLE");
			if(sfdcJob != null && sfdcJob.equalsIgnoreCase("yes")) {
				// SDFC job run logic here
				downloadFileFromSfdc(new Integer(runId).toString());
				return;
			}

			j.wlog("In Startjob");
			
			
			Statement stmt = j.conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					 "select upper(run_status) from entg_test_runs where run_id = "+runId);
			rs.next();
			String status = rs.getString(1);
			stmt.close();
			
			if (status.equals("NEW")) {
				
				entg.util.TCTestcaseGenerator.genTC(j.conn, runId,"LOCAL",null,false,null);
			}
			
			String exmlFile=downloadXmlFile(runId,this.errorFlag);
			j.wlog("Download File ..done. File: "+exmlFile);
			if (exmlFile!=null) {
				String[] args = {""+exmlFile};  
				TCAgentMain.jobMain(args,j.out);
			} else {
				j.wlog("Error Occured in Test Agent: Error downloading exml file");
				j.out.flush();

				throw new Exception("Test Agent Error: Error downloading exml file");
			}
			j.jobCompleted();
			uploadLogFile(JobManager
								.getProperty("JOB_LOG_DIR")
								+ File.separator + j.logFileName);
			

		}
		
		public void uploadLogFile(String logFile) {
			  try {
				String uploadUrl = JobManager.getProperty("ATTEST_URL")
				+ "/servlets/UploadServlet";
				HttpClient client = new HttpClient();
				MultipartPostMethod filePost = new MultipartPostMethod(uploadUrl);
				client.setConnectionTimeout(8000);

				// Send any XML file as the body of the POST request
				File f = new File(logFile);
				filePost.addParameter(f.getName(), f);

				client.setConnectionTimeout(5000);
				int status = client.executeMethod(filePost);
				filePost.releaseConnection();
				
				
			  } catch (Exception e) {
				  e.printStackTrace();
			  }

			}
			
}
