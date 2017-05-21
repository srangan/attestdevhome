package entg.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.sql.*;
import java.util.Calendar;
import java.util.Properties;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import oracle.jdbc.OracleDriver;

public class TCTestcaseGenerator {

	public TCTestcaseGenerator() {
	}

	public static void main(String args[]) throws Exception {

			System.out.println("Entering Generate Testcases.");
			
			String param1 = args[0];
			String fileName = param1;
			boolean debugFlag=false;
			if (param1.equalsIgnoreCase("-debug")) {
				debugFlag=true;
				fileName = args[1];
			} 
			
			
			FileInputStream fin = new FileInputStream(fileName);
			Properties props = new Properties();
			props.load(fin);
			int runId = Integer.parseInt(props.getProperty("RUN_ID"));
			System.out.println("Run ID: " + runId);
			
			makeMetricDBConn();
			System.out.println("Generating Testcases ..");
			
			genTC(getMetricDBConn(), runId,"LOCAL",null,debugFlag,new java.io.PrintWriter(System.out,true));
			
			System.out.println("DONE");
			closeMetricDBConn();
	}

	public static String downloadXlsFile(String fileName) {
		  return "";
	}
	
	static boolean debugFlag=false;
	static java.io.PrintWriter out;
	public static void d(String msg) {
		if (debugFlag) out.println(msg+"<BR>");
	}
	public static void genTC(Connection conn, int runId, String context, String dirPath, boolean debug, java.io.PrintWriter pout) 
	  throws Exception {

		if (conn.getAutoCommit()) {
			
		}
		debugFlag=debug;
		out=pout;
		
		d("Entering genTC");
			
		Connection conn1 = null;
		Statement stmt, stmt8 = null;
		ResultSet rs, rs8 = null;
		String errAction = "";
		try {

			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("select r.run_id,r.data_set_id,r.test_suite_id, r.run_status,nvl(max_data_rows,0) from entg_test_runs r,entg_test_suites t "
							+ "where r.test_suite_id=t.suite_id and r.run_id = "
							+ runId);
			
			while (rs.next()) {
				if (rs.getString(4).equalsIgnoreCase("NEW")) {
					d("D1: Run status NEW. ");
					int runMaxRows = rs.getInt(5);
					Statement stmt1 = conn.createStatement();
					ResultSet rs1 = stmt1
							.executeQuery("select d.scenario_id, d.data_query_id,"
									+ " d.data_selection_id, d.datasource_id ,data_instance_id "
									+ "from entg_test_dataset_scenarios d "
									+ "where  data_set_id = " + rs.getInt(2));

					while (rs1.next()) {
						int selectionId = rs1.getInt(3);
						int queryId = rs1.getInt(2);
						int scenId = rs1.getInt(1);
						int dataSourceId = rs1.getInt(4);
						

						int dataInstId = rs1.getInt(5);
						
						d("D2: selID="+selectionId+",qID="+queryId+",scenID="+scenId+",datasourceID="+dataSourceId+
								  ",dataInstId="+dataInstId);
						if (dataSourceId == -1) {

							d("D3");
							Statement stmt10 = conn.createStatement();
							ResultSet rs10 = stmt10
									.executeQuery("select  physical_instance_id from "
											+ " entg_testrun_phys_instances where run_id="
											+ runId
											+ " and "
											+ " application_instance_id ="
											+ dataInstId);
							while (rs10.next()) {
								int phyId = rs10.getInt(1);
								Statement stmt11 = conn.createStatement();
								ResultSet rs11 = stmt11
										.executeQuery("select datasource_id from "
												+ "  entg_physical_appl_instances where physical_appl_instance_id="
												+ phyId);
								rs11.next();
								dataSourceId = rs11.getInt(1);
								stmt11.close();

							}
							stmt10.close();

							d("D4:dataSourceId="+dataSourceId);
						}
						if (selectionId == -1) {

							d("D5");
							Statement stmt3 = conn.createStatement();
							ResultSet rs3 = stmt3
									.executeQuery("select query_text,decode(nvl(max_rows,0),0,1000,max_rows),query_type,entg_static_data_scenario_pkg.isXls(process_instance_id),entg_static_data_scenario_pkg.get_File(process_instance_id) from entg_test_data_queries "
											+ "where query_id=" + queryId);
							rs3.next();
							String queryText = rs3.getString(1);
							String queryType = rs3.getString(3);
							String isXls = rs3.getString(4);
							String getFile = rs3.getString(5);
							
							
							int maxRows = rs3.getInt(2);
							stmt3.close();
							
							d("D6: queryType="+queryType+",isXls="+isXls+",getFile="+getFile);
							d("D6: queryText="+queryText );

							if (isXls.equals("N")) {
								
								if (runMaxRows > 0)
									maxRows = runMaxRows;
								String[] cols = new String[100];
								int numcols = 0;

								Statement stmt4 = conn.createStatement();
								ResultSet rs4 = stmt4
										.executeQuery("select column_name,column_seq from entg_test_data_query_columns "
												+ "where query_id ="
												+ queryId
												+ " order by column_seq");
								while (rs4.next()) {
									cols[numcols++] = rs4.getString(1);

								}
								stmt4.close();
								d("D7");
								String[] outcols = new String[100];
								int numoutcols = 0;
								stmt4 = conn.createStatement();
								rs4 = stmt4
										.executeQuery("SELECT DISTINCT UPPER(parameter_name) parameter_name FROM ENTG_TEST_FUNCTION_PARAMS fp, ENTG_TEST_SCENARIO_FUNCTIONS sf WHERE fp.function_id = sf.function_id AND scenario_id = "
												+ scenId
												+ " minus select column_name from entg_test_data_query_columns where query_id ="
												+ queryId);
								while (rs4.next()) {
									outcols[numoutcols++] = rs4.getString(1);

								}
								stmt4.close();

								d("D8");
								Statement stmt5 = conn.createStatement();
								ResultSet rs5 = stmt5
										.executeQuery("select username,entg_utils.getDataSourcePassword(datasource_id),datasource_url from entg_test_data_sources "
												+ "where datasource_id="
												+ dataSourceId);
								rs5.next();
								String uname = rs5.getString(1);
								String pwd1 = rs5.getString(2);
								String dUrl = rs5.getString(3);
								stmt5.close();
								d("D8");
								errAction = "Making database connection to generate testcases. (db URL: "
										+ dUrl + ", User: " + uname + ")";
								conn1 = DriverManager.getConnection(dUrl,
										uname, pwd1);
								errAction = "";
								d("D9");
								Statement stmt6 = conn1.createStatement();
								ResultSet rs6 = stmt6.executeQuery(queryText);
								// ResultSet rs6 =
								// stmt6.executeQuery("select * from entg_925");
								int numRows = 0;
								boolean first = true;
								d("D10");
								while (rs6.next()) {
									d("D10a");
									stmt4 = conn.createStatement();
									rs4 = stmt4
											.executeQuery("select entg_testcases_s.nextval from dual");
									rs4.next();
									int testcaseId = rs4.getInt(1);
									stmt4.close();
									String tData = "<testcase name=\"TEST" + testcaseId
											+ "\">";
									
									if ((first)
											&& (queryType.equals("static") || queryType
													.equals("staticscen"))) {
										first = false;
										continue;
									}

									for (int i = 0; i < numcols; i++) {
										String val = rs6.getString(i + 1);
										if (val == null || val.equals(""))
											val = "";
										tData += "<data>"
												+ org.apache.commons.lang.StringEscapeUtils
														.escapeXml(cols[i]
																+ "=" + val)
												+ "</data>";
									}
									d("D11");
									for (int i = 0; i < numoutcols; i++) {
										if (outcols[i] != null
												&& !outcols[i].equals(""))
											tData += "<data>" + outcols[i]
													+ "=null</data>";
									}



									d("D12");

									tData += "<data>TESTCASE_ID=" + testcaseId
									+ "</data>";
									tData += "</testcase>";
									CallableStatement cstmt = conn
									.prepareCall("begin ENTG_UTILS.save_testcase_data(?,?,?,?); end;");
									cstmt.setInt(1, testcaseId);
									cstmt.setInt(2, runId);
									cstmt.setInt(3, rs1.getInt(1));
									cstmt.setString(4, tData);
									cstmt.execute();
									cstmt.close();
									numRows++;
									d("D12");
									if (numRows == maxRows) {
										
										break;
									}	
									d("D13");
								}
								stmt6.close();
								conn1.close();								
								d("D13a");
							} else { // xls Spreadsheet
								
								d("DX1");
								// Open spreadsheet
								String fileFullPath="";
								if (context.equals("SERVER")) 
								  fileFullPath = dirPath+File.separator+getFile;
								else
									fileFullPath = downloadXlsFile(getFile);
								d("DX2. fileFullPath="+fileFullPath);
								POIFSFileSystem fs = null;
								fs = new POIFSFileSystem(new FileInputStream(fileFullPath));
							

								HSSFWorkbook wb = new HSSFWorkbook(fs);
								HSSFSheet sheet = wb.getSheet("Global");

								String[] cols = new String[100];
								int numcols = 0;
								
								d("DX3. xls opened");
								HSSFRow row = sheet.getRow((short) 0);
								while (true) {
									if (row.getCell((short) numcols) == null)
										break;
									String cellVal = row.getCell((short) numcols).getStringCellValue();
									if (!cellVal.trim().equals(""))
									  cols[numcols++]=cellVal;
								}

								d("DX4. After 1st Ros. numCols="+numcols);
								int rowNum=1;
								while (true) {
									d("DX5: rownum="+rowNum);
									row = sheet.getRow((short) rowNum++);
									String[] colVals = new String[100];
									boolean hasData = false;
									for (int i=0;i<numcols;i++) {
										
										HSSFCell curCell = row.getCell((short) i);
										if (curCell==null) {
											colVals[i]="";
											continue;
										}
										
										int curCellType = curCell.getCellType();
										
										String cellVal = "";

										if (curCellType == HSSFCell.CELL_TYPE_NUMERIC) {
											System.out.println("Numeric cell");
											if (HSSFDateUtil.isCellDateFormatted(curCell)) {
												System.out.println("This is a Date");
												Calendar cal = Calendar.getInstance();
												cal.setTime(HSSFDateUtil.getJavaDate(curCell
														.getNumericCellValue()));
												String value = (String.valueOf(cal.get(Calendar.YEAR)));
												value = cal.get(Calendar.MONTH) + 1 + "/"
														+ cal.get(Calendar.DAY_OF_MONTH) + "/" + value;
												cellVal = value;
											} else {
												cellVal = "" + (int) curCell.getNumericCellValue();
											}
										} else if (curCellType == HSSFCell.CELL_TYPE_STRING) {
											cellVal = curCell.getStringCellValue();
											System.out.println("String cell");
										} else if (curCellType == HSSFCell.CELL_TYPE_FORMULA) {
											double icl = curCell.getNumericCellValue();
											if (!Double.isNaN(icl))
												cellVal = Double.toString(icl);
											else
												cellVal = curCell.getStringCellValue();

										} else {

											System.out.println("Some other cell");
										}
										
										colVals[i]=cellVal;
										if (cellVal!=null && !cellVal.trim().equals("")) {
											hasData=true;
										}
									}

									if (!hasData)
										break;
									d("DX6: rownum="+rowNum+". Has data");
									
									
									Statement stmt4 = conn.createStatement();
									ResultSet rs4 = stmt4
									.executeQuery("select entg_testcases_s.nextval from dual");
									rs4.next();
									int testcaseId = rs4.getInt(1);
									stmt4.close();
									String tData = "<testcase name=\"TEST" + testcaseId
									+ "\">";

									for (int i=0;i<numcols;i++) {
										tData += "<data>"+cols[i]+"="+colVals[i]+"</data>";
									}


									tData += "<data>TESTCASE_ID=" + testcaseId
									+ "</data>";
									tData += "</testcase>";
									CallableStatement cstmt = conn
									.prepareCall("begin ENTG_UTILS.save_testcase_data(?,?,?,?); end;");
									cstmt.setInt(1, testcaseId);
									cstmt.setInt(2, runId);
									cstmt.setInt(3, rs1.getInt(1));
									cstmt.setString(4, tData);
									cstmt.execute();
									cstmt.close();
									d("DX7");
								}
							}

						} else { // Data Selection

							String[] cols = new String[100];
							int numcols = 0;
							Statement stmt4 = conn.createStatement();
							ResultSet rs4 = stmt4
									.executeQuery("select query_id from entg_data_selections "
											+ "where data_selection_id ="
											+ selectionId);
							rs4.next();
							queryId = rs4.getInt(1);
							stmt4.close();

							stmt4 = conn.createStatement();
							rs4 = stmt4
									.executeQuery("select column_name,column_seq from entg_test_data_query_columns "
											+ "where query_id ="
											+ queryId
											+ " order by column_seq");
							while (rs4.next()) {
								cols[numcols++] = rs4.getString(1);

							}
							stmt4.close();

							String[] outcols = new String[100];
							int numoutcols = 0;
							stmt4 = conn.createStatement();
							rs4 = stmt4
									.executeQuery("SELECT DISTINCT UPPER(parameter_name) parameter_name FROM ENTG_TEST_FUNCTION_PARAMS fp, ENTG_TEST_SCENARIO_FUNCTIONS sf WHERE fp.function_id = sf.function_id AND scenario_id = "
											+ scenId
											+ " minus select column_name from entg_test_data_query_columns where query_id ="
											+ queryId);
							while (rs4.next()) {
								outcols[numoutcols++] = rs4.getString(1);

							}
							stmt4.close();

							Statement stmt6 = conn.createStatement();
							ResultSet rs6 = stmt6
									.executeQuery("select * from entg_data_selection_rows where data_selection_id = "
											+ selectionId);
							int numRows = 0;
							while (rs6.next()) {

								stmt4 = conn.createStatement();
								rs4 = stmt4
										.executeQuery("select entg_testcases_s.nextval from dual");
								rs4.next();
								int testcaseId = rs4.getInt(1);
								stmt4.close();
								String tData = "<TestCase id=\"" + testcaseId
										+ "\">";

								for (int i = 0; i < numcols; i++) {
									tData += "<DATA>" + cols[i] + "="
											+ rs6.getString(i + 3) + "</DATA>";
								}
								for (int i = 0; i < numoutcols; i++) {
									if (outcols[i] != null
											&& !outcols[i].equals(""))

										tData += "<data>" + outcols[i]
												+ "=null</data>";
								}

								tData += "</TestCase>";

								CallableStatement cstmt = conn
										.prepareCall("begin ENTG_UTILS.save_testcase_data(?,?,?,?); end;");
								cstmt.setInt(1, testcaseId);
								cstmt.setInt(2, runId);
								cstmt.setInt(3, rs1.getInt(1));
								cstmt.setString(4, tData);
								cstmt.execute();
								cstmt.close();
								numRows++;
							}
							stmt6.close();
						}

					}
					stmt1.close();

					d("D20. Updating TR");
					PreparedStatement pstmt = conn
							.prepareStatement("UPDATE ENTG_TEST_RUNS set run_status = 'GENERATED_TESTCASES' where run_id = ?");
					pstmt.setInt(1, runId);

					pstmt.execute();
					pstmt = conn
							.prepareStatement("UPDATE SI_100070_T SET run_status = 'GENERATED_TESTCASES' WHERE process_instance_id = ( SELECT process_instance_id FROM entg_test_runs WHERE run_id = ?)");
					pstmt.setInt(1, runId);

					pstmt.execute();
					d("D21. Done");
				}
			}
			stmt.close();
 
			d("D22. Exiting");
		} catch (Exception e) {

			if (debugFlag) e.printStackTrace(out);
			d("Exception: "+e.getMessage());
			throw new Exception("Error in genTC: "+e.getMessage());
		}

	}

	static Connection metricDBConn;

	public static Connection getMetricDBConn() {
		return metricDBConn;
	}

	public static void closeMetricDBConn() throws Exception {
		metricDBConn.close();
	}


	public static void makeMetricDBConn() throws Exception {

		entg.test.TCAgentMain.loadProps();
		String encryptedPassword = entg.test.TCAgentMain.TCProperties
				.getProperty("METRIC_DB_PW");
		PasswordEncryption encrypter = new PasswordEncryption();
		String password = encrypter.decrypt(encryptedPassword);

		String userName = entg.test.TCAgentMain.TCProperties
				.getProperty("METRIC_DB_USERNAME");

		String dbUrl = entg.test.TCAgentMain.TCProperties.getProperty("METRIC_DB_URL");

		DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		metricDBConn = DriverManager.getConnection(dbUrl, userName, password);

	}

}