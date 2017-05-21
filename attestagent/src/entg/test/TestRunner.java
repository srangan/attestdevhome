package entg.test;

import entg.test.plugin.*;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;
import java.io.*;
import java.util.*;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.model.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;
//import entg.util.*;

import entg.job.JobManager;

public class TestRunner extends Thread {
public static PrintWriter out;
public static PrintStream savedOut;
TCAgentMain tc;
static String logFileName=null;

    public TestRunner (TestRunInfo testrun, TCAgentMain tc) {
    	this.testrun=testrun;
    	this.tc=tc;
 	
    }
    public static void createLogFile() {
    	try {
    	  logFileName=TCAgentMain.getTmpDir()+ "\\log"+System.currentTimeMillis()+".txt";
    	  
      	  out=new PrintWriter(new BufferedWriter(new FileWriter(logFileName)));
      	  savedOut = System.out;
          PrintStream out1 = new PrintStream(new FileOutputStream(TCAgentMain.getTmpDir()+ "\\"+TCAgentMain.runIdStr+".debug"));
          System.setOut(out1);
          
      	} catch (IOException e) {
      		e.printStackTrace();
      	}       	
    }

    public void run()
    {
    	try {
    		runTest();    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    }
    
	private  TestRunInfo testrun;	

	public java.util.Hashtable plugins = new Hashtable();
	
	String lastPlugin=null;
	
	public FunctionPluginInterface getPlugin (TestRunInfo testrun, TestcaseInfo tst1, String funcType) throws Exception {
		FunctionPluginInterface retPlugin;
		if (plugins.containsKey(funcType)) {
			retPlugin= (FunctionPluginInterface) plugins.get(funcType);
		} else {
			retPlugin = FunctionPluginManager.initPlugin(testrun, funcType);
			
			plugins.put(funcType, retPlugin);
			
		}
		if (lastPlugin==null) {
			retPlugin.startTestcase(tst1);
			
		} else if (!lastPlugin.equalsIgnoreCase(funcType)) {
  	       ((FunctionPluginInterface)plugins.get(lastPlugin)).doCleanup();
			plugins.remove(lastPlugin);
			retPlugin.startTestcase(tst1);
			
		}
		lastPlugin = funcType;	
		
		return retPlugin;
			
	}
	
	
	
	public void runTest() throws Exception {

		FunctionPluginInterface fint = null;
		if (TCAgentMain.dbMethod()) TCAgentMain.makeMetricDBConn();
		testrun.generateGlobalSettingFile();
		
		
		
        Vector scenarios = testrun.getScenarios();
		for (int j = 0; j < scenarios.size(); j++) {
            ScenarioInfo s1 = (ScenarioInfo) scenarios.elementAt(j);
			
			Vector testcases = s1.getTestCases();
			


			for (int i = 0; i < testcases.size(); i++) {
				 TestcaseInfo tst1 = (TestcaseInfo) testcases.elementAt(i);
				 
				 
				 
				 Vector functions = s1.getFunctions();
				 for (int k=0;k<functions.size();k++) {
					 
				   FunctionInfo f1 = (FunctionInfo) functions.elementAt(k);
				   System.out.println("Function: "+f1.getFunctionName());
				   if (tst1.getFunctionStatus(k).equals("PASS")) continue;
				   
				   
				   String retStatus;
				   if (f1.isSignon) {
					  System.out.println("Starting Singnon");
					  AppSignInfo a1 = (AppSignInfo) f1;
					   
					  FunctionInfo f2 = (FunctionInfo) functions.elementAt(k+1);
					  
					  fint = this.getPlugin(testrun, tst1, f2.getFunctionType());
					  retStatus =  fint.doSignon(a1, (k+1));
					  System.out.println("Signon retStatus = "+retStatus);
				   } else {
				 
					   System.out.println("Starting function");
 					   fint = this.getPlugin(testrun, tst1, f1.getFunctionType());
 					   
					   retStatus = fint.runFunction(f1,(k+1));
					   System.out.println("Function retStatus = "+retStatus);
				   }
				   while (tc.paused){ 
			         Thread.sleep(1000);
				   }
				   if (retStatus.equalsIgnoreCase("FAIL")) 
					 break; 
				   if (tc.aborted)
						break;
				}
				fint.doCleanup(); 
				plugins.remove(lastPlugin);  
				lastPlugin=null;
				if (tc.aborted)
				  break;
				while (tc.paused){ 
				  Thread.sleep(1000);
				}
			}		
		}
		System.out.println("Test Complete");
		
		if (TCAgentMain.dbMethod())
  		  TCAgentMain.closeMetricDBConn();
	}
	
	

}
