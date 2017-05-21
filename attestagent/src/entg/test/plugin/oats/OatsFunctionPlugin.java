package entg.test.plugin.oats;
import entg.job.JobManager;


import entg.test.plugin.oats.util.JavaAgentWrapper;
import entg.test.plugin.oats.util.RunParameters;
import entg.test.plugin.oats.util.ScriptResult;

import entg.test.*;
import entg.test.plugin.FunctionPluginInterface;
import entg.util.*;
import entg.util.StreamGobbler;

import java.io.*;
import java.sql.*;
import java.util.*;


public class OatsFunctionPlugin implements entg.test.plugin.FunctionPluginInterface {
  
	

    private static final long JAGENT_TIMEOUT = 900000L;
    
	TestRunInfo testrun;
	TestcaseInfo testcase;
	FunctionInfo func;
	int funcSeq;
	

    private String m_jwgFile;
    private JavaAgentWrapper m_qos;
    private ScriptResult m_result;
    private RunParameters m_param;	
    
    public String getType() {
    	return "OATS";
    }

	
	boolean isAppSignon=false;
	public OatsFunctionPlugin (TestRunInfo testrun) {
		this.testrun=testrun;
	}


    public void startTestcase(TestcaseInfo testcase) {
    	this.testcase =testcase; 
    	
    	m_qos = new JavaAgentWrapper( TCAgentMain.TCProperties.getProperty("OATS_INSTALL_DIR")+"\\openScript\\runScript.bat" );

    	
    }
    
  public String doSignon(AppSignInfo appsign, int funcSeq) throws Exception {
  	
	
	  String oatsScript = TCAgentMain.TCProperties.getProperty("OATS_REPOS_DIR")+
			    "\\"+appsign.getFunctionScript()+"\\"+appsign.getFunctionScript()+".jwg";

	  String runSignonParam = TCAgentMain.TCProperties.getProperty("OATS_RUN_SIGNON")+"";
	  boolean runSignon = (runSignonParam.equalsIgnoreCase("Yes"));
	  String retStatus = "";
	  
	  if (new File(oatsScript).exists() && runSignon) {
		  
		  System.out.println("running App signon script(exists)"+retStatus);
		  String logDir = testrun.getLogsDir()+File.separator+
				  appsign.getFunctionScript().replace(' ', '_')+"_"+testcase.getTestcaseId()+"_"+this.funcSeq;
				

			new File(logDir).mkdirs();
			String logF = logDir +File.separator+ "scriptLog.txt";
		    	 
			System.out.println("oatsScript = "+oatsScript);
			
			java.util.Hashtable hash = appsign.getSignonDataMap();			
			generateData(hash,appsign.getFunctionScript()+".csv");
			
			setOatsScript(oatsScript,logDir);
			
			System.out.println("m_param.getScriptJWGFile() = "+m_param.getScriptJWGFile());
			System.out.println("m_param.getResultFolder() = "+m_param.getResultFolder());
			System.out.println("m_param.getAdditionalArgs() = "+m_param.getAdditionalArgs());
			System.out.println("logF = "+logF);
			
			entg.test.plugin.oats.util.Logger.createLogger(logF);		
	        m_result= m_qos.run(m_param, JAGENT_TIMEOUT);
	        entg.test.plugin.oats.util.Logger.closeLogger();
	        
	        System.out.println("After OatS run");
	        String logFile = "";
	        if (m_result==null ) {
	        	retStatus = "FAIL";
	        	logFile=logF;
	        } else {
	        	
	        	System.out.println("Result  = #"+m_result.getOverallResult()+"#");
	        	if (m_result.getOverallResult().toString().equals("Passed")) 
	        		retStatus = "PASS";        	
	            else
	            	retStatus = "FAIL";
	        	
	        	System.out.println("csvReportFile = "+m_result.getCsvReportFile());
	        	
	        }
	        entg.test.plugin.oats.util.Logger.closeLogger();
	        System.out.println("retStatus "+retStatus);
	        logFile = this.createLogFile(logDir,funcSeq);
	        System.out.println("retStatus "+retStatus);
	        testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
					   ,logFile, retStatus);
			
	  	  
		  
	  } else {
 
	  String logFile = testrun.getLogsDir()+File.separator+
				appsign.getFunctionScript().replace(' ', '_')+"_"+testcase.getTestcaseId()+"_"+this.funcSeq+".txt";
			
	  PrintWriter log = new PrintWriter(new BufferedWriter(new FileWriter(new File(logFile))));
	  
	  try {
		
		
		
		log.println("Generating datafile for Login Script - "+appsign.getFunctionScript());
		
		java.util.Hashtable hash = appsign.getSignonDataMap();		
		generateData(hash,appsign.getFunctionScript()+".csv");
		log.println("DONE - success.");
		
		retStatus = "PASS"; 
	  } catch (Exception ex) {
		ex.printStackTrace(log);
		retStatus = "FAIL";  
	  }
	  log.flush();
	  log.close();
	  
		testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
				   ,logFile, retStatus);
		
	  }
		return retStatus;		
		
  }
  
   

    public String runFunction(FunctionInfo func, int funcSeq) throws Exception {
    	
		String logDir = testrun.getLogsDir()+File.separator+
				func.getFunctionScript().replace(' ', '_')+"_"+testcase.getTestcaseId()+"_"+this.funcSeq;
			

		new File(logDir).mkdirs();
		String logF = logDir +File.separator+ "scriptLog.txt";
	    	 
		
		String retStatus="";
		
		String oatsScript = TCAgentMain.TCProperties.getProperty("OATS_REPOS_DIR")+
				    "\\"+func.getFunctionScript()+"\\"+func.getFunctionScript()+".jwg";
		
		System.out.println("oatsScript = "+oatsScript);
		
		java.util.Hashtable hash = testcase.getDataMap();		
		generateData(hash,func.getFunctionScript()+".csv");
		
		setOatsScript(oatsScript,logDir);
		
		System.out.println("m_param.getScriptJWGFile() = "+m_param.getScriptJWGFile());
		System.out.println("m_param.getResultFolder() = "+m_param.getResultFolder());
		System.out.println("m_param.getAdditionalArgs() = "+m_param.getAdditionalArgs());
		System.out.println("logF = "+logF);
		
		entg.test.plugin.oats.util.Logger.createLogger(logF);		
        m_result= m_qos.run(m_param, JAGENT_TIMEOUT);
        entg.test.plugin.oats.util.Logger.closeLogger();
        
        System.out.println("After OatS run");
        String logFile = "";
        if (m_result==null ) {
        	retStatus = "FAIL";
        	logFile=logF;
        } else {
        	
        	System.out.println("Result  = #"+m_result.getOverallResult()+"#");
        	if (m_result.getOverallResult().toString().equals("Passed")) 
        		retStatus = "PASS";        	
            else
            	retStatus = "FAIL";
        	
        	System.out.println("csvReportFile = "+m_result.getCsvReportFile());
        	
        }
        entg.test.plugin.oats.util.Logger.closeLogger();
        System.out.println("retStatus "+retStatus);
        logFile = this.createLogFile(logDir,funcSeq);
        System.out.println("retStatus "+retStatus);
        testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
				   ,logFile, retStatus);
		
		System.out.println("retStatus "+retStatus);
		return retStatus;		
    }
    
	public String createLogFile(String dir,int funcSeq) throws Exception {


		  PrintWriter log = new PrintWriter(new BufferedWriter(new FileWriter(new File(dir+File.separator+"Viewer.html"))));
		  if ((new File(dir+File.separator+"Session1"+File.separator+"BasicReport.htm")).exists()) {
			  log.println("<html><body>  <table>  <tr><td><a href=\"Session1/BasicReport.htm\" target=\"content\">View Script Report</a>|<a href=\"scriptLog.txt\" target=\"content\">View Log</a> </td></tr>  <tr><td ><iframe name=\"content\"  src=\"Session1/BasicReport.htm\" style=\"overflow:hidden;overflow-x:hidden;overflow-y:hidden;height:100%;width:100%;position:absolute;top:40px;left:0px;right:0px;bottom:0px\">   </td></tr></table></body></html>");  
		  } else {
			  log.println("<html><body>  <table>  <tr><td ><iframe name=\"content\"  src=\"scriptLog.txt\" style=\"overflow:hidden;overflow-x:hidden;overflow-y:hidden;height:100%;width:100%;position:absolute;top:40px;left:0px;right:0px;bottom:0px\">   </td></tr></table></body></html>");
		  }
		  

		  log.close();


		
		
    	String fileName = TCAgentMain.getTmpDir()+"\\report_"+testcase.getTestcaseName()
    	  +"_"+funcSeq+".ojar";
    	String cmd = TCAgentMain.getParserDir()+"\\bin\\makeojar.bat \""
    	     +dir+"\" "+fileName;

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
		 TestRunner.out.println("Creating ojar .. Done");

		 return fileName;
	}



    public void generateData(java.util.Hashtable hash,String fileName) throws Exception {
		String dataFile = TCAgentMain.TCProperties.getProperty("OATS_REPOS_DIR")+"\\data\\"+fileName;

    	PrintWriter dataFileWriter = new PrintWriter(new BufferedWriter(new FileWriter(dataFile)));
    	
    	java.util.Enumeration<String> keys = hash.keys();
    	
    	String params="";
    	String values="";
    	while (keys.hasMoreElements()) {
    		String key = keys.nextElement();
    		String val = (String) hash.get(key);
    		if (key.endsWith("password_val")) {
    			entg.util.PasswordEncryption pe = new entg.util.PasswordEncryption();
    			String decVal = pe.decrypt(val);
    			val = decVal;
    		}
    		if (params == null || params.equals("")) {
    			params = key;
    			values = val;
    		} else { 
    			params+=","+key;
    			values+=","+val;
    		}
    	}

    	dataFileWriter.println(params);
    	dataFileWriter.println(values);
    	dataFileWriter.flush();
    	dataFileWriter.close();
    	
    }
    
    public void setOatsScript(String scriptName, String resultsFolder) {
    	
    	m_param = new RunParameters();
        m_param.setScriptJWGFile(scriptName);
        
        //m_param.setAdditionalArgs("-browser.type "+TCAgentMain.TCProperties.getProperty("OATS_BROWSER"));
        m_param.setAdditionalArgs(TCAgentMain.TCProperties.getProperty("OATS_COMMANDLINE_ARGS"));
        m_param.setResultFolder(resultsFolder);
        
      }
    
	  

    public void doCleanup() throws Exception {

    }
	   
    
    
    
    /*  
    public String doSignon(AppSignInfo appsign, int funcSeq) throws Exception {
    	
		String logDir = testrun.getLogsDir()+File.separator+
		          appsign.getFunctionScript().replace(' ', '_')+"_"+testcase.getTestcaseId()+"_"+this.funcSeq;
				
		String retStatus="";
		
		String oatsScript = TCAgentMain.TCProperties.getProperty("OATS_REPOS_DIR")+
				    "\\"+appsign.getFunctionScript()+"\\"+appsign.getFunctionScript()+".jwg";
		java.util.Hashtable hash = appsign.getSignonDataMap();		
		generateData(hash);
		
		setOatsScript(oatsScript,logDir);

        m_result= m_qos.run(m_param, JAGENT_TIMEOUT);
        
        String logFile = "";
        if (m_result==null) {
        	retStatus = "FAIL";
        } else {
        	if (m_result.getOverallResult().equals("Passed")) 
        		retStatus = "PASS";        	
            else
            	retStatus = "FAIL";
        	
        	logFile = this.createLogFile(m_result.getCsvReportFile().getParent(),funcSeq);
        }
        
		testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
				   ,createLogFile(logDir,this.funcSeq), retStatus);
		
		return retStatus;		
		
    }
    */
    
}
