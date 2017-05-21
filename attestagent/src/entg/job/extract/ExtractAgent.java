package entg.job.extract;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.model.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;

import entg.job.JobAgent;
import entg.job.JobManager;
import entg.job.Job;

import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.*;
import java.sql.*; 

public class ExtractAgent implements JobAgent {
	String jobId, programId;

	public ExtractAgent(String jobId, String programId, String parameters) {
		this.programId = programId;
		this.jobId = jobId;
		runId = Integer.parseInt(JobManager.getParameter(parameters, "RUNID"));
		runStepId = Integer.parseInt(JobManager.getParameter(parameters,
				"RUNSTEPID"));
	}
   
	int runId, runStepId;  

	public void setFunctionStatus(Connection conn, int functionId, int seqno, String status)
			throws Exception {
		CallableStatement cstmt = conn
				.prepareCall("begin ENTG_SETUP_UTILS.set_function_status(?,?,?,?); end;");

		cstmt.setInt(1, runStepId);
		cstmt.setInt(2, functionId);
		cstmt.setInt(3, seqno);
		cstmt.setString(4, status);
		cstmt.execute();
		cstmt.close();

	}

	public void createXLS(Connection conn, Connection extConn, String query, String saveDir,
			String fileName,int runStepId,int functionId,int funcSeq)

	{
		

System.out.println("In createXLS. Query "+query);
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("new sheet");
                HSSFCellStyle cellStyle = wb.createCellStyle();
                cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));

		try {

			Statement stmt = extConn.createStatement();
System.out.println("0");
			ResultSet rs = stmt.executeQuery(query);
System.out.println("1");
			ResultSetMetaData rsm = rs.getMetaData();
            int colcount = rsm.getColumnCount();
System.out.println("2");

            int r = 0;
            {
              HSSFRow row = sheet.createRow((short) r++);
              int c = 0;
              for (int i = 1; i <= colcount; i++) {
                    HSSFCell cell = row.createCell((short) c++);
                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                    cell.setCellValue(rsm.getColumnName(i));
                    
              }
            }
System.out.println("After getColumns");

            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery("Select entg_setup_utils.get_migstep_id("+runStepId+") from dual"); 
            rs1.next();
            int migStepId = rs1.getInt(1);
            stmt1.close();
System.out.println("After getMigstep id");
            PreparedStatement pstmt1 = conn.prepareStatement("" +
            		"insert into entg_migration_data values (?,?,?,"+
            		"?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"+
            		"?)"
            		);
            
            
            
            while (rs.next()) {
            	    
                    HSSFRow row = sheet.createRow((short) r++);
                    System.out.println("Here 0");
                    pstmt1.setInt(1, migStepId);
            	    pstmt1.setInt(2, functionId);
            	    pstmt1.setInt(3, r);
            	    System.out.println("Here 1");
                    int c = 0;
                    for (int i = 1; i <= colcount; i++) {
                            HSSFCell cell = row.createCell((short) c++);
                            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                            String val = rs.getString(i);
                            cell.setCellValue(val);
                            cell.setCellStyle(cellStyle);
                            pstmt1.setString((c+3),val);
                    }
                    System.out.println("Here 2");
                    for (int i=(c+3);i<=53;i++) {
                    	pstmt1.setString(i,"");
                    }
                    pstmt1.setInt(54,funcSeq);
                    System.out.println("Here 3");
                    pstmt1.execute();
            }
System.out.println("After create row");

            conn.commit(); 
            pstmt1.close();
			stmt.close();
			

		} catch (SQLException sql) {
				
			System.out.println("Error in createXLS "+sql.getMessage());
		}


	}
	
	private void runStgFunction(Connection extConn,String procName,
			  String funcName,String  stgTable) throws SQLException { 

		String runSql = "begin "+procName+"(?,?); end;";
		CallableStatement cstmt = extConn.prepareCall(runSql);
		cstmt.setString(1, funcName);
		cstmt.setString(2, stgTable);
		cstmt.execute();
		cstmt.close();					 
		
	}
	private void setAppContext( Connection conn, Connection extConn, int respId, String appsUN) 
	     {
		try {
		PreparedStatement pstmt1 = conn.prepareStatement("select resp_name "+
				"from entg_ebs_responsibilities where resp_id = ?");
		pstmt1.setInt(1, respId);
		ResultSet rs1 = pstmt1.executeQuery();
		rs1.next();
		String un = appsUN;
		String resp = rs1.getString(1);
		pstmt1.close();
		 
		
		String initSql = 
		"DECLARE  x_user_id number; x_resp_appl_id number; x_resp_id number; v_org_id NUMBER; "+
		" begin select user_id into x_user_id from fnd_user where upper(user_name)=upper(?); "+
		" select application_id, responsibility_id into x_resp_appl_id, x_resp_id "+
		" from fnd_responsibility_tl where responsibility_name=? and language='US'; "+
		" fnd_global.apps_initialize( user_id => x_user_id, resp_id => x_resp_id, "+
		" resp_appl_id => x_resp_appl_id); "+

		" SELECT fpov.profile_option_value into v_org_id "+
		" FROM FND_PROFILE_OPTION_VALUES fpov, FND_PROFILE_OPTIONS fpo, "+
		" FND_PROFILE_OPTIONS_tl fpot "+
		" WHERE fpo.profile_option_id=fpov.profile_option_id(+) "+
		" AND fpot.LANGUAGE='US' "+
		" AND fpot.profile_option_name=fpo.profile_option_name "+
		" and fpot.profile_option_name='ORG_ID' "+
		" and level_value=(select responsibility_id "+
		" from fnd_responsibility_tl "+
		" where language='US' "+
		" and responsibility_name = ?); "+ 
		" dbms_application_info.set_client_info(v_org_id);  "+
		" END; ";


			  
		CallableStatement cstmt = extConn.prepareCall(initSql);
		cstmt.setString(1, un);
		cstmt.setString(2, resp);
		cstmt.setString(3, resp);
		cstmt.execute();
		cstmt.close();					 
		} catch (Exception ex) {
			System.out.println("Error in setappcontext "+ex.getMessage());
		}
	}

	public boolean validateStep(Connection conn, 
			  int runId, int runStepId, int userId, 
			  String stepType, Job j) throws SQLException 
	{

		Statement stmt = conn.createStatement();

		ResultSet rs = stmt.executeQuery("select rs.status, p.responsibility_id, ds.datasource_url, ds.username, entg_utils.getdatasourcepassword(ds.datasource_id), pi.physical_name, p.pack_Id "+
		"from entg_setup_run_steps rs, entg_setup_runs r, entg_setup_packs p,     entg_setup_workflow_steps ws, entg_physical_Appl_instances pi,     entg_test_data_sources ds "+
		"where rs.run_step_id = "+runStepId+" and rs.run_id = r.run_id and r.pack_id = p.pack_id and rs.workflow_step_id= ws.workflow_step_id and ws.srctrg_app_instance_id = pi.physical_appl_instance_id and pi.datasource_id = ds.datasource_id");
		rs.next();
		String rsStatus = rs.getString(1);
		int packRespId = rs.getInt(2);
		String extDbUrl = rs.getString(3);
		String extDbUn = rs.getString(4);
		String extDbPw = rs.getString(5);
		String migInstName = rs.getString(6);
		int packId = rs.getInt(7);
		String physicalName=rs.getString(6);
		stmt.close();
		boolean allowSubmit = true;
		boolean continueValidation = true;
		String[] problems = new String[20];
		int numProblems=0;
		if (rsStatus.equalsIgnoreCase("completed")) {
		  allowSubmit = false;
		  continueValidation=false;
		  problems[numProblems++] = "Step is already completed.";
		} else if (rsStatus.equalsIgnoreCase("error")) {
		   CallableStatement cstmt1 = conn.prepareCall("begin ENTG_SETUP_UTILS.reset_statuses(?); end;");
		   cstmt1.setInt(1, runStepId);
		   cstmt1.execute();
		   cstmt1.close();
		}
		String userName="";
		if (continueValidation) {
		stmt = conn.createStatement();

		rs = stmt.executeQuery("SELECT upper(entg_utils.getUserName("+userId+")) from dual");
		stmt = conn.createStatement();
		rs.next();
		userName = rs.getString(1);
		stmt.close();


		PreparedStatement pstmt3 = conn.prepareStatement("SELECT USERNAME from entg_Application_users where upper(username) =?");
		pstmt3.setString(1,userName);
		ResultSet rs3 = pstmt3.executeQuery();
		if (!rs3.next()) {
		  allowSubmit = false;
		  continueValidation=false;
		  problems[numProblems++] = "User ["+userName+"] is not defined as an Application User in ["+physicalName+"] " ;
		}
		pstmt3.close();

		}

		Connection extConn = null;
		if (continueValidation) {
		try {
		extConn = DriverManager.getConnection(extDbUrl, extDbUn, extDbPw); 
		} catch (Exception e) {
		  allowSubmit = false;
		  continueValidation=false;
		  problems[numProblems++] = "Error getting DB Connection to "+migInstName+"<BR>"+e.getMessage();
		}
		}
		int ebsUserId = -1;
		if (continueValidation) {
		  PreparedStatement pstmt = extConn.prepareStatement(
		  "select user_id from fnd_user where user_name = ?"
		   );
		  pstmt.setString(1,userName);
		  ResultSet rs1 = pstmt.executeQuery();
		  if (!rs1.next()) {
		    allowSubmit = false;
		    continueValidation=false;
		     problems[numProblems++] = "User ["+userName+"] is not present in the EBS instance: "+migInstName;
		  } else {
		    ebsUserId = rs1.getInt(1);
		  }
		  pstmt.close();
		}

		if (continueValidation) {
		  if (packRespId ==-1) {
		    allowSubmit = false;
		    continueValidation=false;
		    problems[numProblems++] = "No responsibilites chosen in Pack";
		  }
		}

		if (continueValidation) {
		  if (packRespId !=-2) {
		    PreparedStatement pstmt = conn.prepareStatement(
		      "select resp_name from entg_ebs_responsibilities  where "+
		      "resp_id= ?"
		    );
		    pstmt.setInt(1,packRespId);
		    
		    ResultSet rs1 = pstmt.executeQuery();
		    rs1.next();
		    String respName = rs1.getString(1);
		    pstmt.close();
		    
		    pstmt = extConn.prepareStatement(
		      "select responsibility_id from fnd_responsibility_tl where language='US' "+
		      " and responsibility_name = ?"
		    );
		    pstmt.setString(1,respName);
		    rs1 = pstmt.executeQuery();
		    if (!rs1.next()) {
		      allowSubmit = false;
		      continueValidation=false;
		      problems[numProblems++] = "Responsibility with name ["+respName+"] does not exist in Migration instance: "+migInstName;

		    } else {
		      int ebsRespId = rs1.getInt(1);

		      pstmt.close();
		      pstmt = extConn.prepareStatement(
		        "select 1 from fnd_user_resp_groups where user_id = ? and responsibility_id = ?");
		      pstmt.setInt(1,ebsUserId);
		      pstmt.setInt(2,ebsRespId);
		      rs = pstmt.executeQuery();
		      if (!rs.next()) {
		        allowSubmit = false;
		        continueValidation=false;
		        problems[numProblems++] = "User "+userName+" does not have responsibility["+respName+"] in Migration instance: "+migInstName;
		      }
		    }
		  
		  } else {

		    PreparedStatement pstmt2 = conn.prepareStatement(
		      "select distinct resp_name from entg_ebs_responsibilities er, entg_setup_pack_functions pf where er.resp_id= pf.responsibility_id and pf.pack_id = ?"
		     );
		    pstmt2.setInt(1,packId);
		    ResultSet rs2 = pstmt2.executeQuery();
		    while (rs2.next()) {

		      String respName = rs2.getString(1);
		     
		    
		      PreparedStatement pstmt = extConn.prepareStatement(
		        "select responsibility_id from fnd_responsibility_tl where language='US' and responsibility_name = ?"
		      );
		      pstmt.setString(1,respName);
		      ResultSet rs1 = pstmt.executeQuery();
		      if (!rs1.next()) {
		        allowSubmit = false;
		        continueValidation=false;
		        problems[numProblems++] = "Responsibility with name ["+respName+"] does not exist in Migration instance: "+migInstName;

		      } else {
		        int ebsRespId = rs1.getInt(1);

		        pstmt.close();
		        pstmt = extConn.prepareStatement(
		          "select 1 from fnd_user_resp_groups where user_id = ? and responsibility_id = ?");
		        pstmt.setInt(1,ebsUserId);
		        pstmt.setInt(2,ebsRespId);
		        rs = pstmt.executeQuery();
		        if (!rs.next()) {
		          allowSubmit = false;
		          continueValidation=false;
		          problems[numProblems++] = "User "+userName+" does not have responsibility["+respName+"] in Migration instance: "+migInstName;
		        }
		        pstmt.close();
		      }
		    }
		    pstmt2.close();
		  }
		}
		if (extConn != null) 
		extConn.close();
		if (!allowSubmit) {
		  j.wlog("The follow errors encountered:");
		  for (int i=0;i<numProblems;i++) {
			j.wlog(problems[i]);	
			
		  }
		}
		return allowSubmit;
		
	}
	public void startJob(Job j) throws Exception {

		j.wlog("In Startjob");
			
		

		PreparedStatement pstmt1 = j.conn.prepareStatement("update entg_setup_run_steps "+
                "set submission_date=SYSDATE, submitted_by=(select created_by from entg_setup_runs where run_id = ?) "+
                "where run_step_id = ? AND submitted_by is null and submission_date is null");
		pstmt1.setInt(1, runId);
		pstmt1.setInt(2, runStepId);
		pstmt1.execute();
		pstmt1.close();
		
		Statement stmt = j.conn.createStatement();
		ResultSet rs = stmt
				.executeQuery(

				"select datasource_url, username, entg_utils.getdatasourcepassword(ds.datasource_id), rs.submitted_by "
						+ "from entg_physical_appl_instances pi, entg_setup_workflow_steps ws, "
						+ "entg_setup_run_steps rs, entg_test_data_sources ds "
						+ "where rs.run_step_id = "
						+ runStepId
						+ " "
						+ "and rs.workflow_step_id = ws.workflow_step_id "
						+ "and ws.srctrg_app_instance_id = pi.physical_appl_instance_id "
						+ "and ds.datasource_id = pi.datasource_id");
		rs.next();
		String extDbUrl = rs.getString(1);
		String extDbUn = rs.getString(2);
		String extDbPass = rs.getString(3);
		int submittedBy = rs.getInt(4);
		Connection extConn = DriverManager.getConnection(extDbUrl, extDbUn,
				extDbPass);
		stmt.close();
		if (!this.validateStep(j.conn, runId, runStepId, submittedBy, "Extract",j)) {
			j.wlog("Error Occured in Extract Agent");
			
			j.out.flush();
			
			
			throw new Exception("Extract Agent Error: Validation failed");
		}
		pstmt1 = j.conn.prepareStatement("select user_name "+
                "from si_users s, entg_setup_runs r "+
                "where r.created_by = s.user_id "+
                "and r.run_id = ?");
		pstmt1.setInt(1, runId);
		ResultSet rs1 = pstmt1.executeQuery();
		rs1.next();
		String appsUserName = rs1.getString(1);
		

		pstmt1.close();

		j.wlog("Ext Connection Success. URL: " + extDbUrl);

		pstmt1 = j.conn.prepareStatement("select p.pack_id, responsibility_id "+
                "from entg_setup_packs p, entg_setup_runs r "+
                "where r.pack_id = p.pack_id "+
                "and r.run_id = ?");
		pstmt1.setInt(1, runId);
		
		rs1 = pstmt1.executeQuery();
		rs1.next();
		int packId = rs1.getInt(1);
		int packLevelRespId = rs1.getInt(2);
		pstmt1.close();
		
		
		stmt = j.conn.createStatement();
		rs = stmt
				.executeQuery("SELECT f.function_id, extraction_view, ex.seq_no,plsql_procedure_name, staging_table, function_name from entg_setup_run_extstep_dtls ex, entg_setup_functions f "
						+ "where ex.function_id = f.function_id "
						+ "and ex.run_step_id = " + runStepId+" order by ex.seq_no");

		

		while (rs.next()) {
			int functionId = rs.getInt(1);
			String viewName = rs.getString(2);
			int funcSeq = rs.getInt(3);
			String procName = rs.getString(4);
			String stgTable = rs.getString(5);
			String funcName = rs.getString(6);
			j.wlog("Function Id: " + functionId);
			j.wlog("Function Seq: " + funcSeq);
			j.wlog("View: " + viewName);
			j.wlog("Proc Name: "+procName);
			j.wlog("Staging Table: "+stgTable);
		
			
			Statement stmt2 = j.conn.createStatement();
			ResultSet rs2 = stmt2
					.executeQuery("SELECT filter_expr from entg_setup_pack_functions pf "
							+ "where pf.function_id = " +functionId 
							+ "and pf.pack_id = " + packId+" and seq_num = "+funcSeq);
			
			
              rs2.next();
              String filterExpr=rs2.getString(1);
              
             
			// SQL to get filter ..from pacxk_functions. 
			// function_id, and pack_id
			j.wlog("Filter: "+filterExpr);
			try {

				setFunctionStatus(j.conn, functionId, funcSeq,"Processing");
		
				int respId = packLevelRespId;
				if (packLevelRespId == -2) {
				
					pstmt1 = j.conn.prepareStatement("select responsibility_id "+
			                "from entg_setup_pack_functions "+
			                "where pack_id = ? "+
			                "and function_id = ? "+
			                "and seq_num = ?");
					pstmt1.setInt(1, packId);
					pstmt1.setInt(2, functionId);
					pstmt1.setInt(3, funcSeq);
					rs1 = pstmt1.executeQuery();
					rs1.next();
					respId = rs1.getInt(1);
					pstmt1.close();
				}
		
				setAppContext(j.conn,extConn,respId,appsUserName);
				
				if ((viewName == null || viewName.equals("")) && procName!=null) 
				  runStgFunction(extConn,procName,funcName, stgTable);
				String sql = "select * from " + viewName;
				if (viewName == null || viewName.equals(""))
					sql = "select * from " + stgTable;
				
                                if (filterExpr != null && !filterExpr.equals(""))
				   sql=sql+" WHERE  "+filterExpr;
				stmt2.close(); 
				String fileName1 = funcName.replaceAll(" ","_") +"_"+ runStepId + "_" + functionId+ "_" +funcSeq
						+ ".xls";
				createXLS(j.conn,extConn, sql, JobManager
			 			.getProperty("CSV_FILE_SAVE_DIR"), fileName1,runStepId,functionId,funcSeq);
				j.wlog("Data Spreadsheet Created");

				PreparedStatement pstmt = j.conn
						.prepareStatement("update entg_setup_run_extstep_dtls set log_file_location=?,data_file=?,query_id=? "
								+ "where run_step_id = ? and function_id = ? and seq_no = ?");
				pstmt.setString(1, j.logFileName);
				pstmt.setString(2, fileName1);
				pstmt.setInt(3, -1);
				pstmt.setInt(4, runStepId);
				pstmt.setInt(5, functionId);
				pstmt.setInt(6, funcSeq);
				pstmt.execute(); 
				this.setFunctionStatus(j.conn, functionId,funcSeq, "Completed");

			} catch (Exception ex) {
				j.wlog("Error Occured in Extract Agent");
				ex.printStackTrace(j.out);
				j.out.flush();
				setFunctionStatus(j.conn, functionId, funcSeq,"Error");
				if (!extConn.isClosed()) extConn.close();
				try { 
					stmt.close();
				} catch (Exception ex1) {}
				
				throw new Exception("Extract Agent Error: " + ex.getMessage());
			}

			// Create spreadsheet
		}
	        extConn.close();
		stmt.close();

		j.jobCompleted();
		
		
	
		

	}
}
