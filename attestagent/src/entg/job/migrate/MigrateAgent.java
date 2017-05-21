package entg.job.migrate;

import entg.job.JobAgent;
import entg.job.JobManager;
import entg.job.Job;
import entg.test.TCAgentMain;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.MultipartPostMethod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;


import java.sql.*;

public class MigrateAgent implements JobAgent {
	String jobId, programId;

	public MigrateAgent(String jobId, String programId, String parameters) {
		this.programId = programId;
		this.jobId = jobId;
		runId = Integer.parseInt(JobManager.getParameter(parameters, "RUNID"));
		runStepId = Integer.parseInt(JobManager.getParameter(parameters,
				"RUNSTEPID"));
		userId = Integer.parseInt(JobManager.getParameter(parameters,
		"USERID"));
	}

	int runId, runStepId, userId;
/*
	public void setFunctionStatus(Connection conn, int functionId, String status)
			throws Exception {
		CallableStatement cstmt = conn
				.prepareCall("begin ENTG_SETUP_UTILS.set_function_status(?,?,?); end;");

		cstmt.setInt(1, runStepId);
		cstmt.setInt(2, functionId);
		cstmt.setString(3, status);
		cstmt.execute(); 
		cstmt.close();

	}
*/
	public static String downloadFile(String url, String fileName) {
    HttpClient httpClient = new HttpClient();

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
 
      //execute the method
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
	      File f = new File(inExmlFile);
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
	public static String downloadXmlFile(int runId, int runStepId, int userId) throws Exception {

		String url=JobManager.getProperty("ATTEST_URL")+"/servlets/setupRun.jsp?RUN_ID="+runId+
		    "&RUN_STEP_ID="+runStepId+"&USER_ID="+userId;

		System.out.println("URL: "+url);
		
		
		String fileName1 = JobManager.getProperty("TMPDIR")+java.io.File.separator+
		       "run"+runId+System.currentTimeMillis()+".sxml";

		
		String status = downloadFile(url,fileName1);
		String fileName = JobManager.getProperty("TMPDIR")+java.io.File.separator+
	       "run"+runId+System.currentTimeMillis()+".sxml";
		exmlFix(fileName1,fileName);
		if (status.equals("ERROR"))
			  return null;
		else
              return fileName;
	}
		
	public void startJob(Job j) throws Exception {

		j.wlog("In Startjob");
		
		
		String sxmlFile=downloadXmlFile(runId, runStepId,userId);
		j.wlog("Download File ..done. File: "+sxmlFile);
		if (sxmlFile!=null) {
			  RunInfo t = RunInfo.initRunInfo(sxmlFile);
				
		      t.doRun(j.conn,j.out,this.runStepId);
			
		} else {
			j.wlog("Error Occured in MigrateAgent: Error downloading sxml file");
			j.out.flush();

			throw new Exception("Migrate Agent Error: Error downloading sxml file");
		}
		j.jobCompleted();
		
		

	}
	
		
	
}
