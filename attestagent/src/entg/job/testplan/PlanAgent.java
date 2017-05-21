package entg.job.testplan;


import entg.job.JobAgent;
import entg.job.JobManager;
import entg.job.Job;
import entg.util.*;
import java.sql.*;

public class PlanAgent implements JobAgent {
	String jobId, programId;

	public PlanAgent(String jobId, String programId, String parameters) {
		this.programId = programId;
		this.jobId = jobId;
		planSchedId = Integer.parseInt(JobManager.getParameter(parameters, "PLANSCHEDULEID"));
		
		userId = Integer.parseInt(JobManager.getParameter(parameters,
		"USERID"));
	}

	int planSchedId, planId,targetInstanceId,userId, mgrId, cycleId;
		
	public void startJob(Job j) throws Exception {

		j.wlog("In Startjob");
		
		Statement stmt = j.conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT PLAN_ID, INSTANCE_ID, CYCLE_ID" +
				" FROM entg_test_plan_schedules where plan_schedule_id = "+planSchedId);
		rs.next();
		
		planId = rs.getInt(1);
		targetInstanceId = rs.getInt(2);
		cycleId = rs.getInt(3);
		stmt.close();
		
		stmt = j.conn.createStatement();
		rs = stmt.executeQuery("select entg_test_plan_executions_s.nextval from dual" );
		rs.next();
		int planExecId = rs.getInt(1);
		stmt.close();
		
		PreparedStatement pstmt = j.conn.prepareStatement("" +
				"INSERT INTO entg_test_plan_executions values " +
				"(?, ?,?,?,SYSDATE,?,SYSDATE)");
		pstmt.setInt(1,planExecId);
		pstmt.setInt(2,planId);
		pstmt.setInt(3,Integer.parseInt(jobId));
		pstmt.setInt(4, userId);
		pstmt.setInt(5, userId); 
		pstmt.execute();
		pstmt.close();
		stmt = j.conn.createStatement();
		rs = stmt.executeQuery("SELECT plan_name, plan_name||'_'||to_char(SYSDATE,'DD_MON_YYYY')||'-'||pr.process_instance_rec_num, suite_id, dataset_id, null, tp.created_by, pr.seq_no " +
				"from entg_test_plan_runs pr, entg_test_plans tp" +
				" where tp.testplan_id = pr.testplan_id AND pr.testplan_id = "+planId + " order by to_number(seq_no ");
		
		int lastSeqNo = 1;
		while (rs.next()) {
			int seqNo = rs.getInt(7);
			
			CallableStatement cstmt = j.conn.prepareCall("begin ? := " +
					"entg_test_runs_pkg.create_testrun(?,?,?,?,?,?,?,?); end;");
			
			cstmt.registerOutParameter(1, Types.NUMERIC);
			cstmt.setString(2, rs.getString(2));
			cstmt.setString(3, "Test Run for plan "+rs.getString(1));
			cstmt.setInt(4,rs.getInt(3));
			cstmt.setInt(5,rs.getInt(4));
			cstmt.setInt(6,this.planSchedId);
			cstmt.setInt(7,cycleId); 
			cstmt.setInt(8,rs.getInt(6));
			cstmt.setInt(9, planExecId);
			cstmt.execute();
			int runId = cstmt.getInt(1);
			cstmt.close();
			
			
			
			cstmt = j.conn.prepareCall("begin ? := " +
			"entg_job_pkg.submit_job(?,?,?,'N'); end;");
			cstmt.registerOutParameter(1, Types.NUMERIC);
			cstmt.setInt(2,4);
			cstmt.setInt(3,-1);
			cstmt.setInt(4,runId);

			cstmt.execute();
			int runJobId = cstmt.getInt(1);
			cstmt.close();
			
			pstmt = j.conn.prepareStatement(
					"update entg_Test_runs set job_id = ? where run_id = ?");
			pstmt.setInt(1, runJobId);
			pstmt.setInt(2, runId);
			
			pstmt.execute();
			pstmt.close();
			
		}
		
		stmt.close();

		j.jobCompleted();
	}
	
		
	
}

