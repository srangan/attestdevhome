package entg.test.plugin.selenium;
import entg.job.JobManager;
import entg.test.*;
import entg.test.plugin.FunctionPluginInterface;
import entg.util.*;
import java.io.*;
import java.sql.*;
import java.util.*;

import com.thoughtworks.selenium.*;

public class SeleniumFunctionPlugin implements entg.test.plugin.FunctionPluginInterface {
  
	
	TestRunInfo testrun;
	TestcaseInfo testcase;
	FunctionInfo func;
	int funcSeq;
	
    private Selenium browser;
	
    
    public String getType() {
    	return "SELENIUM";
    }

	
	boolean isAppSignon=false;
	public SeleniumFunctionPlugin (TestRunInfo testrun) {
		this.testrun=testrun;
	}


    public void startTestcase(TestcaseInfo testcase) {
    	this.testcase =testcase; 
    	
    	
    	
    }
        
    public String doSignon(AppSignInfo appsign, int funcSeq) throws Exception {
		String logFile = testrun.getLogsDir()+File.separator+
		          appsign.getFunctionScript().replace(' ', '_')+"_"+testcase.getTestcaseId()+"_"+this.funcSeq+".html";
		PrintWriter log = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));	
		log.println("<HTML><BODY><PRE>");
		String retStatus="PASS";

		
		java.util.Hashtable hash = appsign.getSignonDataMap();
    	System.out.println("Adding testcase_id");

		hash.put("TESTCASE_ID", testcase.getTestcaseId());
		hash.put("RUN_ID", this.testrun.getRunId()+"");
		


		try {
		System.out.println("In signon script");
		if (browser == null) {
  		  System.out.print("Starting Browser .. ");		
  		  String sysUrl = (String) hash.get("system_url");
  		
  		  
  		  String selBrowserPath = TCAgentMain.TCProperties.getProperty("SELENIUM_BROWSER_PATH");
  		  if (selBrowserPath == null || selBrowserPath.equals(""))
  			selBrowserPath = "*iexplore";
		  browser = new DefaultSelenium("localhost",
                4444, selBrowserPath, sysUrl);
          browser.start();
          System.out.print("OK");
		}
        
		
        retStatus = runSeleniumScript(appsign.getFunctionScript(),hash,log);
		
        log.println("</PRE></BODY></HTML>");
		
		log.flush();
		log.close();
		} catch (Exception ex) {
			log.println("Error Occured.");
	    	ex.printStackTrace(log);
    	    log.println();
    	    log.flush();
    		log.close();
			retStatus = "FAIL";
		}
		testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
				   ,logFile, retStatus);
		
		return retStatus;		
		
    }
    
    public static EntgSeleniumTest selTest;
    
    public static java.util.Hashtable selTestMap = new Hashtable();

    
    public String runSeleniumScript(String selTestClass,Hashtable hash,PrintWriter log) {
    	
    	
    System.out.println("In Run Script");
      try {
    	  log.println("Running "+selTestClass);
    	  
    	  if (!selTestMap.containsKey(selTestClass)) {
    	    Class.forName(selTestClass);
    	    selTestMap.put(selTestClass, selTest);
    	  } else {
    		  selTest= (EntgSeleniumTest)selTestMap.get(selTestClass);
    	  }
    	  
    	  Hashtable retHash = selTest.run(browser, hash, log);
    	  if (retHash != null) {
    	
    		    Enumeration h = retHash.keys();
    	    	while (h.hasMoreElements()) {
    	    		String s = (String)h.nextElement();
    	    		System.out.println("RETURN - "+s+":"+retHash.get(s));
    	    		testcase.addData(s+"="+retHash.get(s));
    	    	}
    	    	
    	  }
    	  log.println("OK");
    	  return "PASS";
      } catch (Exception ex) {
    	  
    	  log.println("Error Occured.");
    	  ex.printStackTrace(log);
    	  log.println();
    	  return "FAIL";  
      }
   	  
    }
	  

    public String runFunction(FunctionInfo func, int funcSeq) throws Exception {
    	
    	String logFile = testrun.getLogsDir()+File.separator+ 
             func.getFunctionScript().replace(' ', '_')+"_"+testcase.getTestcaseId()+"_"+this.funcSeq+".html";

    	PrintWriter log = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
		log.println("<HTML><BODY><PRE>");
    	
    	
    	log.println("In test script: "+func.getFunctionScript());
    	
    	
    	System.out.println("Before : "+func.getFunctionName());
    	Hashtable inp = testcase.getDataMap();
    	System.out.println("Adding testcase_id");
    	inp.put("TESTCASE_ID", testcase.getTestcaseId());
		inp.put("RUN_ID", this.testrun.getRunId()+"");
		

    	
    	String retStatus=runSeleniumScript(func.getFunctionScript(),inp,log);

        log.println("</PRE></BODY></HTML>");
    	
		log.flush();
		log.close();
		
		testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
				   ,logFile, retStatus);
		
		return retStatus;		
 
    }

    public void doCleanup() throws Exception {
	Thread.sleep(20000);
    	if (browser != null) {
			browser.stop();
		} 
    }
	   
}
