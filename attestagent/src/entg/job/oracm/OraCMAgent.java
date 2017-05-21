package entg.job.oracm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import entg.util.*;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.Properties;
import java.io.*;

import entg.job.Job;
import entg.job.JobAgent;
import entg.job.JobManager;

public class OraCMAgent implements JobAgent {

	int testcaseId, tcfunctionId, tcseqNo;
	String programId, jobId;
	String fLogfile, requestId;

	public OraCMAgent(String programId, String jobId, String parameters) {
		this.programId = programId;
		this.jobId = jobId;
		testcaseId = Integer.parseInt(JobManager.getParameter(parameters,
				"TESTCASE_ID"));
		requestId = JobManager.getParameter(parameters, "REQUEST_ID");
		tcfunctionId = Integer.parseInt(JobManager.getParameter(parameters,
				"TESTCASE_FUNCTION_ID"));
		tcseqNo = Integer.parseInt(JobManager
				.getParameter(parameters, "SEQ_NO"));
		//fLogfile = JobManager.getParameter(parameters, "LOG_FILE");
	}

	public static void copy(File src, File dst) throws IOException {

		InputStream in = new FileInputStream(src);

		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len, len1;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}

		in.close();

		out.close();
	}
 
	public void startJob(Job j) throws Exception {

		j.wlog("In Startjob");

		System.out.println((new java.util.Date()).toString()
				+ ": Checking for completion. Request ID: " + requestId);

		Statement stmt2 = j.conn.createStatement();
		ResultSet rs2 = null;
		System.out.println("Testcase ID: " + testcaseId);
		rs2 = stmt2
		    .executeQuery("SELECT logfile from entg_testcase_functions "+
		    		"where testcase_id = "+testcaseId+" and function_id="+this.tcfunctionId+
		    		" and seq_no = "+this.tcseqNo);
		rs2.next();
		this.fLogfile=rs2.getString(1);
		stmt2.close();
		stmt2 = j.conn.createStatement();
		System.out.println("Testcase ID: " + testcaseId);
		rs2 = stmt2
				.executeQuery("SELECT retrieval_method,ftp_machine_name,ftp_username, "
						+ "ftp_password,logfile_directory,outputfile_directory , d.datasource_url,d.username, entg_utils.getdatasourcepassword(d.datasource_id) FROM "
						+ "entg_testcases t,entg_test_runs r ,entg_application_signatures a ,"
						+ "entg_test_dataset_scenarios ds,entg_testrun_phys_instances tpi,"
						+ "entg_physical_appl_instances pa, entg_test_data_sources d WHERE t.run_id = r.run_id "
						+ "AND ds.scenario_id =t.scenario_id AND ds.data_set_id=r.data_Set_id "
						+ "AND ds.application_instance_id=a.application_signature_id "
						+ "AND tpi.run_id = r.run_id and tpi.application_instance_id = a.application_instance_id "
						+ "and tpi.physical_instance_id = physical_appl_instance_id and pa.datasource_id = d.datasource_id "
						+ "and t.testcase_id=" + testcaseId + " and rownum=1 ");
		rs2.next();
		String retrievalMethod = rs2.getString(1);
		String machineName = rs2.getString(2);
		String ftpUsername = rs2.getString(3);
		String ftpPassword = rs2.getString(4);
		String logfile = rs2.getString(5);
		String outputfile = rs2.getString(6);
		String Username = rs2.getString(8);
		String Password = rs2.getString(9);
		String appsURL = rs2.getString(7);
		stmt2.close();

		boolean concPgmCompleted=false;
		String statusCode=null;
		for (int i=0;i<10;i++) {
		DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		Connection conn1 = DriverManager.getConnection(appsURL, Username,
				Password);
		Statement stmt1 = conn1.createStatement();
		ResultSet rs1 = null;
		rs1 = stmt1
				.executeQuery("select phase_code,status_code from  fnd_concurrent_requests "
						+ " where request_id =" + requestId);
		rs1.next();
		String phaseCode = rs1.getString(1);
		statusCode = rs1.getString(2);
		
		stmt1.close();
		conn1.close();
		if (phaseCode.equals("C")) {
			concPgmCompleted = true;
			break;
		} else {
			Thread.sleep((i+1)*5000);
			
		}
		}
		if (concPgmCompleted) {
			// Oracle apps DB server, port, username, password
			// Make a DB connection to the oracle apps DB
			// select phase_code from fnd_concurrent_requests where requestId =
			// <reqId>
			// if phase_code = 'C', then do the ftp

			// Get a property called retrievalLocation
			String retrievalLocation = JobManager
					.getProperty("retrievallocation");
			if (retrievalMethod.equals("FTP")) {
				System.out.println("Method FTP");
				String str_host_bat_call = new String("open  " + machineName
						+ "\n" + ftpUsername + "\n" + ftpPassword + "\n"
						+ "lcd  " + retrievalLocation + "\n" + "cd " + logfile
						+ "\n" + "binary" + "\n" + "get " + "l" + requestId
						+ ".req" + "\n" + "cd " + outputfile + "\n" + "get "
						+ "o" + requestId + ".out" + "\n" + " bye");

				byte buf[] = str_host_bat_call.getBytes();
				FileOutputStream secondBatchFile = new FileOutputStream(
						"ftp.txt");
				secondBatchFile.write(buf);
				Runtime rt = Runtime.getRuntime();
				String cmd = "ftp -s:ftp.txt";

				Process proc = rt.exec(cmd);
				entg.util.StreamGobbler errorGobbler = new entg.util.StreamGobbler(proc
						.getErrorStream(), "ERR"); // any output?

				entg.util.StreamGobbler outputGobbler = new entg.util.StreamGobbler(proc
						.getInputStream(), "OUT"); // kick them off
				errorGobbler.start();
				outputGobbler.start(); // any error??? int
				System.out.println("Waiting for process .. ");
				int exitVal = proc.waitFor();
			}
			if (retrievalMethod.equals("COPY")) {
				System.out.println("Method COPY");
				String src1 = logfile + "/l" + requestId + ".req";
				String dst = retrievalLocation + "l" + requestId + ".req";
				String dst1 = retrievalLocation + "/o" + requestId + ".out";
				String src = outputfile + "o" + requestId + ".out";
				File srcfile = new File(src);
				File dstfile = new File(dst);
				File dstfile1 = new File(dst1);
				File src1file = new File(src1);
				copy(srcfile, dstfile);
				copy(src1file, dstfile1);

			}
			if (retrievalMethod.equals("SCP")) {
				ScpFrom.doScpCopy(ftpUsername, machineName, ftpPassword, logfile+"/"+"l" + requestId
						+ ".req", retrievalLocation);
				
				ScpFrom.doScpCopy(ftpUsername, machineName, ftpPassword, outputfile + "/"
						+ "o" + requestId + ".out", retrievalLocation);
				
			}

			String status;
			PreparedStatement pstmt = j.conn
					.prepareStatement("update entg_testcase_functions "
							+ "set processed_flag='Y' where request_id=?");

			pstmt.setString(1, requestId);
			pstmt.execute();

			if (statusCode.equals("C")) {
				status = "PASS";
			} else {
				status = "FAIL";
			}

			CallableStatement cstmt = j.conn
					.prepareCall("begin ENTG_UTILS.set_testcase_function_status(?,?,?,?,?); end;");
			cstmt.setString(1, status);
			cstmt.setString(2, fLogfile);
			cstmt.setInt(3, testcaseId);
			cstmt.setInt(4, tcfunctionId);
			cstmt.setInt(5, tcseqNo);
			cstmt.execute();
			cstmt.close();
			j.conn.commit();
			// if statusCode = 'C' then status is PASS, else status
			// is FAIL
			// Callablestatement
			// call entg_utils.set_testcase_function_status
		
		} else {
		  j.wlog("**** Timed out. Concurrent Program not completing");
		}
		

		j.jobCompleted();
	}

}
