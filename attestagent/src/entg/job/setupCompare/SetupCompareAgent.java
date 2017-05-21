package entg.job.setupCompare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;

import entg.job.Job;
import entg.job.JobAgent;
import entg.job.JobManager;

public class SetupCompareAgent implements JobAgent {

	  int setupCompareId;
	  
	  String jobId, programId;

	  public SetupCompareAgent() {
		  
	  }
		public SetupCompareAgent(String jobId, String programId, String parameters) {
			this.programId = programId;
			this.jobId = jobId;
			setupCompareId = Integer.parseInt(
					  JobManager.getParameter(parameters, "SETUP_COMPARE_ID"));
			
		}
	  
	  

		private void runStgFunction(Connection extConn,String procName,
				  String funcName,String  stgTable) throws Exception { 

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
		"DECLARE  x_user_id number; x_resp_appl_id number; x_resp_id number; "+
		" begin select user_id into x_user_id from fnd_user where upper(user_name)=upper(?); "+
		" select application_id, responsibility_id into x_resp_appl_id, x_resp_id "+
		" from fnd_responsibility_tl where responsibility_name=? and language='US'; "+
		" fnd_global.apps_initialize( user_id => x_user_id, resp_id => x_resp_id, "+
		" resp_appl_id => x_resp_appl_id); end;";
			  
		CallableStatement cstmt = extConn.prepareCall(initSql);
		cstmt.setString(1, un);
		cstmt.setString(2, resp);
		cstmt.execute();
		cstmt.close();					 
		} catch (Exception ex) {
			System.out.println("Error in setappcontext "+ex.getMessage());
		}
	}
	
	public void startJob(Job j) throws Exception {

		j.wlog("In Startjob");
		PreparedStatement pstmt = j.conn.prepareStatement("update entg_setup_comparisons " +
				"set status=?, job_id=? where compare_run_id=?");
		pstmt.setString(1, "Processing");
		pstmt.setInt(2, -1);
		pstmt.setInt(3, setupCompareId);
		 
	    pstmt.execute();
	    pstmt.close();

	    j.wlog("Initializing Compare Session ..");
	    

		CallableStatement cstmt = j.conn
		   .prepareCall("begin ?:= ENTG_SETUP_COMPARE_PKG.initCompareSession(?); end;");
		cstmt.registerOutParameter(1, java.sql.Types.VARCHAR);
		cstmt.setInt(2, setupCompareId);
		
        cstmt.execute();
        String dbLinkStmt = cstmt.getString(1);
        cstmt.close();

        j.wlog("DONE");
        String retStat = dbLinkStmt;
        if (dbLinkStmt.startsWith("ERROR")) {

        	j.wlog("Error in InitCompareSession "+retStat);
        	pstmt = j.conn.prepareStatement("update entg_setup_comparisons " +
			"set status=? where compare_run_id=?");
	        pstmt.setString(1, "Error");
	        pstmt.setInt(2, setupCompareId);
            pstmt.execute();
            pstmt.close();
            j.out.flush();
        	
            throw new Exception ("Compare Agent Error: "+retStat);
        	 
        	
        }
        j.wlog("DBLinkStatement "+dbLinkStmt);
        j.wlog("Creating Connections to compare Instances");
        Statement stmt = j.conn.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT compare_instance1_id,compare_instance2_id" +
			" FROM entg_setup_comparisons where compare_run_id = "+setupCompareId);
	    rs.next();
	    String inst1=rs.getString(1);
	    String inst2=rs.getString(2);
     	stmt.close();
	
	    Statement stmt1 = j.conn.createStatement();
	    ResultSet rs1 = stmt1.executeQuery("SELECT datasource_url,username,entg_utils.getPasswordDecrypted(d.password) " +
			" FROM entg_test_data_sources d,entg_physical_appl_instances p where p.datasource_id=d.datasource_id "+
			" and physical_appl_instance_id="+inst1);
	    rs1.next();
	    String url1=rs1.getString(1);
	    String uname1=rs1.getString(2);
	    String pwd1=rs1.getString(3);
	
	    stmt1.close();
	
	    
	
	    Statement stmt2 = j.conn.createStatement();
	    ResultSet rs2 = stmt2.executeQuery("SELECT datasource_url,username,entg_utils.getPasswordDecrypted(d.password) " +
			" FROM entg_test_data_sources d,entg_physical_appl_instances p where p.datasource_id=d.datasource_id "+
			" and physical_appl_instance_id="+inst2);
	    rs2.next();
	    String url2=rs2.getString(1);
	    String uname2=rs2.getString(2);
	    String pwd2=rs2.getString(3);
	
	
	    stmt2.close();
	
	    Connection extConn1 = DriverManager.getConnection(url1, uname1,
				pwd1); 
	    Connection extConn2 = DriverManager.getConnection(url2, uname2,
				pwd2);
	    j.wlog("DONE");

	    j.wlog("Create DB Links to setupcompare schema");
	    stmt2 = j.conn.createStatement();

	    PreparedStatement pstmtE1 = extConn1.prepareStatement(dbLinkStmt);
	    pstmtE1.execute();
	    pstmtE1.close();
	    
	    PreparedStatement pstmtE2 = extConn2.prepareStatement(dbLinkStmt);
	    pstmtE2.execute();
	    pstmtE2.close();

	    j.wlog("DONE");
	    
		PreparedStatement pstmt1 = j.conn.prepareStatement("select user_name "+
                "from si_users s, entg_setup_comparisons r "+
                "where r.created_by = s.user_id "+
                "and r.compare_run_id = ?");
		pstmt1.setInt(1, setupCompareId);
		ResultSet rs3 = pstmt1.executeQuery();
		rs3.next();
		String appsUN = rs3.getString(1);
		

		pstmt1.close();

	    
	    boolean partialComp = false;
	    stmt2 = j.conn.createStatement();
	    rs2 = stmt2.executeQuery("select f.function_id, f.function_name, " +
	    		"f.extraction_type, f.plsql_procedure_name,  f.staging_table, nvl(cf.responsibility_id,-1) " +
	    		"from entg_setup_compare_functions cf, entg_setup_functions f "+
	    		"where cf.function_id = f.function_id "+
	    		"and cf.compare_run_id = "+this.setupCompareId);
	    
	    
	    
	    while (rs2.next()) {
	    	int fId = rs2.getInt(1);
	    	String fType = rs2.getString(3);
	    	String fName = rs2.getString(2);
	    	int respId = rs2.getInt(6);

	    	if (respId != -1) {
  	          this.setAppContext(j.conn, extConn1, respId, appsUN);
	          this.setAppContext(j.conn, extConn2, respId, appsUN);	        
	    	}
	    	
	    	j.wlog("Comparing Function: "+fName);

	    	if (fType.equals("PL/SQL")) {
	          String pName = rs2.getString(4);
	          j.wlog("PLSQL Proc .. Running staging procedures");
	          String stgTable = rs2.getString(5);
	          
	    	  this.runStgFunction(extConn1, pName, fName, stgTable);
	    	  this.runStgFunction(extConn2, pName, fName, stgTable);
	    	  j.wlog("DONE");
	    	}
	    	
	    
	    	
	    	j.wlog("Calling PLSQL Proc for comparison");
	    	
	    	cstmt = j.conn
			   .prepareCall("begin ? := ENTG_SETUP_COMPARE_PKG.compareFunction(?,?,?); end;");
			cstmt.registerOutParameter(1, java.sql.Types.VARCHAR);
			cstmt.setInt(2, fId);
			cstmt.registerOutParameter(3, java.sql.Types.VARCHAR);
			cstmt.registerOutParameter(4, java.sql.Types.VARCHAR);			
	        cstmt.execute();
	        retStat = cstmt.getString(1);
	        String insertStr1 = cstmt.getString(3);
	        String insertStr2 = cstmt.getString(4);

	        j.wlog(insertStr1);
	        j.wlog(insertStr2);	        
	        cstmt.close();
	        if (retStat.startsWith("ERROR")) {
	        	partialComp = true;
	        	j.wlog("Error in comparison .. "+retStat);
		        j.out.flush();	        	
	        	continue;
	        } else {
	        	j.wlog("DONE");
	        	
	        }

	        this.setAppContext(j.conn, extConn1, respId, appsUN);
	        this.setAppContext(j.conn, extConn2, respId, appsUN);	        
	        j.wlog("Insert rows from Instance 1...");
	        pstmtE1 = extConn1.prepareStatement(insertStr1);
		    pstmtE1.execute();
		    pstmtE1.close();
		    j.wlog("DONE");
		    
	        j.wlog("Insert rows from Instance 2...");
	        pstmtE2 = extConn2.prepareStatement(insertStr2);
		    pstmtE2.execute();
		    pstmtE2.close();
		    j.wlog("DONE");
		    
	    	cstmt = j.conn
			   .prepareCall("begin ?:= ENTG_SETUP_COMPARE_PKG.compareFunctionContd(?); end;");
			cstmt.registerOutParameter(1, java.sql.Types.VARCHAR);
			cstmt.setInt(2, fId);
	        cstmt.execute();
	        retStat = cstmt.getString(1);
	        cstmt.close();	        
	        if (retStat.startsWith("ERROR")) {
	        	partialComp = true;
	        	j.wlog("Error in comparison .. "+retStat);
		        j.out.flush();	        	
	        	continue;	        	
	        } else {
	        	j.wlog("DONE");
	        	
	        }

	        j.out.flush();
	        
	
	    }
	    stmt2.close();
	    extConn1.close();
	    extConn2.close();
	    
		pstmt = j.conn.prepareStatement("update entg_setup_comparisons " +
		"set status=? where compare_run_id=?");
        pstmt.setString(1, partialComp?"PARTIALLY_COMPLETE":"COMPLETE");
        pstmt.setInt(2, setupCompareId);
        pstmt.execute();
        pstmt.close();

		cstmt = j.conn
		   .prepareCall("begin ?:= ENTG_SETUP_COMPARE_PKG.closeCompareSession; end;");
		cstmt.registerOutParameter(1, java.sql.Types.VARCHAR);
		
        cstmt.execute();
        retStat = cstmt.getString(1);
        cstmt.close();
        j.wlog("DONE");
        if (retStat.startsWith("ERROR")) {
        	j.wlog("Error in closing compare Session:");
        	j.wlog(retStat);
        	j.out.flush();
        	
            throw new Exception ("Compare Agent	 Error: "+retStat);
        	
        }
        this.createSpreadSheetReport(setupCompareId,j.conn);
		j.jobCompleted();
	}
	
	public void createSpreadSheetReport(int compareId, Connection conn) throws Exception {

		HSSFWorkbook wb = new HSSFWorkbook();
		

		
		HSSFCellStyle csBoldLarge = wb.createCellStyle();
		HSSFFont f = wb.createFont();
		f.setFontHeightInPoints((short) 12);
		f.setBoldweight(f.BOLDWEIGHT_BOLD);
		csBoldLarge.setFont(f);
		csBoldLarge.setBorderBottom(csBoldLarge.BORDER_MEDIUM);
		csBoldLarge.setBorderTop(csBoldLarge.BORDER_MEDIUM);
		csBoldLarge.setBorderLeft(csBoldLarge.BORDER_MEDIUM);
		csBoldLarge.setBorderRight(csBoldLarge.BORDER_MEDIUM);
		csBoldLarge.setFillPattern((short) HSSFCellStyle.SOLID_FOREGROUND);
		csBoldLarge.setFillForegroundColor(HSSFColor.LEMON_CHIFFON.index);
		

		HSSFCellStyle csBoldNormal = wb.createCellStyle();
		HSSFFont f1 = wb.createFont();
		f1.setFontHeightInPoints((short) 10);
		f1.setBoldweight(f1.BOLDWEIGHT_BOLD);
		csBoldNormal.setFont(f1);
		csBoldNormal.setBorderBottom(csBoldNormal.BORDER_MEDIUM);
		csBoldNormal.setBorderTop(csBoldNormal.BORDER_MEDIUM);
		csBoldNormal.setBorderLeft(csBoldNormal.BORDER_MEDIUM);
		csBoldNormal.setBorderRight(csBoldNormal.BORDER_MEDIUM);
		csBoldNormal.setFillPattern((short) HSSFCellStyle.SOLID_FOREGROUND);
		csBoldNormal.setFillForegroundColor(HSSFColor.LEMON_CHIFFON.index);
		
		HSSFCellStyle csTabHeader = wb.createCellStyle();
		csTabHeader.setFont(f1);
		csTabHeader.setBorderBottom(csTabHeader.BORDER_MEDIUM);
		csTabHeader.setBorderTop(csTabHeader.BORDER_MEDIUM);
		csTabHeader.setBorderLeft(csTabHeader.BORDER_MEDIUM);
		csTabHeader.setBorderRight(csTabHeader.BORDER_MEDIUM);
		csTabHeader.setFillPattern((short) HSSFCellStyle.SOLID_FOREGROUND);
		csTabHeader.setFillForegroundColor(HSSFColor.LEMON_CHIFFON.index);		
		
		HSSFCellStyle csNormal = wb.createCellStyle();
		HSSFFont f2 = wb.createFont();
		f2.setFontHeightInPoints((short) 10);
		csNormal.setFont(f2);
		csNormal.setBorderBottom(csNormal.BORDER_MEDIUM);
		csNormal.setBorderTop(csNormal.BORDER_MEDIUM);
		csNormal.setBorderLeft(csNormal.BORDER_MEDIUM);
		csNormal.setBorderRight(csNormal.BORDER_MEDIUM);

		
		HSSFCellStyle csBoldRedNormal= wb.createCellStyle();
		csBoldRedNormal.setFont(f2);
		csBoldRedNormal.setBorderBottom(csBoldRedNormal.BORDER_MEDIUM);
		csBoldRedNormal.setBorderTop(csBoldRedNormal.BORDER_MEDIUM);
		csBoldRedNormal.setBorderLeft(csBoldRedNormal.BORDER_MEDIUM);
		csBoldRedNormal.setBorderRight(csBoldRedNormal.BORDER_MEDIUM);
		csBoldRedNormal.setFillPattern((short) HSSFCellStyle.SOLID_FOREGROUND);
		csBoldRedNormal.setFillForegroundColor(HSSFColor.RED.index);
		
		HSSFCellStyle csBoldOrangeNormal= wb.createCellStyle();
		csBoldOrangeNormal.setFont(f2);
		csBoldOrangeNormal.setBorderBottom(csBoldOrangeNormal.BORDER_MEDIUM);
		csBoldOrangeNormal.setBorderTop(csBoldOrangeNormal.BORDER_MEDIUM);
		csBoldOrangeNormal.setBorderLeft(csBoldOrangeNormal.BORDER_MEDIUM);
		csBoldOrangeNormal.setBorderRight(csBoldOrangeNormal.BORDER_MEDIUM);
		csBoldOrangeNormal.setFillPattern((short) HSSFCellStyle.SOLID_FOREGROUND);
		csBoldOrangeNormal.setFillForegroundColor(HSSFColor.ORANGE.index);		
		
		
		HSSFSheet sheet = wb.createSheet("Header");

		sheet.setColumnWidth((short) 0, (short) 1000);
		sheet.setColumnWidth((short) 1, (short) 6000);
		sheet.setColumnWidth((short) 2, (short) 6000);
		sheet.setColumnWidth((short) 3, (short) 6000);
		sheet.setColumnWidth((short) 4, (short) 6000);
		sheet.setColumnWidth((short) 5, (short) 6000);		

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select compare_run_name,status, " +
					"p1.physical_name,p2.physical_name "+
                    "from entg_setup_comparisons c ,entg_physical_appl_instances p1,"+
                    "  entg_physical_appl_instances p2 "+
                  " where p1.physical_appl_instance_id = c.compare_instance1_id and "+
                  "p2.physical_appl_instance_id = c.compare_instance2_id  "+
                  " and c.compare_run_id="+compareId);

			rs.next();
			int r = 0;
			HSSFRow row;
			HSSFCell cell;
			row = sheet.createRow((short) 1);
			cell = row.createCell((short)1);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(csBoldNormal);
			cell.setCellValue("Run Name");			
			cell = row.createCell((short)2);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(csNormal);
			cell.setCellValue(rs.getString(1));	

			row = sheet.createRow((short) 2);
			cell = row.createCell((short)1);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(csBoldNormal);
			cell.setCellValue("Status");			
			cell = row.createCell((short)2);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(csNormal);
			cell.setCellValue(rs.getString(2));

			
			row = sheet.createRow((short) 4);
			cell = row.createCell((short)1);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(csBoldLarge);
			cell.setCellValue("Instances");	
			
			row = sheet.createRow((short) 5);
			cell = row.createCell((short)1);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(csBoldNormal);
			cell.setCellValue("Instance 1");			
			cell = row.createCell((short)2);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(csNormal);
			cell.setCellValue(rs.getString(3));

			row = sheet.createRow((short) 6);
			cell = row.createCell((short)1);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(csBoldNormal);
			cell.setCellValue("Instance 2");			
			cell = row.createCell((short)2);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(csNormal);
			cell.setCellValue(rs.getString(4));						

			stmt.close();
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select function_name,comparison_result,num_rows_inst1,num_rows_inst2,num_differences,cf.function_id  from "+
                    " entg_setup_functions sf,entg_setup_compare_functions cf "+
                    "  where sf.function_id=cf.function_id and "+
                "  cf.compare_run_id="+compareId);
			
			row = sheet.createRow((short) 8);
			cell = row.createCell((short)1);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellStyle(csBoldLarge);
			cell.setCellValue("Functions");	
			
			
			row = sheet.createRow((short) 9);
			cell = row.createCell((short)1);
			cell.setCellStyle(csTabHeader);
			cell.setCellValue("Name");			
			cell = row.createCell((short)2);
			cell.setCellStyle(csTabHeader);
			cell.setCellValue("Result");
			cell = row.createCell((short)3);
			cell.setCellStyle(csTabHeader);
			cell.setCellValue("Rows (Instance 1)");			
			cell = row.createCell((short)4);
			cell.setCellStyle(csTabHeader);
			cell.setCellValue("Rows (Instance 2)");
			cell = row.createCell((short)5);
			cell.setCellStyle(csTabHeader);
			cell.setCellValue("Number of Differences");
			
			int i=0;
			int funcs[] = new int[100];
			String funcNames[] = new String[100];
			int funcCnt = 0;

			
			
			while (rs.next()) {
  			    row = sheet.createRow((short) (10+(i++)));
				cell = row.createCell((short)1);
				cell.setCellStyle(csNormal);
				cell.setCellValue(rs.getString(1));			
				cell = row.createCell((short)2);
				cell.setCellStyle(csNormal);
				cell.setCellValue(rs.getString(2));
				cell = row.createCell((short)3);
				cell.setCellStyle(csNormal);
				cell.setCellValue(rs.getString(3));			
				cell = row.createCell((short)4);
				cell.setCellStyle(csNormal);
				cell.setCellValue(rs.getString(4));
				cell = row.createCell((short)5);
				cell.setCellStyle(csNormal);
				cell.setCellValue(rs.getString(5));
				
				funcNames[funcCnt] = rs.getString(1);
				funcs[funcCnt++]=rs.getInt(6);
				
			}
			
			stmt.close();
			
			
			for (i = 0;i<funcCnt;i++) {
				HSSFSheet dSheet = wb.createSheet(funcNames[i]);
				r = 1;
				row = dSheet.createRow((short) r);
				stmt = conn.createStatement();
				rs=stmt.executeQuery("select parameter_name from entg_setup_function_params "+
				            " where function_id ="+funcs[i]);
				int colcnt=0;
				cell = row.createCell((short)(0));
				cell.setCellStyle(csTabHeader);
				cell.setCellValue("Row #");				
				cell = row.createCell((short)(1));
				cell.setCellStyle(csTabHeader);
				cell.setCellValue("Differences");
				dSheet.setColumnWidth((short) 0, (short) 2000);	
				dSheet.setColumnWidth((short) 1, (short) 4000);	
				cell = row.createCell((short)(2));
				cell.setCellStyle(csTabHeader);
				cell.setCellValue("Instance");
				dSheet.setColumnWidth((short) 2, (short) 4000);					
				while (rs.next()) {
					cell = row.createCell((short)(3+colcnt));
					cell.setCellStyle(csTabHeader);
					cell.setCellValue(rs.getString(1));
					dSheet.setColumnWidth((short) (3+colcnt++), (short) 4000);	
					
				}
				stmt.close();
				r++;
				stmt = conn.createStatement();
				rs=stmt.executeQuery("select compare_instance1_id, compare_instance2_id, P1.physical_name, P2.physical_name, "
				    +" entg_setup_compare_pkg.getCompareKeyStr("+funcs[i]+") "
				    +" FROM entg_setup_comparisons R, entg_physical_appl_instances P1, entg_physical_appl_instances P2 "
				    +" WHERE compare_run_id = "+compareId+" AND P1.PHYSICAL_APPL_INSTANCE_ID = R.Compare_Instance1_Id AND P2.PHYSICAL_APPL_INSTANCE_ID = R.Compare_Instance2_Id ");

				rs.next();
				int phys1Id = rs.getInt(1);
				String phys1Name = rs.getString(3);
				int phys2Id = rs.getInt(2);
				String phys2Name = rs.getString(4);
				String compareStr = rs.getString(5);

				stmt.close();
				
				// Different Rows
				stmt = conn.createStatement();
				rs=stmt.executeQuery("SELECT compare_key_id FROM ENTG_SETUP_COMPARISON_KEYS WHERE COMPARE_RUN_ID="+
						  compareId+" AND FUNCTION_ID="+funcs[i] +" AND nvl(EXIST_WITH_DIFF,'N')='Y'");
				int rownumber = 1;
				while (rs.next()) {
					String key1 = rs.getString(1);

					
					String key2 = "";
					Statement stmt1 = conn.createStatement();
					ResultSet rs1=stmt1.executeQuery("SELECT D1.compare_key_id FROM entg_setup_comparison_keys D, entg_setup_comparison_data D1, entg_setup_comparisons R "+
					    " WHERE "+compareStr+ " AND D.COMPARE_KEY_ID = '"+key1+"' "+
					    " AND D1.COMPARE_RUN_ID = D.COMPARE_RUN_ID AND D1.FUNCTION_ID = D.FUNCTION_ID AND R.compare_run_id = D.compare_run_id "+
					    " and d1.INSTANCE_ID = R.compare_instance2_id ");
					if (rs1.next())
						key2 = rs1.getString(1);

					stmt1.close();

					row = dSheet.createRow((short) r);
					cell = row.createCell((short)(0));
					cell.setCellStyle(csNormal);
					cell.setCellValue(rownumber++);					
					
					
					cell = row.createCell((short)(1));
					cell.setCellStyle(csBoldRedNormal);
					cell.setCellValue("Different");
					cell = row.createCell((short)(2));
					cell.setCellStyle(csNormal);
					cell.setCellValue(phys1Name);		
					stmt1 = conn.createStatement();
					rs1=stmt1.executeQuery("select * from "+
                             " entg_setup_comparison_data where compare_key_id='"+key1+"' and "+
                             "function_id ="+funcs[i]);
					while (rs1.next()) {
						for (int j=0;j<colcnt;j++) {
							cell = row.createCell((short)(3+j));
							cell.setCellStyle(csNormal);
							cell.setCellValue(rs1.getString(j+5));	
						}

					}
					stmt1.close();
					r++;					

					HSSFRow row1 = dSheet.createRow((short) r);
					cell = row1.createCell((short)(0));
					cell.setCellStyle(csNormal);
					cell.setCellValue("");					
										
					cell = row1.createCell((short)(1));
					cell.setCellStyle(csNormal);
					cell.setCellValue("");
					cell = row1.createCell((short)(2));
					cell.setCellStyle(csNormal);
					cell.setCellValue(phys2Name);		
					stmt1 = conn.createStatement();
					rs1=stmt1.executeQuery("select * from "+
                             " entg_setup_comparison_data where compare_key_id='"+key2+"' and "+
                             "function_id ="+funcs[i]);
					while (rs1.next()) {
						for (int j=0;j<colcnt;j++) {
							String cVal = rs1.getString(j+5);
							HSSFCell cell1 = row.getCell((short) (3+j));
							String cVal1 = cell1.getStringCellValue();
							cell = row1.createCell((short)(3+j));
							if (!(cVal+"X").equals(cVal1+"X")) {
								cell.setCellStyle(csBoldRedNormal);
								cell1.setCellStyle(csBoldRedNormal);
							} else {
							  cell.setCellStyle(csNormal);
							}
							
							
							cell.setCellValue(cVal);	
							
							
							
						}

					}
					stmt1.close();
					r++;		


				}
				stmt.close();
				


				// In Inst 1
				stmt = conn.createStatement();
				rs=stmt.executeQuery(
						"select * from entg_setup_comparison_data where "+
                        "function_id ="+funcs[i] +" and compare_key_id in ("+
						"SELECT compare_key_id FROM ENTG_SETUP_COMPARISON_KEYS WHERE COMPARE_RUN_ID="+
						  compareId+" AND FUNCTION_ID="+funcs[i] +" AND nvl(ONLY_IN_INST1,'N')='Y')");
				while (rs.next()) {


					row = dSheet.createRow((short) r);
					cell = row.createCell((short)(0));
					cell.setCellStyle(csNormal);
					cell.setCellValue(rownumber++);					
					
					
					cell = row.createCell((short)(1));
					cell.setCellStyle(csBoldOrangeNormal);
					cell.setCellValue("In Instance 1");
					cell = row.createCell((short)(2));
					cell.setCellStyle(csNormal);
					cell.setCellValue(phys1Name);		
						for (int j=0;j<colcnt;j++) {
							cell = row.createCell((short)(3+j));
							cell.setCellStyle(csNormal);
							cell.setCellValue(rs.getString(j+5));	
						}
					r++;					

				}
				stmt.close();			
				

				// In Inst 2
				stmt = conn.createStatement();
				rs=stmt.executeQuery(
						"select * from entg_setup_comparison_data where "+
                        "function_id ="+funcs[i] +" and compare_key_id in ("+
						"SELECT compare_key_id FROM ENTG_SETUP_COMPARISON_KEYS WHERE COMPARE_RUN_ID="+
						  compareId+" AND FUNCTION_ID="+funcs[i] +" AND nvl(NOT_IN_INST1,'N')='Y')");
				while (rs.next()) {


					row = dSheet.createRow((short) r);
					cell = row.createCell((short)(0));
					cell.setCellStyle(csNormal);
					cell.setCellValue(rownumber++);					
					
					
					cell = row.createCell((short)(1));
					cell.setCellStyle(csBoldOrangeNormal);
					cell.setCellValue("In Instance 2");
					cell = row.createCell((short)(2));
					cell.setCellStyle(csNormal);
					cell.setCellValue(phys2Name);		
						for (int j=0;j<colcnt;j++) {
							cell = row.createCell((short)(3+j));
							cell.setCellStyle(csNormal);
							cell.setCellValue(rs.getString(j+5));	
						}
					r++;					

				}
				stmt.close();						
		
				
				// In Both
				stmt = conn.createStatement();
				rs=stmt.executeQuery(
						"select * from entg_setup_comparison_data where "+
                        "function_id ="+funcs[i] +" and compare_key_id in ("+
						"SELECT compare_key_id FROM ENTG_SETUP_COMPARISON_KEYS WHERE COMPARE_RUN_ID="+
						  compareId+" AND FUNCTION_ID="+funcs[i] +" AND nvl(EXIST_with_no_diff,'N')='Y')");
				while (rs.next()) {


					row = dSheet.createRow((short) r);
					cell = row.createCell((short)(0));
					cell.setCellStyle(csNormal);
					cell.setCellValue(rownumber++);					
					
					
					cell = row.createCell((short)(1));
					cell.setCellStyle(csNormal);
					cell.setCellValue("Exists in both");
					cell = row.createCell((short)(2));
					cell.setCellStyle(csNormal);
					cell.setCellValue("Both");		
						for (int j=0;j<colcnt;j++) {
							cell = row.createCell((short)(3+j));
							cell.setCellStyle(csNormal);
							cell.setCellValue(rs.getString(j+5));	
						}
					r++;					

				}
				stmt.close();						
			}
			

		try {

			String saveDir = JobManager.getProperty("CSV_FILE_SAVE_DIR");
			String fileName = "setupCompare_"+compareId+".xls";
			FileOutputStream fileOut = new FileOutputStream(saveDir
					+ File.separator + fileName);
			wb.write(fileOut);

			fileOut.close();
		} catch (IOException io) {
			io.printStackTrace();
		}

	}
	
	public static void main (String args[]) throws Exception {
		DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@yamuna.entegration.com:1521:VIS", "metricstream",
				"metricstream");
		SetupCompareAgent sc = new SetupCompareAgent();
	
		sc.createSpreadSheetReport(106, conn);
		conn.close();
	}
	
	
}
