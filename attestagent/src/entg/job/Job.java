package entg.job;

import java.io.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.MultipartPostMethod;

import entg.test.TCAgentMain;
import entg.util.*;

public class Job implements Runnable {

	JobAgent e;

	String jobId;

	public PrintWriter out;

	public Connection conn;

	public String logFileName;

	boolean inited = false;
	PreparedStatement pstmt;
	String programId;

	public Job(String jobId, String programId, String programName,
			String programClass, String parameters) throws Exception {

		System.out.println("Class: " + programClass);

		this.jobId = jobId + "";
		this.programId = programId;

		logFileName = "entg_" + programId + "_" + jobId + ".log";
		out = new PrintWriter(new BufferedWriter(new FileWriter(
				JobManager.getProperty("JOB_LOG_DIR") + File.separator
						+ logFileName)));

		out.println("******************************************************************");
		out.println("Program Name: " + programName);
		out.println("Job ID:" + jobId);
		out.println("Program ID:" + programId);
		out.println("Current Time:" + (new java.util.Date()).toString());
		out.println("******************************************************************");
		out.flush();

		if (JobManager.isSfdcJobMgr())
			SfdcJobManager.updateJobStatus(jobId, "Processing", "entg_"
					+ programId + "_" + jobId + ".log");

		else {
			String userName = JobManager.getProperty("METRIC_DB_USERNAME");
			String pwd = JobManager.getProperty("METRIC_DB_PW");
			PasswordEncryption encrypter = new PasswordEncryption();
			String password = encrypter.decrypt(pwd);

			String dbUrl = JobManager.getProperty("METRIC_DB_URL");
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			conn = DriverManager.getConnection(dbUrl, userName, password);
			pstmt = conn
					.prepareStatement("update entg_jobs set status = 'Processing',log='entg_"
							+ programId + "_" + jobId + ".log' where job_Id=?");
			pstmt.setInt(1, Integer.parseInt(jobId));
			pstmt.executeUpdate();
			pstmt.close();

		}
		try {
			Class.forName(programClass);
			e = JobManager.getJobAgent(programId, jobId, programName,
					parameters);
			inited = true;
		} catch (Exception e) {
			try {
				e.printStackTrace(out);
				out.println("Error occured in Job. Error: " + e.getMessage());
				out.println("Completion Time:"
						+ (new java.util.Date()).toString());
				if (JobManager.isSfdcJobMgr())
					SfdcJobManager.updateJobStatus(jobId, "Error", "entg_"
							+ programId + "_" + jobId + ".log");

				else {
					pstmt = conn
							.prepareStatement("update entg_jobs set status = 'Error' where job_Id=?");
					pstmt.setInt(1, Integer.parseInt(jobId));
					pstmt.executeUpdate();
					conn.close();
				}
				out.flush();
				out.close();

			} catch (Exception ex) {
				System.out.println("Error: " + ex.getMessage());
			}
		}

	}

	public void run() {
		try {

			e.startJob(this);
		} catch (Exception e) {
			try {
				e.printStackTrace(out);

				out.println("Error occured in Job. Error: " + e.getMessage());
				out.println("Completion Time:"
						+ (new java.util.Date()).toString());

				if (JobManager.isSfdcJobMgr())
					SfdcJobManager.updateJobStatus(jobId, "Error", "entg_"
							+ programId + "_" + jobId + ".log");

				else {
					PreparedStatement pstmt = conn
							.prepareStatement("update entg_jobs set status = 'Error' where job_Id=?");
					pstmt.setInt(1, Integer.parseInt(jobId));
					pstmt.executeUpdate();
					conn.close();
				}
				out.flush();
				out.close();

			} catch (Exception ex) {
				System.out.println("Error: " + ex.getMessage());
			}
		}
	}

	public void jobCompleted() throws Exception {
		out.println("Completion Time:" + (new java.util.Date()).toString());

		if (JobManager.isSfdcJobMgr())
			SfdcJobManager.updateJobStatus(jobId, "Error", "entg_" + programId
					+ "_" + jobId + ".log");

		else {
			CallableStatement cstmt = conn
					.prepareCall("begin ENTG_JOB_PKG.set_job_complete(?,?); end;");

			cstmt.setInt(1, Integer.parseInt(jobId));
			cstmt.setString(2, logFileName);
			cstmt.execute();
			cstmt.close();
		}

		/*
		 * PreparedStatement pstmt = conn .prepareStatement(
		 * "update entg_jobs set status = 'Completed', log=? where job_Id=?");
		 * pstmt.setString(1, logFileName); pstmt.setInt(2, jobId);
		 * 
		 * pstmt.executeUpdate(); ; pstmt.close();
		 */
		out.flush();
		out.close();
		Thread.sleep(5000);
		conn.close();

		if (!JobManager.mgrType.equals("SERVER")) {
			uploadLogFile(JobManager.getProperty("JOB_LOG_DIR")
					+ File.separator + logFileName);
		}
	}

	public void uploadLogFile(String logFile) {
		try {
			System.out.println("In upload log ");
			String uploadUrl = JobManager.getProperty("ATTEST_URL")
					+ "/servlets/UploadServlet";
			HttpClient client = new HttpClient();

			String proxyHost = TCAgentMain.TCProperties
					.getProperty("HTTP_PROXY_HOST");
			if (proxyHost != null && !proxyHost.equals("")) {
				int proxyPort = 80;

				String p = TCAgentMain.TCProperties
						.getProperty("HTTP_PROXY_PORT");
				if (p != null && !p.equals(""))
					proxyPort = Integer.parseInt(p);

				client.getHostConfiguration().setProxy(proxyHost, proxyPort);
				client.getParams()
						.setParameter("http.useragent", "Test Client");
			}

			MultipartPostMethod filePost = new MultipartPostMethod(uploadUrl);
			client.setConnectionTimeout(8000);

			// Send any XML file as the body of the POST request
			File f = new File(logFile);
			filePost.addParameter(f.getName(), f);

			client.setConnectionTimeout(5000);
			int status = client.executeMethod(filePost);
			filePost.releaseConnection();
			System.out.println("After upload log ");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void wlog(String msg) {
		out.println(msg);
		out.flush();
	}

}
