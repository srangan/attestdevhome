<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.util.*" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>Validate and </title>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
		<link rel="stylesheet" href="/gui_styles.css" type="text/css">
	</head>
<body bgcolor="#EFEFEF" leftmargin="0" topmargin="0">
<%
String runId = request.getParameter("RUN_ID");
String runStepId = request.getParameter("RUN_STEP_ID");
String userId=request.getParameter("USER_ID");
String stepType=request.getParameter("STEP_TYPE");


String dbUserName = pageContext.getServletContext().getInitParameter("dbuser");
String pwd = pageContext.getServletContext().getInitParameter("dbpwd"); 
String dbUrl = pageContext.getServletContext().getInitParameter("dburl"); 

DriverManager.registerDriver(new oracle.jdbc.OracleDriver()); 
Connection conn = DriverManager.getConnection(dbUrl, dbUserName, pwd); 

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
  problems[numProblems++] = "Migration Step is already completed.";
} else if (rsStatus.equalsIgnoreCase("error")) {
   CallableStatement cstmt1 = conn.prepareCall("begin ENTG_SETUP_UTILS.reset_statuses(?); end;");
   cstmt1.setString(1, runStepId);
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
conn.close();
if (allowSubmit) {
  if (stepType.equals ("Migrate")){


%>
<table border=0 cellpadding=5 cellspacing=5><tr><td>
<font color="green"><B>Validation Successful</B></font>
<P>
Please choose an option:<BR>
 &nbsp;&nbsp;<span class="ms_tab_button">
<a tabindex="1001" href="#" onClick="javascript:launchClose();" class="mstab">&nbsp;Launch Locally&nbsp;</a></span><BR>
 &nbsp;&nbsp;<span class="ms_tab_button">
<a tabindex="1001" href="/servlets/submitSetupRun.jsp?RUN_ID=<%=runId%>&RUN_STEP_ID=<%=runStepId%>&USER_ID=<%= userId %>" class="mstab">&nbsp;
Submit to Remote Manager&nbsp;</a></span><BR>
 &nbsp;&nbsp;<span class="ms_tab_button">
<a tabindex="1001" href="#" onClick="javascript:window.close()" class="mstab">&nbsp;
Close&nbsp;</a></span>
</tr></td></table>
<%
} else if (stepType .equals ("Extract")){
   
%>
<table border=0 cellpadding=5 cellspacing=5><tr><td>
<font color="green"><B>Validation Successful</B></font>
<P>
Please choose an option:<BR>
 &nbsp;&nbsp;<span class="ms_tab_button">
<a tabindex="1001" href="/servlets/saveJob.jsp?&PROGRAM_ID=1&MANAGER_ID=1&PARAM1=<%=runId%>&PARAM2=<%=runStepId%>&PARAM3=<%= userId %>" class="mstab">&nbsp;
Submit to Remote Manager&nbsp;</a></span><BR>
 &nbsp;&nbsp;<span class="ms_tab_button">
<a tabindex="1001" href="#" onClick="javascript:window.close()" class="mstab">&nbsp;
Close&nbsp;</a></span>
</tr></td></table>
<%
} 
} else {
%>
<font color="red">The following errors exist that need to be corrected before the run can be submitted:<font><BR><UL>
<%
for (int i=0;i<numProblems;i++) {
%>
<LI><%= problems[i] %>
<%
}
%>
</UL>
<BR><center><input type="button" value="OK" onClick="javascript:window.close()"></center>
<%

}
conn.close();
%>

<script language ="Javascript">
function launchClose(){
window.opener.location="/servlets/setupRun.jsp?RUN_ID=<%=runId%>&RUN_STEP_ID=<%=runStepId%>&USER_ID=<%=userId%>"; 
//closePop.focus();
window.close();

}
</script>

</body>
</html>
