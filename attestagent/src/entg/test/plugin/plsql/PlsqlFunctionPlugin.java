package entg.test.plugin.plsql;
import entg.job.JobManager;
import entg.test.*;
import entg.test.plugin.FunctionPluginInterface;
import entg.util.*;
import java.io.*;
import java.sql.*;
import java.util.*;


public class PlsqlFunctionPlugin implements entg.test.plugin.FunctionPluginInterface {
  
	entg.util.DbmsOutput dbmsOut;
	
	TestRunInfo testrun;
	TestcaseInfo testcase;
	FunctionInfo func;
	int funcSeq;
	
	static String appUserName = ""; 
	
    
    public String getType() {
    	return "PLSQL";
    }

	
	boolean isAppSignon=false;
	public PlsqlFunctionPlugin (TestRunInfo testrun) {
		this.testrun=testrun;
	}


    public void startTestcase(TestcaseInfo testcase) {
    	this.testcase =testcase; 
    	
    }
    
   
    
    Connection dbConn=null;
        
    public String doSignon(AppSignInfo appsign, int funcSeq) throws Exception {

    	System.out.println("In dbSingon");
		String logFile = testrun.getLogsDir()+File.separator+
		          appsign.getFunctionScript().replace(' ', '_')+"_"+testcase.getTestcaseId()+"_"+this.funcSeq+".txt";
		PrintWriter log = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));		
		String retStatus="PASS";

		try {
			

			log.println("Making DB Connection");;
			java.util.Hashtable signonData = appsign.getSignonDataMap();
			String dbUser = (String) signonData.get("database_username");
			String dbPwd = (String) signonData.get("database_password_val");
			String dbMc = (String) signonData.get("database_machine");
			String dbPort = (String) signonData.get("database_port");
			String dbSid = (String) signonData.get("database_SID");
			String dbUrl = (String) signonData.get("database_url");
			
			if (dbUrl == null) 
			  dbUrl = "jdbc:oracle:thin:@"+dbMc+":"+dbPort+":"+dbSid;
			System.out.println("dbUser:"+dbUser);
			System.out.println("dbPwd:"+dbPwd+"#");
			System.out.println("dbUrl:"+dbUrl+"#");
			
			String dbPwd1 = (String) signonData.get("database_password");
			if (dbPwd != null && !dbPwd.equals("")) {
			   
			  entg.util.PasswordEncryption pwp = new entg.util.PasswordEncryption();
			  dbPwd1 = pwp.decrypt(dbPwd);
		    }
			
			
			
			//String dbPwd1 = entg.util.PasswordEncryption. 
			System.out.println("dbPwd1:"+dbPwd1);
			
			
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			dbConn = DriverManager.getConnection(dbUrl, dbUser,dbPwd1);
			

			System.out.println("DB Connection .. Done");
			log.println("DB Connection .. Done");
			try {
			dbmsOut = new entg.util.DbmsOutput(dbConn);
			dbmsOut.enable(100000);
			System.out.println("Enabled dbms_output");
			} catch (Exception e) { 
			  System.out.println("Error enabling dbms_output - "+e.getMessage());				
			}
			
			log.println("Initializing AppContext");
			/*
			String appsUN = (String) signonData.get("username");
			String role = (String) signonData.get("role");
			if (appsUN!=null && role!=null && !appsUN.equals("") && !role.equals(""))
   			  setAppContext(appsUN,role);
			*/

			String sql = "BEGIN "+appsign.getAppSignOnScript();
	    	
	    	Vector params = appsign.getFunctionParams();
	    	
	    	log.println(params.size()+" parameters");
	    	if (params.size()==0) 
	    		sql+="(p_username => ?,p_testcase_id => ?, x_retstat => ?, x_retmsg =>?); END;";
	    	else {
	    		sql+="(";
	    		for (int p=0;p<params.size();p++) {
	    			FunctionParamInfo funcPar = (FunctionParamInfo) params.elementAt(p);
	    			String parName = funcPar.getParamName();
	    			if (p==0) sql+= parName +"=> ?"; else sql+=", "+parName +" => ?";
	    		}
	    		sql+=",p_username => ?,p_testcase_id => ?, x_retstat => ?, x_retmsg =>?); END;";
	    		
	    	}
	    	
	    	log.println("SQL Statement: \n"+sql);
	    	CallableStatement cstmt = dbConn.prepareCall(sql);
	    	
	    	log.println("Setting IN Parameters");    	
	    	for (int p=0;p<params.size();p++) {
	    		FunctionParamInfo funcPar = (FunctionParamInfo) params.elementAt(p);

	    		if (funcPar.getParamType().equals("IN") || funcPar.getParamType().equals("INOUT")) {
	    		  log.println("  Index: "+(p+1));
	      		  log.println("  Name: "+funcPar.getParamName());
	    		  log.println("  Type: "+funcPar.getParamType());    			
	    		  String parName = funcPar.getParamName();

	    		  if (signonData.containsKey(parName)) {
	    			  String val = (String)signonData.get(parName);
	    			  log.println("  Val: "+val);
	    			  if (!val.equals("") && !val.equals("null")) 
	    			    cstmt.setString((p+1), val);
	    			  else 
	    				cstmt.setNull((p+1), java.sql.Types.VARCHAR);
	    		  }
	    			 
	    		  else
	    			 cstmt.setNull((p+1), java.sql.Types.VARCHAR);
	    		}
	    		if (funcPar.getParamType().equals("OUT") || funcPar.getParamType().equals("INOUT")) {
	    			cstmt.registerOutParameter((p+1), java.sql.Types.VARCHAR);
	    		}
	    	}
	    	
	    	
	    	int numParams = params.size();
	    	cstmt.setString(numParams+1,(String) signonData.get("username"));
	    	cstmt.setString(numParams+2,testcase.getTestcaseId());
	    	cstmt.registerOutParameter(numParams+3, java.sql.Types.VARCHAR);
	    	cstmt.registerOutParameter(numParams+4, java.sql.Types.VARCHAR);
	    	System.out.println("Before Exec");
			cstmt.execute();
	    	System.out.println("After Exec");
			log.println("SQL Call complete");
			
			appUserName = (String) signonData.get("username");
			String retStat = cstmt.getString(numParams+3);
			String retMsg = cstmt.getString(numParams+4);
			
			cstmt.close();		
			
			try {
			System.out.println("Dumping dbms_output");
			log.println("DBMS_OUTPUT :");
			log.println("------------ ");
			dbmsOut.show(log);
			dbmsOut.close();
			} catch (Exception ex) {
				System.out.println("Error dumping dbms_output "+ex.getMessage());
			}
			if (!retStat.equals("S")) {
				log.println("Function returned with error code: "+retStat);
				log.println("Following error message was returned:\n"+retMsg);
				retStatus = "FAIL";
			} else {
				log.println("Signon script complete. No errors.");
			}
			
		} catch (Exception e) {

			System.out.println("Caught Excep");
			
			log.println("Caught Exception.\nError: "+e.getMessage());
			e.printStackTrace();
			e.printStackTrace(log);
			retStatus = "FAIL";
			
		}
		log.flush();
		log.close();
		
		testrun.setFunctionStatus(testcase, testcase.getTestcaseName(), ""+funcSeq
				   ,logFile, retStatus);
		
		return retStatus;		
		
    }
	  

    public String runFunction(FunctionInfo func, int funcSeq) throws Exception {
    	
    	String logFile = testrun.getLogsDir()+File.separator+ 
             func.getFunctionScript().replace(' ', '_')+"_"+testcase.getTestcaseId()+"_"+this.funcSeq+".txt";

    	try {
			dbmsOut = new entg.util.DbmsOutput(dbConn);
			dbmsOut.enable(100000);
			System.out.println("Enabled dbms_output");
			} catch (Exception e) { 
			  System.out.println("Error enabling dbms_output - "+e.getMessage());				
			}
			
 	
    	PrintWriter log = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));		
    	String retStatus="PASS";
    	
    	try {
    	
    		log.println("Entering PLSQL Function: " +func.getFunctionScript());
    	String sql = "BEGIN "+func.getFunctionScript();
    	
    	Vector params = func.getFunctionParams();
    	
    	/*
    	 * p_username IN varchar2,
                            p_testcase_id IN NUMBER,
                            x_retstat OUT VARCHAR2,
                            x_retmsg OUT VARCHAR2
    	 */
    	log.println(params.size()+" parameters");
    	if (params.size()==0) 
    		sql+="(p_username => ?,p_testcase_id => ?, x_retstat => ?, x_retmsg =>?); END;";
    	else {
    		sql+="(";
    		for (int p=0;p<params.size();p++) {
    			FunctionParamInfo funcPar = (FunctionParamInfo) params.elementAt(p);
    			String parName = funcPar.getParamName();
    			if (p==0) sql+= parName +"=> ?"; else sql+=", "+parName +" => ?";
    		}
    		sql+=",p_username => ?,p_testcase_id => ?, x_retstat => ?, x_retmsg =>?); END;";
    		
    	}
    	
    	log.println("SQL Statement: \n"+sql);
    	CallableStatement cstmt = dbConn.prepareCall(sql);
    	

    	Hashtable testcaseData = testcase.getDataMap(); 
    	
    	log.println("Setting IN Parameters");    	
    	for (int p=0;p<params.size();p++) {
    		FunctionParamInfo funcPar = (FunctionParamInfo) params.elementAt(p);

    		if (funcPar.getParamType().equals("IN") || funcPar.getParamType().equals("INOUT")) {
    		  log.println("  Index: "+(p+1));
      		  log.println("  Name: "+funcPar.getParamName());
    		  log.println("  Type: "+funcPar.getParamType());    			
    		  String parName = funcPar.getParamName();

    		  if (testcaseData.containsKey(parName)) {
    			  String val = (String)testcaseData.get(parName);
    			  log.println("  Val: "+val);
    			  if (!val.equals("") && !val.equals("null")) 
    			    cstmt.setString((p+1), val);
    			  else 
    				cstmt.setNull((p+1), java.sql.Types.VARCHAR);
    		  }
    			 
    		  else
    			 cstmt.setNull((p+1), java.sql.Types.VARCHAR);
    		}
    		if (funcPar.getParamType().equals("OUT") || funcPar.getParamType().equals("INOUT")) {
    			cstmt.registerOutParameter((p+1), java.sql.Types.VARCHAR);
    		}
    	}

		int numParams = params.size();
		log.println("setting p_username (Index = "+(numParams+1)+"):"+appUserName);

		if (appUserName!=null && !appUserName.equals(""))
    	  cstmt.setString(numParams+1,appUserName);
		else 
	       cstmt.setString(numParams+1,"-1");
		
		log.println("setting p_testcaseid (Index = "+(numParams+2)+"):"+testcase.getTestcaseId());
		if (testcase.getTestcaseId()!=null && !testcase.getTestcaseId().equals(""))
    	  cstmt.setString(numParams+2,testcase.getTestcaseId());
		else 
	      cstmt.setString(numParams+2,"-1");
		  
    	cstmt.registerOutParameter(numParams+3, java.sql.Types.VARCHAR);
    	cstmt.registerOutParameter(numParams+4, java.sql.Types.VARCHAR);
		cstmt.execute();
		log.println("SQL Call complete");
				
		String retStat = cstmt.getString(numParams+3);
		String retMsg = cstmt.getString(numParams+4);		
		
		if (!retStat.equals("S")) {
			log.println("Function returned with error code: "+retStat);
			log.println("Following error message was returned:\n"+retMsg);
			retStatus = "FAIL";
		} else {
		log.println("Function complete with no errors");
		log.println("Retrieving Out Parameters");
		
		
		for (int p=0;p<params.size();p++) {
			FunctionParamInfo funcPar = (FunctionParamInfo) params.elementAt(p);
			
			if (!funcPar.getParamType().equals("IN")) {
                log.println("  Parameter Index: "+(p+1));
     		    log.println("  Name: "+funcPar.getParamName());
    		    log.println("  Type: "+funcPar.getParamType()); 
    		    log.println("  Val: "+cstmt.getString((p+1)));
    		    testcaseData.put(funcPar.getParamName(),cstmt.getString(p+1));
    		}			
		}
		
		Vector dataV = testcase.getDataVect();
        dataV.removeAllElements();
        Enumeration e = testcaseData.keys();
        while (e.hasMoreElements()) {
        	String paramName = (String) e.nextElement();
        	String paramValue = (String) testcaseData.get(paramName);
        	dataV.addElement(paramName+"="+paramValue);
        	
        }
        
        
		for (int p=0;p<params.size();p++) {
			FunctionParamInfo funcPar = (FunctionParamInfo) params.elementAt(p);
			String data = funcPar.getParamName()+"=";
    		if (funcPar.getParamType().equals("IN")) {
    			if (testcaseData.containsKey(funcPar.getParamName())) 
    				data+=(String)testcaseData.get(funcPar.getParamName());
    		} else {
                log.println("  Index: "+(p+1));
     		    log.println("  Name: "+funcPar.getParamName());
    		    log.println("  Type: "+funcPar.getParamType()); 
    		    log.println("  Val: "+cstmt.getString((p+1)));
    			data+=cstmt.getString((p+1));
    		}
    		dataV.addElement(data);
		}
		}
		cstmt.close();

		try {
		System.out.println("Dumping dbms_output");
		log.println("DBMS_OUTPUT :");
		log.println("------------ ");
		dbmsOut.show(log);
		dbmsOut.close();
		} catch (Exception ex) {
			System.out.println("Error dumping dbms_output "+ex.getMessage());
		}
		log.println("DONE. Function complete");
				
    	
    	} catch (Exception e) {

			
			log.println("Caught Exception.\nError: "+e.getMessage());
			retStatus = "FAIL";
			
		}
		log.flush();
		log.close();
		
		testrun.setFunctionStatus(testcase,testcase.getTestcaseName(), ""+funcSeq
				   ,logFile, retStatus);
		
		return retStatus;		
 
    }

    public void doCleanup() throws Exception {
    	if (dbConn != null && !dbConn.isClosed())
    	dbConn.close();
    }
	   
}
