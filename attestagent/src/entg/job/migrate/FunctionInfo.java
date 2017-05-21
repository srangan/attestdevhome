package entg.job.migrate;

import entg.job.*;
import entg.test.TCAgentMain;
import entg.util.StreamGobbler;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.io.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class FunctionInfo {
	String functionName;

	String locationName;

	Vector signondata = new Vector();

	String functionScript;

	String dataUrl; 

	int functionId;
	 
	int funcSeq;

	Connection conn;

	public FunctionInfo() {

	}

	public void setSignonData(String appSign) {
		signondata.addElement(appSign);
	}

	public void setFunctionName(String newfunction) {
		functionName = newfunction;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionId(int functionId) {
		this.functionId = functionId;
	}

	public int getFunctionId() {
		return functionId;
	}

	public void setFuncSeq(int funcSeq) {
		this.funcSeq = funcSeq;
	}

	public int getFuncSeq() {
		return funcSeq;
	}
	
	public void setFunctionScript(String newscript) {
		functionScript = newscript;
	}

	public String getFunctionScript() {
		return functionScript;
	}

	public void setDataUrl(String dataUrl) {
		this.dataUrl = JobManager.getProperty("ATTEST_URL")+ dataUrl;
	}

	public String getDataUrl() {
		return dataUrl;
	}

	public String getLocation() {
		return locationName;
	}

	public void setLocation(String locationName) {
		this.locationName = locationName;
	}

	public void setFunctionStatus(Connection conn, int functionId,int funcSeq, String status)
			throws Exception {
		CallableStatement cstmt = conn
				.prepareCall("begin ENTG_SETUP_UTILS.set_function_status(?,?,?,?); end;");

		cstmt.setInt(1, runStepId);
		cstmt.setInt(2, functionId);
		cstmt.setInt(3, funcSeq);
		cstmt.setString(4, status);
		cstmt.execute();
		cstmt.close();

		if (status.equals("Error") || status.equals("Completed")) {
			PreparedStatement pstmt = conn
					.prepareStatement("update entg_setup_run_migstep_dtls set logon_log_file_location=?,data_log_file_location=? "
							+ "where run_step_id = ? and function_id = ? and seq_no  = ?");
			pstmt.setString(1, this.logonLogFile);
			pstmt.setString(2, this.scriptLogFile);
			pstmt.setInt(3, runStepId);
			pstmt.setInt(4, functionId);
			pstmt.setInt(5, funcSeq); 
			pstmt.execute();
		}
	}

	int runStepId = 0;

	PrintWriter out=null;
	public void doprint(String msg) {
		out.println(msg);
		out.flush();
	}
	public void doRun(String execDir, Connection conn, PrintWriter out,
			int runStepId) throws Exception {

		this.out = out;
		this.conn = conn;
		this.runStepId = runStepId;
		doprint("Running function: " + functionName);
		setFunctionStatus(conn, functionId, funcSeq, "Processing");

		doprint("Creating Directories .. ");
		String logDir = execDir + File.separator + "logs"
				+ File.separator + "l" + functionId+"_"+funcSeq;
		String dataDir = execDir + File.separator + "data"
				+ File.separator + "d" + functionId+"_"+funcSeq;
		File f = new File(logDir);
		f.mkdir();
		f = new File(dataDir);
		f.mkdir();
		doprint("Creating Directories .. done ");

		doprint("Downloading testcase XLS .. ");
		String dataFileName = "f" + functionId + "_"+funcSeq+".xls";
		String dataFile = dataDir + File.separator + dataFileName;
		String status = MigrateAgent.downloadFile(dataUrl, dataFile);
		if (status.equals("SUCCESS"))
			doprint("Downloading testcase XLS .. done ");
		else
			throw new Exception("Error downloading file");

		String logonFileName = "logon_" + functionId+"_"+funcSeq+ ".xls";
		doprint("Creating logon XLS .. ");

		signondata.addElement(new String("RUN_STEP_ID="+this.runStepId));
		signondata.addElement(new String("FUNCTION_ID="+this.functionId));
		signondata.addElement(new String("FUNCTION_SEQ="+this.funcSeq));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("new sheet");

		HSSFRow row = sheet.createRow((short) 0);
		for (int i = 0; i < this.signondata.size(); i++) {
			HSSFCell cell = row.createCell((short) (i));
			StringTokenizer st = new StringTokenizer((String) signondata
					.elementAt(i), "=");
			cell.setCellValue(st.nextToken());
		}
		

		row = sheet.createRow((short) 1);
		for (int i = 0; i < this.signondata.size(); i++) {
			HSSFCell cell = row.createCell((short) (i));
			StringTokenizer st = new StringTokenizer((String) signondata
					.elementAt(i), "=");
			st.nextToken();
			cell.setCellValue(st.nextToken());
		}

		FileOutputStream fileOut = new FileOutputStream(dataDir
				+ File.separator + logonFileName);
		wb.write(fileOut);

		fileOut.close();
		doprint("Creating logon XLS ..  done ");

		doprint("Creating exec script ..   ");
		String execScript = dataDir + File.separator + "s" + functionId+"_"+funcSeq
				+ ".txt";
		PrintWriter scenOut = new PrintWriter(new BufferedWriter(
				new FileWriter(execScript)));
		scenOut.println(JobManager.getProperty("SETUP_SCRIPT_DIRECTORY")+File.separator+JobManager.getProperty("SETUP_LOGON_SCRIPT_NAME") + "$"
				+ dataDir
				+ File.separator + logonFileName);
		scenOut.println(JobManager.getProperty("SETUP_SCRIPT_DIRECTORY")+File.separator+this.functionScript);
		scenOut.close();
		doprint("Creating exec script ..  done ");
		doprint("Running QTP script ..   ");
		runScript(dataFile, logDir, execScript, out);
		doprint("Running QTP script ..  done ");
		// update finish status

	}

	public void runScript(String dataFile, String logDir, String execScript,
			PrintWriter out) throws Exception {

		String tempFile = JobManager.getProperty("TMPDIR") + File.separator
				+ "f_" + functionId + "_"+funcSeq+"comm.txt";

		FileOutputStream fs = new FileOutputStream(new File(tempFile));
		fs.close();

		String cmd =

		"cscript.exe " + JobManager.getProperty("AGENT_ROOT_DIRECTORY")
				+ "\\drivers\\"+JobManager.getProperty("setup_qtp_execution_driver")+

				" "+ execScript + " " + dataFile + " " + logDir + "  " + "0" + " "
				+ tempFile;

	
		doprint(cmd);

		Runtime rt = Runtime.getRuntime();
		ScriptMonitor sm = new ScriptMonitor(tempFile, out, this);
		sm.start();
		Process proc = rt.exec(cmd);
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(),
				"ERR",out); // any output?
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(),
				"OUT",out); // kick them off
		errorGobbler.start();
		outputGobbler.start(); // any error??? int
		doprint("Waiting for process .. ");
		int exitVal = proc.waitFor();
		
		//sm.wait();
		//sm.stop();
		doprint("Done");

	}

	String status = "NEW";

	public void setScriptStatus(String status) throws Exception {

		if (status.equals("PASS"))
			this.status = "Completed";
		else
			this.status = "Error";
		setFunctionStatus(conn, functionId,funcSeq, this.status);
	}

	String logonLogFile;

	String scriptLogFile;

	public void uploadLogDir(String logDir, int funcSeq, PrintWriter out)
			throws Exception {

		String uploadUrl = JobManager.getProperty("ATTEST_URL")
				+ "/servlets/UploadServlet";

		String fileName = JobManager.getProperty("TMPDIR") + File.separator
				+ "report_setup_" +runStepId+"_"+ functionId + "_" + funcSeq + ".qjar";
		String cmd = JobManager.getProperty("AGENT_ROOT_DIRECTORY")
		+ "\\bin\\makeqjar.bat \"" + logDir + "\" "+fileName;
		doprint("File: " + fileName);
		doprint("directory " + logDir);

		Runtime rt = Runtime.getRuntime();

		Process proc = rt.exec(cmd);
		/*try { 
		 Process proc = rt.exec(cmd); 
		 } catch (IOException e1) { 
		 e1.printStackTrace(); 
		 } */
		
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(),
				"ERR", out); // any output?

		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(),
				"OUT", out); // kick them off
		errorGobbler.start();
		outputGobbler.start(); // any error??? int
		doprint("Waiting for process .. ");
		int exitVal = proc.waitFor();	
		doprint("Creating qjar .. Done");
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
    	
		MultipartPostMethod filePost = new MultipartPostMethod(uploadUrl);
		client.setConnectionTimeout(8000);

		// Send any XML file as the body of the POST request
		File f = new File(fileName);
		if (f.isFile()) 
			doprint("File is normal");
		else
			doprint("File is not normal");
		filePost.addParameter(f.getName(), f);
		doprint(f.getName());
		client.setConnectionTimeout(5000);
		int status = client.executeMethod(filePost);
		filePost.releaseConnection();

		doprint("Upload Done");

		if (funcSeq == 1)
			logonLogFile = f.getName();
		else
			scriptLogFile = f.getName();

	}

}