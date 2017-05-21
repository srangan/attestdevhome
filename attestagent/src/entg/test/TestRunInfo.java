package entg.test;

import java.io.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
//import org.apache.xpath.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.xml.sax.SAXException;
import java.io.*;
import java.util.*;
import java.util.Date;

import entg.util.DropboxUploader;
import entg.util.PasswordEncryption;

import org.apache.commons.digester.Digester;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;


import java.sql.*;
//import tstSimple.testSetup;
//import tstSimple.testSuiteBuilder;
public class TestRunInfo { 
	public String uploadUrl;

	private int runId ;
	private String suiteName;
	private String runName;
	private String runStatus;

	Vector scenarios;

	private String testRunDirectory;
	private String driversDir;
	private String testcasesDir;
	private String commonDir;
	private String logsDir;
	private String dataDir;
	private String previousRunsDir;
	private String testRootDir;

	private String globalSettingsFileName;
	
	
	
	// Create variables, getters and setters for passTestcases, failTestcases, totalTestcases 
	// and percentageComplete
	private int passTestcases;
	private int failTestcases;
	private int totalTestcases;
	private int percentageComplete;

	private TCAgentMain tc;
	
	public Vector globalDataVect;
	
	public void addGlobalData(String str) {
		globalDataVect.addElement(str);
	}
	
	public void setTCAgent(TCAgentMain tc) {
		this.tc=tc;
	}
	public int getFailTestcases() {
		return failTestcases;
	}

	public void setFailTestcases(int failTestcases) {
		this.failTestcases = failTestcases;
	}

	public int getPercenatgeComplete() {
		return percentageComplete;
	}

	public void setPercenatgeComplete(int percentageComplete) {
		this.percentageComplete = percentageComplete;
	}
	
	public int getPassTestcases() {
		return passTestcases;
	}

	public void setPassTestcases(int passTestcases) {
		this.passTestcases = passTestcases;
	}

	public int getTotalTestcases() {
		return totalTestcases;
	}

	public void setTotalTestcases(int totalTestcases) {
		this.totalTestcases = totalTestcases;
	} 
	

	public void  initializeTestRunStatus(){
		int num=0;
		System.out.println("In TR Status");
		for (int k=0;k<scenarios.size();k++){
			
			ScenarioInfo s2 = (ScenarioInfo) scenarios.elementAt(k);
			num+=s2.getTestCases().size();
			s2.initializeTestcaseStatus();
		}
	
		percentageComplete = 0;
		passTestcases = 0;
		failTestcases= 0;
		totalTestcases=num;
	}

	public void addScenario (ScenarioInfo s){
		scenarios.addElement(s);
	}

	public TestRunInfo() {

		scenarios=new Vector();
		globalDataVect = new Vector();
	}
	
	
	public void setRunId(int newrun) throws Exception {
		runId=newrun;
		
		File fin = new File(TCAgentMain.getParserDir());
		String s = new String(fin.getAbsolutePath());
		s = s.replace('.', '\\') + "\\\\"; 
		this.testRunDirectory= s + "test_runs\\run_" + runId;
		this.driversDir= this.testRunDirectory + "\\" + "drivers";
		this.testcasesDir = this.testRunDirectory+ "\\" + "testcases";
		this.commonDir = this.testRunDirectory + "\\" + "common";
		this.logsDir = this.testRunDirectory + "\\" + "logs";

		this.dataDir = this.testRunDirectory + "\\" + "data";
		this.previousRunsDir = this.testRunDirectory + "\\" + "previous_runs";
		
		// Create the directories where the files will be created
		System.out.println("Directory: " + testRunDirectory);
		File testExecDirectoryHandle = new File(testRunDirectory);
		if (testExecDirectoryHandle.isDirectory()) {
			Date dateStamp = new Date();
			System.out.println("*** Directory already exists...."
					+ testRunDirectory + "***");
			String backupFileName = testRunDirectory
					+ "_"
					+ dateStamp.toString().replace(' ', '_')
							.replace(':', '_');
			File backupFile = new File(backupFileName);
			if (testExecDirectoryHandle.renameTo(backupFile)) 
			  System.out.println("*** Directory backed up to" + "_"
					+ backupFileName + "***");
			else 
			   throw new Exception ("Could not backup existing test_runs directory. "+
					   testRunDirectory+"\nPlease check if some file from the directory is open in another program.");
			testExecDirectoryHandle = new File(testRunDirectory);
		}
		// Later: put this inside a try
		if (testExecDirectoryHandle.mkdir()) {
			System.out.println("New Directory" + testRunDirectory
					+ " created ");
			testExecDirectoryHandle = new File(this.testcasesDir);
			if (testExecDirectoryHandle.mkdir()) {
				System.out.println("New Directory" + testcasesDir
						+ " created ");
			}
			testExecDirectoryHandle = new File(this.driversDir);
			if (testExecDirectoryHandle.mkdir()) {
				System.out.println("New Directory" + driversDir
						+ " created ");
			}
			testExecDirectoryHandle = new File(this.commonDir);
			if (testExecDirectoryHandle.mkdir()) {
				System.out.println("New Directory" + commonDir
						+ " created ");
			}
			testExecDirectoryHandle = new File(this.logsDir);
			if (testExecDirectoryHandle.mkdir()) {
				System.out.println("New Directory" + logsDir
						+ " created ");
			}

			testExecDirectoryHandle = new File(this.dataDir);
			if (testExecDirectoryHandle.mkdir()) {
				System.out.println("New Directory" + dataDir
						+ " created ");
			}
			

		}
		
		
		
	}
	
	public int getRunId(){
		return runId;
	}
	
	public void setSuiteName(String newsuite){
		suiteName=newsuite;
	}
	
	public String  getSuiteName(){
		return suiteName;
	}
		
	public Vector getScenarios() {
		return scenarios;
	}

	public String getCommonDir() {
		return commonDir;
	}
	public void setCommonDir(String commonDir) {
		this.commonDir = commonDir;
	}
	public String getDataDir() {
		return dataDir;
	}
	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}
	public String getDriversDir() {
		return driversDir;
	}
	public void setDriversDir(String driversDir) {
		this.driversDir = driversDir;
	}
	public String getLogsDir() {
		return logsDir;
	}
	public void setLogsDir(String logsDir) {
		this.logsDir = logsDir;
	}
	public String getPreviousRunsDir() {
		return previousRunsDir;
	}
	public void setPreviousRunsDir(String previousRunsDir) {
		this.previousRunsDir = previousRunsDir;
	}
	public String getTestcasesDir() {
		return testcasesDir;
	}
	public void setTestcasesDir(String testcasesDir) {
		this.testcasesDir = testcasesDir;
	}
	public String getTestRunDirectory() {
		return testRunDirectory;
	}
	public void setTestRunDirectory(String testRunDirectory) {
		this.testRunDirectory = testRunDirectory;
	}
	public String getTestRootDir() {
		return testRootDir;
	}
	public void setTestRootDir(String testRootDir) {
		this.testRootDir = testRootDir;
	}

    
	public static TestRunInfo initRunInfo(String exmlFile) throws IOException,SAXException{
		Digester digester = new Digester();
		digester.setValidating(false);

		// instantiate testClientXMLParser class
		digester.addObjectCreate("agentdriver", TestRunInfo.class);
		// set type property of Contact instance when 'filename' attribute is
		// found
		digester.addSetProperties("agentdriver", "run_id", "runId");
		digester.addSetProperties("agentdriver", "run_name", "runName");
		digester.addSetProperties("agentdriver/testrundata", "filename","globalSettingsFileName");
		digester.addCallMethod("agentdriver/testrundata/data",
				"addGlobalData", 0);
		
		digester.addSetProperties("agentdriver/suite", "name","suiteName");
						
		
		
		digester.addObjectCreate("agentdriver/suite/scenario", ScenarioInfo.class);
		digester.addSetProperties("agentdriver/suite/scenario", "name",
				"scenarioName");
		digester.addObjectCreate("agentdriver/suite/scenario/appsignondata", AppSignInfo.class);
	
		
		digester.addBeanPropertySetter("agentdriver/suite/scenario/appsignondata/script",
		"appSignOnScript");
		
		digester.addObjectCreate("agentdriver/suite/scenario/appsignondata/parameter", FunctionParamInfo.class);
		digester.addSetProperties("agentdriver/suite/scenario/appsignondata/parameter",
				"name", "paramName");
		digester.addSetProperties("agentdriver/suite/scenario/appsignondata/parameter",
				"type", "paramType");
		digester.addSetNext( "agentdriver/suite/scenario/appsignondata/parameter", "addFunctionParam" );
		
		digester.addCallMethod("agentdriver/suite/scenario/appsignondata/data",
				"addData", 0);				
		digester.addSetNext( "agentdriver/suite/scenario/appsignondata", "addFunction" );
		

		digester.addObjectCreate("agentdriver/suite/scenario/function", FunctionInfo.class);
		digester.addSetProperties("agentdriver/suite/scenario/function",
				"name", "functionName");
		digester.addSetProperties("agentdriver/suite/scenario/function",
				"type", "functionType");
		digester.addBeanPropertySetter("agentdriver/suite/scenario/function/location",
				"functionScript");
		
		digester.addObjectCreate("agentdriver/suite/scenario/function/parameter", FunctionParamInfo.class);
		digester.addSetProperties("agentdriver/suite/scenario/function/parameter",
				"name", "paramName");
		digester.addSetProperties("agentdriver/suite/scenario/function/parameter",
				"type", "paramType");
		digester.addSetNext( "agentdriver/suite/scenario/function/parameter", "addFunctionParam" );
		
		digester.addSetNext( "agentdriver/suite/scenario/function", "addFunction" );
		
		digester.addObjectCreate("agentdriver/suite/scenario/testcases/testcase", TestcaseInfo.class);
		digester.addSetProperties(
				"agentdriver/suite/scenario/testcases/testcase", "lastpassfunc",
				"lastpassfunc");		
		digester.addSetProperties(
				"agentdriver/suite/scenario/testcases/testcase", "overridedatafile",
				"overridedatafile");		 
		digester.addSetProperties(
				"agentdriver/suite/scenario/testcases/testcase", "name",
				"testcaseName");
		
		digester.addCallMethod("agentdriver/suite/scenario/testcases/testcase/functionids",
				"setFunctionRunIds", 0);
		
		digester.addCallMethod("agentdriver/suite/scenario/testcases/testcase/data",
				"addData", 0);
		
				
		digester.addSetNext( "agentdriver/suite/scenario/testcases/testcase", "addTestcase" );
		
		
		digester.addSetNext( "agentdriver/suite/scenario", "addScenario" );
		
		
		
        TestRunInfo t = (TestRunInfo) digester.parse( new File(exmlFile) );


 
	    return t;
	    
	}

	public void setFunctionStarted (String testcasename, 
		    String funcSeq) {
		
	    
		boolean statusDone = false;
		for (int j = 0; j < scenarios.size()&&!statusDone; j++) {
            ScenarioInfo s1 = (ScenarioInfo) scenarios.elementAt(j);
            
			Vector testcases = s1.getTestCases();    
			
			for (int i = 0; i < testcases.size()&&!statusDone; i++) {
				TestcaseInfo tst1 = (TestcaseInfo) testcases.elementAt(i);
				if (tst1.getTestcaseName().equals(testcasename)) {
		           tst1.setFunctionStatus(Integer.parseInt(funcSeq)-1,"RUNNING");
		           testcases.setElementAt(tst1,i);
		           statusDone = true;
		           
				}
			}
			scenarios.setElementAt(s1,j);
		}		
		
	}

	public void setFunctionPerfMetrics(String testcasename, 
			String funcSeq,String stime, String etime) throws Exception {
		
		Connection conn = TCAgentMain.getMetricDBConn();
		
		CallableStatement cstmt1 = conn.prepareCall ("begin ENTG_UTILS.set_testcase_func_perfmetrics(?,?,?,?); end;"); 

    cstmt1.setString(1, testcasename );
    cstmt1.setInt(2, Integer.parseInt(funcSeq));
    cstmt1.setString(3, stime);
    cstmt1.setString(4, etime);
    cstmt1.execute();
    cstmt1.close();      	 			

	}
	public void setFunctionStatus(TestcaseInfo testcase, String testcasename, String funcSeq,
			String logFile, String status) throws IOException, Exception {
		setFunctionStatus (testcase, testcasename, funcSeq,logFile,status,"","");
	}	
	public void setFunctionStatus(TestcaseInfo testcase,String testcasename, String funcSeq,
				String logFile, String status, String stime, String etime) throws Exception {	
		String logUrl= uploadFile(logFile);
	
		if (TCAgentMain.sfdcConsole()) {

		   UpdateSFDCStatus.sfdcUpdateStatus(testcase.getFunctionRunId(Integer.parseInt(funcSeq)),funcSeq,testcasename,""+this.runId,status,logUrl,stime,etime);
			 
		} else if (TCAgentMain.dbMethod()) {
			System.out.println ("***** Using DB Method");
			Connection conn = TCAgentMain.getMetricDBConn();

			File logFileF = new File(logFile);
			CallableStatement cstmt1 = conn
					.prepareCall("begin ENTG_UTILS.set_testcase_function_status1(?,?,?,?); end;");

			cstmt1.setString(1, status);
			cstmt1.setString(2, logFileF.getName());
			cstmt1.setString(3, testcasename);
			cstmt1.setInt(4, Integer.parseInt(funcSeq));

			cstmt1.execute();
			cstmt1.close();
			
			setFunctionPerfMetrics(testcasename, funcSeq,stime,etime);
			
		} else {
			
			System.out.println ("***** Using HTTP Method");
			
			File logFileF = new File(logFile);
			String[] params = {"TESTCASENAME","FUNCSEQ","LOGFILE","STATUS","STIME","ETIME"};
			String[] paramVals = {testcasename, funcSeq,logFileF.getName(),status,stime,etime};
			int numParams = params.length;
			 
			System.err.println("Params "+params);
			System.err.println("paramVals "+paramVals);
			doPostToAgentOperations("SET_FUNCTION_STATUS",params,paramVals,numParams);
			
		}
			
		boolean statusDone = false;
		for (int j = 0; j < scenarios.size() && !statusDone; j++) {
			ScenarioInfo s1 = (ScenarioInfo) scenarios.elementAt(j);

			Vector testcases = s1.getTestCases();

			for (int i = 0; i < testcases.size() && !statusDone; i++) {
				TestcaseInfo tst1 = (TestcaseInfo) testcases.elementAt(i);
				if (tst1.getTestcaseName().equals(testcasename)) {
					tst1.setFunctionStatus(Integer.parseInt(funcSeq) - 1,
							status);
					testcases.setElementAt(tst1, i);
					statusDone = true;

				}
			}
			scenarios.setElementAt(s1, j);
		}

		int localPassTestcases = 0;
		int localFailTestcases = 0;
		int localPercComplete;

		for (int k = 0; k < scenarios.size(); k++) {
			ScenarioInfo s2 = (ScenarioInfo) scenarios.elementAt(k);
			Vector testcases = s2.getTestCases();

			for (int i = 0; i < testcases.size(); i++) {

				TestcaseInfo tst1 = (TestcaseInfo) testcases.elementAt(i);

				if (tst1.getStatus().equals("PASS")) {
					localPassTestcases++;
				}
				if (tst1.getStatus().equals("FAIL")) {
					localFailTestcases++;
				}
			}
		}

		TestRunner.out
				.println((double) (localPassTestcases + localFailTestcases));
		TestRunner.out.println((double) totalTestcases);
		TestRunner.out
				.println((double) (localPassTestcases + localFailTestcases)
						/ (double) totalTestcases);
		localPercComplete = (int) (((double) (localPassTestcases + localFailTestcases) / (double) totalTestcases) * 100);
		TestRunner.out.println("Perc Complete " + localPercComplete);
		this.setPercenatgeComplete(localPercComplete);
		setPassTestcases(localPassTestcases);
		setFailTestcases(localFailTestcases);
		
		tc.setRepaint();
	}

    public void doPostToAgentOperations (String operation, String[] params, String[] paramVals, int numParams) 
      throws Exception {
    	HttpClient client = new HttpClient();
    	
    	String proxyHost = TCAgentMain.TCProperties.getProperty("HTTP_PROXY_HOST");
    	if (proxyHost != null && !proxyHost.equals("")) {
    		int proxyPort = 80;
    		
    		String p = TCAgentMain.TCProperties.getProperty("HTTP_PROXY_PORT");
    		if (p!= null && !p.equals("")) 
    		   proxyPort = Integer.parseInt(p);
    		
    		client.getHostConfiguration().setProxy(proxyHost, proxyPort);    	
            client.getParams().setParameter("http.useragent", "Test Client");    		
    	}
    	
    	
    	
    	
    	

        BufferedReader br = null;

        String servletPrefix = "servlets";
        
        String msV = TCAgentMain.TCAgentproperties("MS_VERSION_6");
        if (msV != null && msV.equalsIgnoreCase("yes")) 
        	servletPrefix = "metricstream";
        
        PostMethod method = new PostMethod(TCAgentMain.TCAgentproperties("ATTEST_URL")+"/"+servletPrefix+"/AgentOperationsServlet");
        
        method.addParameter("OPERATION", operation);
    	System.out.println("OPERATION="+operation);
        for (int i=0;i<numParams;i++) {
        	method.addParameter(params[i], paramVals[i]);
        	System.out.println(params[i]+"="+paramVals[i]);
        }

        int returnCode = client.executeMethod(method);
 
        if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
            System.err.println("The Post method is not implemented by this URI");
            // still consume the response body
            method.getResponseBodyAsString();
        } else {
            br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            String readLine;
            while(((readLine = br.readLine()) != null)) {
              if (readLine.startsWith("ERROR")) {
             	System.out.println("RESPONSE FROM AGENTHANDLER SERVLET:" +readLine);  
                System.err.println("RESPONSE FROM AGENTHANDLER SERVLET:" +readLine);
                throw new Exception("Error updating status to server. "+br.readLine());
              } else {
            	  System.out.println("RESPONSE FROM AGENTHANDLER SERVLET:" +readLine);  
                  System.err.println("RESPONSE FROM AGENTHANDLER SERVLET:" +readLine);  
              }
            }
        }
    }
	
	
    public static String uploadFile (String fileName) {
    	
    	
    	 
    	try {
    		
    		String dropBox = TCAgentMain.TCAgentproperties("DROPBOX_UPLOAD");
    		if (dropBox != null && dropBox.equals("yes")) {
    			return DropboxUploader.uploadFile(fileName);
    			
    		}
    		
            String servletPrefix = "servlets";
            
            String msV = TCAgentMain.TCAgentproperties("MS_VERSION_6");
            if (msV != null && msV.equalsIgnoreCase("yes")) 
            	servletPrefix = "metricstream";
                	  
		 String uploadUrl=TCAgentMain.TCAgentproperties("ATTEST_URL")+"/"+servletPrefix+"/UploadServlet";
		 
		 System.out.println("Upload URL :"+uploadUrl);
		 HttpClient client = new HttpClient();
		 
			
	    	String proxyHost = TCAgentMain.TCProperties.getProperty("HTTP_PROXY_HOST");
	    	if (proxyHost != null && !proxyHost.equals("")) {
	    		int proxyPort = 80;
	    		
	    		String p = TCAgentMain.TCProperties.getProperty("HTTP_PROXY_PORT");
	    		if (p!= null && !p.equals("")) 
	    		   proxyPort = Integer.parseInt(p);
	    		
	    		client.getHostConfiguration().setProxy(proxyHost, proxyPort);    	
	            client.getParams().setParameter("http.useragent", "Test Client");    		
	    	}
	    			 
		 
		 
	    	MultipartPostMethod filePost =
	    		new MultipartPostMethod(uploadUrl);
	    	client.setConnectionTimeout(8000);
	    	
    	// Send any XML file as the body of the POST request
    	File f = new File(fileName);
		if (f.isFile()) 
			System.out.println("File is normal");
	    else
	    	System.out.println("File is not normal");    	
    	filePost.addParameter(f.getName(), f);
    	System.out.println(f.getName());
    	client.setConnectionTimeout(5000);
    	int status = client.executeMethod(filePost);
    	filePost.releaseConnection();
    	
    	System.out.println("Upload Done "+status);  
    	
    	return TCAgentMain.TCAgentproperties("ATTEST_URL")+"/testresults/"+f.getName().toLowerCase();
    	//http://metricstream.entegration.com/testresults/report_test11239_1.qjar
    	} catch (Exception e) {
    		e.printStackTrace(System.out);
    		return null;
    	}
    }

	public String getRunName() {
		return runName;
	}

	public void setRunName(String runName) {
		this.runName = runName;
	}

	public String getRunStatus() {
		if ((failTestcases + passTestcases) == 0){
			setRunStatus("Processing");
		}	
			else if (failTestcases== totalTestcases) {
				setRunStatus("Complete With Errors");
		}
			else if (passTestcases == totalTestcases){
				setRunStatus("Completed");
			}
			else setRunStatus("New");
		return runStatus;
	}

	public void setRunStatus(String runStatus) {
		this.runStatus = runStatus;
	}
	public String getGlobalSettingsFileName() {
		return globalSettingsFileName;
	}
	public void setGlobalSettingsFileName(String globalSettingsFileName) {
		this.globalSettingsFileName = globalSettingsFileName;
	}
	
	public void generateGlobalSettingFile() throws Exception {
		PrintWriter out=new PrintWriter(new BufferedWriter(
				  new FileWriter(this.getDataDir()+File.separator+this.globalSettingsFileName)));
	    for (int i=0;i<globalDataVect.size();i++) {
	    	out.println((String)globalDataVect.elementAt(i));
	    }
	    out.close();
	}

	
}

