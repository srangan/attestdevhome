package entg.test.plugin.qtp;
import entg.test.*;
import entg.test.plugin.FunctionPluginInterface;
import entg.util.*;
import java.io.*;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;


public class QtpFunctionPlugin implements entg.test.plugin.FunctionPluginInterface {


	TestRunInfo testrun;
	TestcaseInfo testcase;
	FunctionInfo func;
	QtpAppsignon qas;

    QtpTestcase tc;

    static AppSignInfo lastAppSign = null;
    static String lastLogFile=null;

    public String getType() {
    	return "QTP";
    }


	boolean isAppSignon=false;
	public QtpFunctionPlugin (TestRunInfo testrun) {
		this.testrun=testrun;
	}


    public void startTestcase(TestcaseInfo testcase) {
    	this.testcase =testcase;
    	System.out.println("*** IN start testcase");
    	System.out.println("testcase.getOverrideDataFile "+testcase.getOverrideDataFile());
    	System.out.println("testcase.getLastpassfunc "+testcase.getLastpassfunc());
    	tc = new QtpTestcase (testcase);
    	
    	if (testcase.getOverrideDataFile()!=null) {
            System.out.println("LogDir: "+testrun.getLogsDir());
        	
        	System.out.println("testcase.getTestcaseId(): "+testcase.getTestcaseId());
    		
    		String logDir = testrun.getLogsDir()+File.separator+"LASTPASS_"
	          +testcase.getTestcaseId()+"_0";
    		
    		tc.overrideReadAndSetTestcaseXls(logDir);
    	} 
    	if (testcase.getLastpassfunc()>0 && testcase.getOverrideDataFile()==null) {
    		
        	System.out.println("LogDir: "+testrun.getLogsDir());
        	
        	System.out.println("testcase.getTestcaseId(): "+testcase.getTestcaseId());
    		
    		String logDir = testrun.getLogsDir()+File.separator+"LASTPASS_"+
	          testcase.getTestcaseId()+"_0";

    		tc.continueReadAndSetTestcaseXls(logDir);
    	}
    }


	public static Document loadDocument(String logDir) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		// Parse the XML file and build the Document object in RAM
		Document doc = docBuilder.parse(new File(logDir + File.separator
				+ "Report" + File.separator + "results.xml"));

		return doc;
	}

	public static String evalXpathExpr(Document doc, String expr)
			throws Exception {
		String str = XPathAPI.eval(doc, expr).toString();
		return str;
	}
    
    public String doSignon(AppSignInfo appsign, int funcSeq) throws Exception {

    	if (lastAppSign!= null && lastAppSign==appsign) {
    		tc.setCurrentSignon (appsign);
    		System.out.println("***** Skipping signon ");
    		testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
 				   ,lastLogFile, "PASS");

    		return "PASS";


    	}

    	lastAppSign=appsign;

    	qas = new QtpAppsignon(appsign);
		qas.generateXls(testrun.getTestcasesDir(),funcSeq);


    	String logDir = testrun.getLogsDir()+File.separator+
           appsign.getFunctionScript().replace(' ', '_').replace('\\', '_').replace('/', '_')+"_"+testcase.getTestcaseId()+"_"+funcSeq;

    	
		File logDirHandle = new File(logDir);
		if (logDirHandle.isDirectory()) {
			throw new Exception("Dir already exists");
		} else
			logDirHandle.mkdir();

		String retStatus;
		
		// Write passwords
		java.util.Hashtable signonPar = qas.appsign.getSignonDataMap();
		String passwordVal = (String) signonPar.get("password_val");
		String dbPasswordVal = (String) signonPar.get("database_password_val");
		/*
		System.out.println("PW "+passwordVal);
		System.out.println("DBPW "+dbPasswordVal);*/
		entg.util.PasswordEncryption pe = new entg.util.PasswordEncryption();
		
		
		String pw = null;
		
		if (passwordVal!=null) pw=pe.decrypt(passwordVal);
		String dbPw = null;
		if (dbPasswordVal!=null) dbPw=pe.decrypt(dbPasswordVal);
		/*
		System.out.println("PWD "+pw);
		System.out.println("DBPWD "+dbPw);
		*/
		java.io.PrintWriter pout = new PrintWriter(new FileWriter(TCAgentMain.getTmpDir()+File.separator+"p.pp", false));
		pout.println(pw);
		pout.println(dbPw);
		pout.flush();
		pout.close();
		String[] envp={"APP_PASSWORD="+pw,"DB_PASSWORD="+dbPw};
		try {
		retStatus = runScript(appsign.getFunctionScript(),
					 qas.getXlsFilename(),logDir,envp );
		} catch (Exception e) {
		    File f = new File(TCAgentMain.getTmpDir()+File.separator+"p.pp");
			f.delete();
			throw e;
		}
		File f = new File(TCAgentMain.getTmpDir()+File.separator+"p.pp");
		f.delete();
		
		String logFile=this.createLogFile(logDir,funcSeq);
		lastLogFile = logFile;
		

		String stime="",etime="";
		try {
		Document doc = loadDocument(logDir);
			
		stime = evalXpathExpr(doc, "//@sTime");
		etime = evalXpathExpr(doc, "//@eTime");
		//testrun.setFunctionPerfMetrics(testcase.getTestcaseName(), ""+funcSeq,
		//		      stime, etime);
		} catch (Exception ex) {
			System.out.println("*** Error Stamping perf-metrics");
		}
		testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
				   ,logFile, retStatus, stime,etime);
		tc.setCurrentSignon (appsign);

		return retStatus;

    }
    
    


    public String runFunction(FunctionInfo func, int funcSeq) throws Exception {

    	//if (tc.getXlsFilename()==null)
	      tc.generateXls(testrun.getTestcasesDir(),func,funcSeq);


		String logDir = testrun.getLogsDir()+File.separator+
		          func.getFunctionScript().replace(' ', '_').replace('\\', '_').replace('/', '_')+"_"+testcase.getTestcaseId()+"_"+funcSeq;

		File logDirHandle = new File(logDir);
		if (logDirHandle.isDirectory()) {
			throw new Exception("Dir already exists");
		} else
			logDirHandle.mkdir();

		String retStatus = runScript(func.getFunctionScript(),
					 tc.getXlsFilename(),logDir,null );

		System.out.println("Here -- after runScript");
		tc.readXlsAndSetData(logDir);


		String logFile=this.createLogFile(logDir,funcSeq);
		String stime="",etime="";
		try {
		Document doc = loadDocument(logDir);
			
		stime = evalXpathExpr(doc, "//@sTime");
		etime = evalXpathExpr(doc, "//@eTime");
		//testrun.setFunctionPerfMetrics(testcase.getTestcaseName(), ""+funcSeq,
		//		      stime, etime);
		} catch (Exception ex) {
			System.out.println("*** Error Stamping perf-metrics");
		}
		testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
				   ,logFile, retStatus, stime,etime);
		
		return retStatus;
	}

	public String runScript(String script, String dataSheet, String logDir, String[] envp)
	  throws Exception {

	    Runtime rt = Runtime.getRuntime();

	    
	    String tempTestcaseFile = testrun.getLogsDir()+java.io.File.separator+
	       "temp"+System.currentTimeMillis()+".txt";
	    System.out.println("tempTestcaseFile "+tempTestcaseFile);
		String cmd = 	"cscript.exe " + TCAgentMain.getParserDir()+"\\drivers\\"
				+ "qtpVBDriverNew.wsf \"" +
				TCAgentMain.TCAgentproperties("TEST_SCRIPT_DIRECTORY")
				+File.separator+script + "\" \""
				+ testrun.getTestcasesDir()+File.separator+dataSheet + "\" \"" + logDir + "\"  "
				+ tempTestcaseFile;

		System.out.println(cmd);

		
		Process proc = rt.exec(cmd);
		//Process proc = rt.exec(cmd);
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(),
				"ERR"); // any output?
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(),
				"OUT"); // kick them off
		errorGobbler.start();
		outputGobbler.start(); // any error??? int
		System.out.println("Waiting for process .. ");
		int exitVal = proc.waitFor();

		String retStatus = "";
		try {
  		  FileInputStream fin = new FileInputStream(tempTestcaseFile);
		  Properties retProp = new Properties();
		  retProp.load(fin);
		  retStatus = retProp.getProperty("STATUS");
		} catch (Exception ex) {
			retStatus = "FAIL";
			System.out.println("ERROR: QTP did not create status file");
			// ex.printStackTrace();
		}
		return retStatus;
	}

	public String createLogFile(String dir,int funcSeq) throws Exception {

    	String fileName = TCAgentMain.getTmpDir()+"\\report_"+testcase.getTestcaseName()
    	  +"_"+funcSeq+".qjar";
    	String cmd = TCAgentMain.getParserDir()+"\\bin\\makeqjar.bat \""
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
		 TestRunner.out.println("Creating qjar .. Done");

		 return fileName;
	}


    public void doCleanup() throws Exception {

    }

}
