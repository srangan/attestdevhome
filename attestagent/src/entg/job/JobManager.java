package entg.job;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import java.io.*;
import java.util.*;
import entg.util.*;
public class JobManager {
	
	public static Vector agentDrivers;
	
	static {
		agentDrivers = new Vector();
	}  
	public static void registerAgent(AgentDriver ag) {
		
		if (!agentDrivers.contains(ag)) 
			agentDrivers.addElement(ag); 
	}
	public static String parameters;
	public static Hashtable paramList;
	public static String getParameter(String params, String name) {
		if (parameters == null || !parameters.equals(params)) {
			
		 	paramList = new Hashtable();
		 	parameters = params;
			StringTokenizer st = new StringTokenizer(parameters,",");
			while (st.hasMoreElements()) {
				String stTok = st.nextToken();
				StringTokenizer st1 = new StringTokenizer(stTok,"=");
				String paramName=st1.nextToken();
				String paramValue=st1.nextToken();
				paramList.put(paramName, paramValue);
			}
			
		}
		if (paramList.containsKey(name))
			return (String) paramList.get(name);
		return null;
	}
	public static JobAgent getJobAgent(String programId, String jobId, 
			String programName, String parameters) throws Exception {
		  
		Enumeration e = agentDrivers.elements();
		while (e.hasMoreElements()) {
			AgentDriver a = (AgentDriver) e.nextElement();
			if (a.isForProgram(programName)) {
			  return a.getAgent(programId, jobId,parameters);	
			}  
		}
		throw new Exception("No Job Drivers Found");
	}
	static Properties jobMgrProps;
	public static void loadProperties()  throws Exception {
		jobMgrProps = new Properties();
		FileInputStream fin = new FileInputStream(System.getProperty("attest.path")+File.separator+"attest.properties");
		jobMgrProps.load(fin);
	}
	
	public static String getProperty(String name) {
		return jobMgrProps.getProperty(name);
	}
	
	public static void setMgrStatus (Connection conn, String stat, int mgrId) throws Exception {
		
		PreparedStatement pstmt = conn
	     .prepareStatement("update entg_job_managers set status = ?, last_status_upd_date=SYSDATE where mgr_id = ?");
   	    pstmt.setString(1, stat);
		pstmt.setInt(2, mgrId);
   	    pstmt.executeUpdate();
        pstmt.close();
	}
	static String mgrType;
	static String jobMgrType;
	
	public static boolean isSfdcJobMgr(){
		if (jobMgrType!=null && jobMgrType.equalsIgnoreCase("sfdc")) 
			return true;
		else 
			return false;
	}
	public static int initMgr() throws Exception {

		mgrType = jobMgrProps.getProperty("MANAGER_TYPE");
        String mgrName = jobMgrProps.getProperty("MANAGER_NAME");
        if (mgrType==null)
        	mgrType="SERVER";
        
        String userName = jobMgrProps.getProperty("METRIC_DB_USERNAME");
		String pwd = jobMgrProps.getProperty("METRIC_DB_PW");
		 PasswordEncryption encrypter = new PasswordEncryption();
		 String password = encrypter.decrypt(pwd);

		String dbUrl = jobMgrProps.getProperty("METRIC_DB_URL");
	 
		int mgrId = 1;
		
		DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		Connection conn = DriverManager.getConnection(dbUrl, userName,
				password);
		if (mgrType.equals("DESKTOP")) {
			Statement stmt = conn.createStatement();
			ResultSet rs = null;
			rs = stmt.executeQuery("select mgr_id from entg_job_managers where node_name='"
					+java.net.InetAddress.getLocalHost().getHostName()+"'");
			if (!rs.next()) {
			  stmt.close();
			  stmt = conn.createStatement();
			  rs = stmt.executeQuery("select entg_job_managers_s.nextval from dual");
			  rs.next();
			  mgrId = rs.getInt(1);
			  stmt.close();
			} else {
				mgrId = rs.getInt(1);
		    	stmt.close();
		    	PreparedStatement pstmt = conn
			     .prepareStatement("delete from entg_job_managers where mgr_id = ?");
				pstmt.setInt(1, mgrId);
				pstmt.executeUpdate();
		        pstmt.close();
		    	
		    	
		    			
				
			}
			 PreparedStatement pstmt = conn
			     .prepareStatement("insert into entg_job_managers values(?,?,?,?,?,'Alive',SYSDATE)");
	          pstmt.setInt(1, mgrId);
	          pstmt.setString(2, mgrName);
	          pstmt.setString(3, mgrName);
	          pstmt.setString(4, mgrType);
	          pstmt.setString(5, java.net.InetAddress.getLocalHost().getHostName());
	          
	          
	          pstmt.executeUpdate();
	          pstmt.close();
	          
	          setMgrStatus(conn,"Alive",mgrId);
			String mgrProgs = jobMgrProps.getProperty("MANAGER_PROGRAMS");
			StringTokenizer st = new StringTokenizer(mgrProgs,",");
			
			pstmt = conn
		     .prepareStatement("delete from entg_manager_programs where mgr_id = ?");
			pstmt.setInt(1, mgrId);
			pstmt.executeUpdate();
	        pstmt.close();
			while (st.hasMoreTokens()) {
				
				pstmt = conn
			     .prepareStatement("insert into entg_manager_programs values (?,?)");
				pstmt.setInt(1, mgrId);
				pstmt.setInt(2, Integer.parseInt(st.nextToken()));
				pstmt.executeUpdate();
		        pstmt.close();
			}
		}
		
		return mgrId;
	}
	
	public static void main(String args[]) throws Exception {
		
		loadProperties();
		
		if ((jobMgrProps.getProperty("SFDC_CONSOLE") != null && jobMgrProps.getProperty("SFDC_CONSOLE").equals("yes")) ||
		  (args.length>0 && args[1].equals("-sfdc"))) {
			jobMgrType = "SFDC";
			SfdcJobManager.launch(args);
		} else {

		
		
		int mgrId = initMgr();

		String userName = jobMgrProps.getProperty("METRIC_DB_USERNAME");
		String pwd = jobMgrProps.getProperty("METRIC_DB_PW");
		 PasswordEncryption encrypter = new PasswordEncryption();
		 String password = encrypter.decrypt(pwd);

		String dbUrl = jobMgrProps.getProperty("METRIC_DB_URL");
		boolean singleThreaded = 
			(jobMgrProps.getProperty("MULTITHREADED")!=null&&
					jobMgrProps.getProperty("MULTITHREADED").equalsIgnoreCase("FALSE"));
	 
		String mgrProgs = jobMgrProps.getProperty("MANAGER_PROGRAMS");
		StringTokenizer st = new StringTokenizer(mgrProgs,",");
		java.util.Hashtable h = new java.util.Hashtable();
		while (st.hasMoreTokens()) {
		  	h.put(st.nextToken(), "");
		}
		
		//String retrievalMethod = args[0];
		while (true) {
	            System.out.println((new java.util.Date()).toString() + ": Polling ...");
	            
				//		 Load the JDBC-ODBC bridge
	            
				
				DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
				Connection conn = DriverManager.getConnection(dbUrl, userName,
						password);

				setMgrStatus(conn,"Alive",mgrId);
			
  				
				Statement stmt = conn.createStatement();
				ResultSet rs = null;
				rs = stmt.executeQuery("select job_id, p.program_id, name, status, " +
						               "program_impl_class, parameters, j.mgr_id, " +
						               "decode(p.program_id,4,entg_job_pkg.check_testrun_dep(parameters),'Y'), nvl(j.waitfor_job_id,-1) " +
						               " from entg_jobs j, entg_job_programs p " + 
						               "where p.program_id = j.program_id and j.status='PENDING' "+
						               "AND nvl(j.start_date,(SYSDATE-1))<= SYSDATE AND (j.mgr_id = -1 OR j.mgr_id = "+mgrId+")" +
						               		" order by job_id");
				while (rs.next()) {
				  try {
					  
					int jobId = rs.getInt(1);
					int prgId = rs.getInt(2);
					int jobMgrId = rs.getInt(7);
					String params = rs.getString(6);
					
					boolean isDepComplete = (rs.getString(8).equals("Y"));					

					int waitforJobId = rs.getInt(9);
					if (!h.containsKey(""+prgId)) 
					    continue;

					if (!isDepComplete) continue;

					if (waitforJobId != -1) {
						Statement stmt1 = conn.createStatement();
						ResultSet rs1 = stmt1.executeQuery(
								 "select status from entg_jobs where job_id = "+waitforJobId);
						boolean isComplete=false;
						if (rs1.next()) {
							if (rs1.getString(1).equalsIgnoreCase("complete")) 
							  isComplete=true;
						}
						stmt1.close();
						if (!isComplete)
							continue;
					}
					
					try {
					  PreparedStatement pstmt = conn.prepareStatement(
							   "select job_id from entg_jobs where job_id = "+jobId+" for update nowait"
							  );
					  pstmt.execute();
					  pstmt.close();
					  
					  pstmt = conn.prepareStatement(
							  "update entg_jobs set mgr_id = ?, status = 'Processing' " +
							  "where job_id = ?");
					  pstmt.setInt(1, mgrId);
					  pstmt.setInt(2, jobId);
					  pstmt.execute();
					  pstmt.close();
					} catch (Exception ex) {
					  System.out.println("Exception when updating. "+ex.getMessage());
					  continue;	
					}
					
					Job j= new Job(rs.getInt(1)+"",rs.getInt(2)+"",rs.getString(3),rs.getString(5), rs.getString(6));
				    
					if (!j.inited) continue;
					if (singleThreaded) {
						System.out.println("In startjob");
						setMgrStatus(conn,"Busy",mgrId);
						j.run();
						setMgrStatus(conn,"Alive",mgrId);
						System.out.println("After startjob");
					} else {
					  System.out.println("In MT startjob");
					  Thread job = new Thread(j);
					  job.setDaemon(true);
					  job.start();
					}
				  } catch (Exception ex) {
					  ex.printStackTrace();
				  }
				}
				stmt.close();
				conn.close();


			Thread.sleep(5000);
		}

		}
	}
}
