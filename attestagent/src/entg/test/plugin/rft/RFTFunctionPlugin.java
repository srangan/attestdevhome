package entg.test.plugin.rft;
import entg.test.*;
import entg.test.plugin.FunctionPluginInterface;
import entg.util.*;
import java.io.*;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;


public class RFTFunctionPlugin implements entg.test.plugin.FunctionPluginInterface {


	TestRunInfo testrun;
	TestcaseInfo testcase;
	FunctionInfo func;

    static AppSignInfo lastAppSign = null;
    static String lastLogFile=null;

    public String getType() {
    	return "RFT";
    }


	boolean isAppSignon=false;
	public RFTFunctionPlugin (TestRunInfo testrun) {
		this.testrun=testrun;
	}


    public void startTestcase(TestcaseInfo testcase) {
    	this.testcase =testcase;
    }
    
    public String doSignon(AppSignInfo appsign, int funcSeq) throws Exception {

    	if (lastAppSign!= null && lastAppSign==appsign) {
    		System.out.println("***** Skipping signon ");
    		testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
 				   ,lastLogFile, "PASS");
    		return "PASS";

    	}

    	lastAppSign=appsign;

    	String logDir = testrun.getLogsDir()+File.separator+
           appsign.getFunctionScript().replace(' ', '_')+"_"+testcase.getTestcaseId()+"_"+funcSeq;

		File logDirHandle = new File(logDir);
		if (logDirHandle.isDirectory()) {
			throw new Exception("Dir already exists");
		} else
			logDirHandle.mkdir();

		String retStatus;
		//retStatus = runScript(appsign.getFunctionScript(),logDir );
		
		java.util.StringTokenizer st = new java.util.StringTokenizer(appsign.getFunctionScript(),"/");
		st.nextToken();
		String script = st.nextToken();
		String logName = script.replace(' ', '_')+"_"+testcase.getTestcaseId()+"_"+funcSeq;
		 retStatus = runScript(appsign.getFunctionScript(),testrun.getLogsDir(), logName );
		String logFile=this.createLogFile(testrun.getLogsDir(), logName);
		lastLogFile = logFile;
		testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
				   ,logFile, retStatus);

		return retStatus;

    }
    
    


    public String runFunction(FunctionInfo func, int funcSeq) throws Exception {

		String logDir = testrun.getLogsDir()+File.separator+
		          func.getFunctionScript().replace(' ', '_')+"_"+testcase.getTestcaseId()+"_"+funcSeq;

		File logDirHandle = new File(logDir);
		if (logDirHandle.isDirectory()) {
			throw new Exception("Dir already exists");
		} else
			logDirHandle.mkdir();
		java.util.StringTokenizer st = new java.util.StringTokenizer(func.getFunctionScript(),"/");
		st.nextToken();
		String script = st.nextToken();
		String logName = script.replace(' ', '_')+"_"+testcase.getTestcaseId()+"_"+funcSeq;

		String retStatus = runScript(func.getFunctionScript(),testrun.getLogsDir(), logName );

		String logFile=this.createLogFile(testrun.getLogsDir(), logName);
		testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
				   ,logFile, retStatus);

		return retStatus;
	}

	public String runScript(String script, String logDir, String logName)
	  throws Exception {

	    Runtime rt = Runtime.getRuntime();

	    java.util.StringTokenizer st = new java.util.StringTokenizer (script,"/");
	    String workspace = st.nextToken();
	    String runScript = st.nextToken();
	    
	    
		String cmd =
			TCAgentMain.getParserDir()+"\\bin\\runrft.bat "+workspace+" "+runScript+" \""+logDir+"\" "+logName;
			
		System.out.println(cmd);

		Process proc = rt.exec(cmd);
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(),
				"ERR"); // any output?
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(),
				"OUT"); // kick them off
		errorGobbler.start();
		outputGobbler.start(); // any error??? int
		System.out.println("Waiting for process .. ");
		int exitVal = proc.waitFor();

		return "PASS";
		
		
	}

	public String createLogFile(String logDir, String logName) throws Exception {

    	String fileName = TCAgentMain.getTmpDir()+logName+".rjar";
    	String cmd = TCAgentMain.getParserDir()+"\\bin\\makerftjar.bat \""
    	     +logDir+File.separator+logName+"\" "+fileName;

		Runtime rt = Runtime.getRuntime();

		Process proc = rt.exec(cmd);
		StreamGobbler errorGobbler = new
        StreamGobbler(proc.getErrorStream(), "ERR"); // any output?

		 StreamGobbler outputGobbler = new
		       StreamGobbler(proc.getInputStream(), "OUT"); // kick them off
		 errorGobbler.start();
	 	 outputGobbler.start(); // any error??? int
	 	 System.out.println("Waiting for process .. ");
		 int exitVal = proc.waitFor();
		 TestRunner.out.println("Creating qjar .. Done");

		 return fileName;
	}


    public void doCleanup() throws Exception {

    }

}
