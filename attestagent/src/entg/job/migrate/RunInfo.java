package entg.job.migrate;

import entg.job.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import java.io.*;
import java.sql.*;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;
import entg.util.*;

public class RunInfo {
	String name;
	int runId;
	int runStepId;
    Vector functions;
	public RunInfo(){
		functions=new Vector();
	}
	
	public void addFunction (FunctionInfo f){
		functions.addElement(f);
	}
	public void setName(String name){
		this.name=name;
	}
	public String  getName(){
		return name;
	}
	
	public void setRunId(int runId){
		this.runId=runId;
	}
	public int getRunId(){
		return runId;
	}
	
	public void setRunStepId(int runStepId){
		this.runStepId=runStepId;
	}
	public int getRunStepId(){
		return runStepId;
	}
	
	
	public static RunInfo initRunInfo(String exmlFile) throws IOException,SAXException{
		Digester digester = new Digester();
		digester.setValidating(false);

		// instantiate testClientXMLParser class
		digester.addObjectCreate("setupagentdriver", RunInfo.class);
		// set type property of Contact instance when 'filename' attribute is
		// found
		digester.addSetProperties("setupagentdriver", "run_id", "runId");
		digester.addSetProperties("setupagentdriver", "run_step_id", "runStepId");
			
		digester.addSetProperties("setupagentdriver", "run_name", "name");
		
		digester.addObjectCreate("setupagentdriver/function", FunctionInfo.class);
		digester.addSetProperties("setupagentdriver/function", "name", "functionName");
		digester.addSetProperties("setupagentdriver/function", "id", "functionId");
		digester.addSetProperties("setupagentdriver/function", "seq", "funcSeq");
		
		digester.addBeanPropertySetter("setupagentdriver/function/appsignondata/data",
		            "signonData");
		digester.addBeanPropertySetter("setupagentdriver/function/location",
				"functionScript");
		digester.addBeanPropertySetter("setupagentdriver/function/migdata/url",
   		      "dataUrl");

		digester.addSetNext( "setupagentdriver/function", "addFunction" );		
		
		
        RunInfo t = (RunInfo) digester.parse( new File(exmlFile) );
        
        
	    return t;
   }
	
	public void doRun(Connection conn, PrintWriter out, int runStepId) throws Exception {
		String agentRootDir = JobManager.getProperty("MIGRATE_AGENT_DIR");
		
		File f = new File(agentRootDir+File.separator+"run"+runId);
		if (f.isDirectory()) {
			out.println("Directory already exists. Renaming");
			File rF = new File (
					agentRootDir+File.separator+"previous_runs"+File.separator+"run"+runId+ "_"
			+ (new java.util.Date()).toString().replace(' ', '_')
					.replace(':', '_'));
			f.renameTo(rF);
		}
		out.println("Creating run directory");
				
		f.mkdir();
		
		f=new File(agentRootDir+File.separator+"run"+runId+File.separator+"logs");
		f.mkdir();
		f=new File(agentRootDir+File.separator+"run"+runId+File.separator+"data");
		f.mkdir();
		
		for (int i=0;i<functions.size();i++) {
			
			FunctionInfo func = (FunctionInfo)functions.elementAt(i);


			func.doRun(agentRootDir+File.separator+"run"+runId,conn,out,runStepId);
		}
		   
	}
	
	
	public static void main(String args[]) throws Exception {
		JobManager.loadProperties();
		String fileName = JobManager.getProperty("TMPDIR")+java.io.File.separator+
	       "run"+System.currentTimeMillis()+".sxml";
		
		MigrateAgent.exmlFix(args[0],fileName);
		
		RunInfo t = initRunInfo(fileName); 
		
		String userName = JobManager.getProperty("METRIC_DB_USERNAME");
		String pwd = JobManager.getProperty("METRIC_DB_PW");
		String dbUrl = JobManager.getProperty("METRIC_DB_URL");
        PasswordEncryption encrypter = new PasswordEncryption();
        String password = encrypter.decrypt(pwd);
		DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		Connection conn = DriverManager.getConnection(dbUrl, userName, password);
		String logFileName = "Entg_"+t.runId+"_"+t.runStepId+".log";
		PrintWriter out = new PrintWriter(
				  new BufferedWriter(new FileWriter(
				JobManager.getProperty("MIGRATE_AGENT_DIR")
				+ File.separator + logFileName)));

		String cmd = JobManager.getProperty("AGENT_ROOT_DIRECTORY")+File.separator+"bin"+File.separator+"Wintail "+JobManager.getProperty("MIGRATE_AGENT_DIR")
		+ File.separator + logFileName;
		Runtime rt = Runtime.getRuntime();

		Process proc = rt.exec(cmd);
		
		t.doRun(conn,out,t.runStepId);
		
		
	}
}