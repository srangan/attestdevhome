package entg.util;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import entg.test.TCAgentMain;
import entg.test.TestRunInfo;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;


import com.sforce.soap.ApplicationMethods.SoapConnection;
import com.sforce.soap.ApplicationMethods.Connector;
import com.sforce.soap.ApplicationMethods.FunctionUpdateClass;
import com.sforce.soap.ApplicationMethods.CreateTestCaseClass;
import com.sforce.soap.ApplicationMethods.UpdateTestRunClass;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.soap.enterprise.*; 

public class TCSFDCTestcaseGenerator {

	/**
	 * @param args
	 */
	

	static boolean DEBUG = false;
	  static String USERNAME = null;
	  static String PASSWORD = null;

	  static SoapConnection MyWebserviceWSconnection;
	  static EnterpriseConnection enterpriseConnection;
	

	private java.util.Vector dataVect;
	

	public TCSFDCTestcaseGenerator(){
		dataVect = new java.util.Vector();
		
	}
	
	public void addData(String data) {
		if (DEBUG) {
		System.out.println("In addData "+data);
		}
		dataVect.add(data);
		
	}
	
	public java.util.Hashtable getDataMap () {
		java.util.Hashtable dataMap = new java.util.Hashtable();
		
	    for (int c=0;c<dataVect.size();c++) {
	    	
	        
	    	if (DEBUG) {
	        System.out.println("In TCGen: " + (String)dataVect.elementAt(c));
	    	}
	        String str = (String)dataVect.elementAt(c);
	        StringTokenizer st= new StringTokenizer(str,"=");
	        String key = st.nextToken();
	        
	        
	        
	        String val = str.substring(key.length()+1);
	        /*if (st.hasMoreTokens()) 
	        	val = st.nextToken();*/
	        
	        
	        //String eqIdx = str.indexOf("=");
	    	if (DEBUG) {
	        System.out.println("In TCGen: " + key + "="+val);
	    	}
	        dataMap.put(key, val);
	    }
	        
		return dataMap;
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
		
		
		TCAgentMain.loadProps();

        USERNAME = TCAgentMain.TCAgentproperties("SFDC_USERNAME");//"attestdev@45demo.com";
		PASSWORD = TCAgentMain.TCAgentproperties("SFDC_PASSWORD");//"attest@123Othi3MSL6zS4iUBRVdYPYsxD7";

		System.out.println("Entering Generate Testcases.");
		
		String fileName = args[0];
		if (!fileName.endsWith(".qxml")) {
			System.out.println("Usage: java entg.util.TCSFDCTestcaseGenerator <file>.qxml");
		}

		TCSFDCTestcaseGenerator tc = initTCGen(args[0]);
		
		java.util.Hashtable data = tc.getDataMap();
		
		DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		Connection conn = DriverManager.getConnection((String) data.get("databaseurl"), (String) data.get("username"), (String) data.get("password"));


		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery((String) data.get("query"));
		

		String tcData = "";
		ResultSetMetaData rsm = rs.getMetaData();
        int colcount = rsm.getColumnCount();
        
        String[] parameterNames = new String[colcount];
        String[] parameterValues = new String[colcount];
        
        while (rs.next()) {
        	for (int i=0;i<colcount;i++) {
        		parameterNames[i]=rsm.getColumnName(i+1);
        		parameterValues[i]=rs.getString(i+1);        		
        	}
        	
        sfdcCreateTestcase(tc.getDsId(),tc.getGroupNumber(),tc.getScenId(),tc.getScenDsId(),tc.getRunId(),parameterNames,parameterValues);
        	
        }
        
    	if (DEBUG) {
        System.out.println(tcData);
    	}
    	System.out.println("DONE");
        
        stmt.close();
		
		// create testcase in sfdc. 
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	String runId;

	String dsId;
	String groupNumber;
	String scenName;
	String scenDsId;
	String scenId;
	

	public static TCSFDCTestcaseGenerator initTCGen(String qxmlFile) throws IOException,SAXException{
		

		
		Digester digester = new Digester();
		digester.setValidating(false);

		// instantiate TCSFDCTestcaseGenerator class
		digester.addObjectCreate("TestRun", TCSFDCTestcaseGenerator.class);
		digester.addSetProperties("TestRun","id","runId");
		digester.addSetProperties("TestRun/DataSet","id","dsId");
		
		digester.addSetProperties("TestRun/DataSet/scenario","groupNumber","groupNumber");
		digester.addSetProperties("TestRun/DataSet/scenario","name","scenName");
		digester.addSetProperties("TestRun/DataSet/scenario","id","scenId");
		digester.addSetProperties("TestRun/DataSet/scenario","ScenarioToDataSetId","scenDsId");
		
		digester.addCallMethod("TestRun/DataSet/scenario/data",
				"addData", 0);
		
		TCSFDCTestcaseGenerator t = (TCSFDCTestcaseGenerator) digester.parse( new File(qxmlFile) );
		return t;
	}	
	
	

	  /**  
		 * @param args
		 */
		public static void sfdcCreateTestcase(String dataSetId,String groupNumber, String scenarioId, String scenarioToDataSetId, 
			      String testRunId, String[] parameterNames, String[] parameterValues ) {
			// TODO Auto-generated method stub

			if (DEBUG) {
			System.out.println("dataSetId "+dataSetId);
			System.out.println("groupNumber "+groupNumber);
			System.out.println("testRunId "+testRunId);
			System.out.println("scenarioId "+scenarioId);
			System.out.println("scenarioToDataSetId "+scenarioToDataSetId);
			System.out.println("parameterNames "+parameterNames.length);
			for (int i=0;i<parameterNames.length;i++) {
				System.out.println("parameterNames["+i+"]="+parameterNames[i]);
				System.out.println("parameterValues["+i+"]="+parameterValues[i]);
			}
			}
			String password = "";
			if (PASSWORD != null && !PASSWORD.equals("")) {
				   
				  entg.util.PasswordEncryption pwp = new entg.util.PasswordEncryption();
				  password = pwp.decrypt(PASSWORD);
			    }
				
		    ConnectorConfig config = new ConnectorConfig();  
		    config.setUsername(USERNAME);
		    config.setPassword(password);

		    

		    try {

		      //create a connection to Enterprise API -- authentication occurs
		      enterpriseConnection = com.sforce.soap.enterprise.Connector.newConnection(config);    
		      // display some current settings
		  	if (DEBUG) {
		      System.out.println("Auth EndPoint: "+config.getAuthEndpoint());
		      System.out.println("Service EndPoint: "+config.getServiceEndpoint());
		      System.out.println("Username: "+config.getUsername());
		      System.out.println("SessionId: "+config.getSessionId());
		  	}

		      //create new connection to exportData webservice -- no authentication information is included
		      MyWebserviceWSconnection = Connector.newConnection("","");
		      //include session Id (obtained from enterprise api) in exportData webservice
		      MyWebserviceWSconnection.setSessionHeader(config.getSessionId());
		        
		      
		      CreateTestCaseClass reqClass = new CreateTestCaseClass();
		      
		      
		      reqClass.setDataSetId(dataSetId);
		      
		      reqClass.setGroupNumber(((Float)Float.parseFloat(groupNumber)).intValue());
		      reqClass.setScenarioId(scenarioId);
		      reqClass.setScenarioToDataSetId(scenarioToDataSetId);
		      reqClass.setTestRunId(testRunId);
		      reqClass.setParameterNames(parameterNames);
		      reqClass.setParameterValues(parameterValues);
		      
		      
		      
		       
		      String updStatus  = MyWebserviceWSconnection.CreateTestCase(reqClass);

		      //String result = MyWebserviceWSconnection.receiveData("test");
		      //System.out.println("Result: "+result);

		      UpdateTestRunClass u = new UpdateTestRunClass(); 
		      u.setTestRunId(testRunId);
		      u.setStatus("GENERATED");
		      
		      updStatus  = MyWebserviceWSconnection.UpdateTestRun(u);
		      
		    } catch (ConnectionException e1) {
		        e1.printStackTrace();
		    }  
		}

	public String getRunId() {
		return runId;
	}

	public void setRunId(String runId) {
		this.runId = runId;
	}

	public String getDsId() {
		return dsId;
	}

	public void setDsId(String dsId) {
		this.dsId = dsId;
	}

	public String getGroupNumber() {
		return groupNumber;
	}

	public void setGroupNumber(String groupNumber) {
		this.groupNumber = groupNumber;
	}

	public String getScenName() {
		return scenName;
	}

	public void setScenName(String scenName) {
		this.scenName = scenName;
	}

	public String getScenDsId() {
		return scenDsId;
	}

	public void setScenDsId(String scenDsId) {
		this.scenDsId = scenDsId;
	}

	public String getScenId() {
		return scenId;
	}

	public void setScenId(String scenId) {
		this.scenId = scenId;
	}	

}
