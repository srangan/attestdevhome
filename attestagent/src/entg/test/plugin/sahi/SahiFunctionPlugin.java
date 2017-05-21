package entg.test.plugin.sahi;

import entg.test.*;
import entg.test.plugin.FunctionPluginInterface;
import entg.util.*;
import java.io.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;


import net.sf.sahi.test.TestRunner;
import java.util.HashMap;

public class SahiFunctionPlugin implements entg.test.plugin.FunctionPluginInterface {


	TestRunInfo testrun;
	TestcaseInfo testcase;
	FunctionInfo func;
	String status;

    static AppSignInfo lastAppSign = null;
    static String lastLogFile=null;

    public String getType() {
    	return "SAHI";
    }


	boolean isAppSignon=false;
	public SahiFunctionPlugin (TestRunInfo testrun) {
		this.testrun=testrun;
	}


    public void startTestcase(TestcaseInfo testcase) {
    	try {
    	this.testcase =testcase;
    	
    	
    	String base = "";
    	String browserType = "ie";
    	// create test sah script
    	String scriptName = TCAgentMain.TCProperties.getProperty("SAHI_SCRIPTS_LOCATION")+"\\"+
    			testcase.getTestcaseName().replace(' ', '_').replace('\\', '_').replace('/', '_')+ ".sah";
    	PrintWriter wrapperScript = new PrintWriter(new BufferedWriter(new FileWriter(scriptName)));
    	// driver script in testrun.getDriver
    	
    	HashMap<String, Object> variableHashMap = new HashMap<String, Object>();
    	
    	ScenarioInfo s1 = null;
    	Vector scenarios = testrun.getScenarios();
		for (int j = 0; j < scenarios.size(); j++) {
            s1 = (ScenarioInfo) scenarios.elementAt(j);
			if (s1.getScenarioId() == testcase.getScenarioId())
				break;
		}
    	Vector functions = s1.getFunctions();
		for (int k=0;k<functions.size();k++) {
			FunctionInfo f1 = (FunctionInfo) functions.elementAt(k);
			if (f1.isSignon) {
				AppSignInfo a1 = (AppSignInfo) f1;
				Vector dataV = a1.getDataVect();
				
				for (int c=0;c<dataV.size();c++) {
			          
			          String str = (String) dataV.elementAt(c);
					  StringTokenizer st = new StringTokenizer(str, "=");
					  

					  String key = st.nextToken();
					  String val = "";

			  		  try {
							val = str.substring((key.length() + 1));
					  } catch (Exception ex) {
					  }
			  		  if (key.equals("system_url")) base = val;
			  		  
			  		  if (key.equals("password_val")) {
			  			entg.util.PasswordEncryption pwp = new entg.util.PasswordEncryption();
						String pwd = pwp.decrypt(val);
						val = pwd;
			  		  }
			  		
			  		  variableHashMap.put("$"+key, val);
			  		  
				}
			}
			
			
			wrapperScript.println("_include(\""+f1.getFunctionScript()+"\");");
		}
        wrapperScript.close();
        
        Vector dataV = testcase.getDataVect();

		for (int c = 0; c < dataV.size(); c++) {

			String str = (String) dataV.elementAt(c);
			System.out.println("Writing data " + str);

			StringTokenizer st = new StringTokenizer(str, "=");

			String key = st.nextToken();
			String val = "";

			try {
				val = str.substring((key.length() + 1));
				if (val.equals("null"))
					val = "";
			} catch (Exception ex) {
			}
			variableHashMap.put("$"+key, val);
		}
        
		System.out.println("Printing variableHashMap");
        Iterator i = variableHashMap.keySet().iterator();
        while (i.hasNext()) {
        	String k = (String) i.next();
        	System.out.println(k+"="+(String)variableHashMap.get(k));
        }
          
        TestRunner testRunner = 
    			new TestRunner(scriptName, browserType, base,"1");
    		 
        
        
    	testRunner.setInitJS(variableHashMap);
    	status = testRunner.execute();
    	System.out.println(status);
    	
    	
    	
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    	
    }

    
    public String doSignon(AppSignInfo appsign, int funcSeq) throws Exception {

    	// capture the status and log-file.
    	  
    	testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
				   ,"", (status.equals("SUCCESS"))?"PASS":"FAIL");
		return (status.equals("SUCCESS"))?"PASS":"FAIL";

    }
    
    public String runFunction(FunctionInfo func, int funcSeq) throws Exception {
    	testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
				   ,"", (status.equals("SUCCESS"))?"PASS":"FAIL");
    	return (status.equals("SUCCESS"))?"PASS":"FAIL";
    }

    public void doCleanup() throws Exception {

    }

}
