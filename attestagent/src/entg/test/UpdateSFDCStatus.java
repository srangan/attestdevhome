package entg.test;

import com.sforce.soap.ApplicationMethods.SoapConnection;
import com.sforce.soap.ApplicationMethods.Connector;
import com.sforce.soap.ApplicationMethods.FunctionUpdateClass;
import com.sforce.soap.ApplicationMethods.CreateTestCaseClass;

import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.soap.enterprise.*; 

public class UpdateSFDCStatus {
	
	       

             
	  static final String USERNAME = TCAgentMain.TCAgentproperties("SFDC_USERNAME");//"attestdev@45demo.com";
	  static final String PASSWORD = TCAgentMain.TCAgentproperties("SFDC_PASSWORD");//"attest@123Othi3MSL6zS4iUBRVdYPYsxD7";
  
	       	
	  static SoapConnection MyWebserviceWSconnection;
	  static EnterpriseConnection enterpriseConnection;
	  
	  /**  
		 * @param args
		 */    
		public static void sfdcCreateTestcase(String dataSetId,String groupNumber, String scenarioId, String scenarioToDataSetId, 
			      String testRunId, String[] parameterNames, String[] parameterValues ) {
			// TODO Auto-generated method stub

			 
			String password = "";
			if (PASSWORD != null && !PASSWORD.equals("")) {
				
				System.out.println("PASSWORD =  "+PASSWORD);
				   
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
		      System.out.println("Auth EndPoint: "+config.getAuthEndpoint());
		      System.out.println("Service EndPoint: "+config.getServiceEndpoint());
		      System.out.println("Username: "+config.getUsername());
		      System.out.println("SessionId: "+config.getSessionId());


		      //create new connection to exportData webservice -- no authentication information is included
		      MyWebserviceWSconnection = Connector.newConnection("","");
		      //include session Id (obtained from enterprise api) in exportData webservice
		      MyWebserviceWSconnection.setSessionHeader(config.getSessionId());
		        
		      System.out.println("In SFDC Set Status");
		      
		      CreateTestCaseClass reqClass = new CreateTestCaseClass();
		      
		      
		      reqClass.setDataSetId(dataSetId);
		      reqClass.setGroupNumber(new Integer(Integer.parseInt(groupNumber)));
		      reqClass.setScenarioId(scenarioId);
		      reqClass.setScenarioToDataSetId(scenarioToDataSetId);
		      reqClass.setTestRunId(testRunId);
		      reqClass.setParameterNames(parameterNames);
		      reqClass.setParameterValues(parameterValues);
		      
		       
		      String updStatus  = MyWebserviceWSconnection.CreateTestCase(reqClass);

		      //String result = MyWebserviceWSconnection.receiveData("test");
		      //System.out.println("Result: "+result);

		      

		      
		    } catch (ConnectionException e1) {
		        e1.printStackTrace();
		    }  
		}
		
		
	public static String getPasswordFromSfdc(String sfdcId, String type) {
		String passReturn = "";
		String password = "";
		if (PASSWORD != null && !PASSWORD.equals("")) {
			System.out.println("PASSWORD =  "+PASSWORD);
			   
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
		      System.out.println("Auth EndPoint: "+config.getAuthEndpoint());
		      System.out.println("Service EndPoint: "+config.getServiceEndpoint());
		      System.out.println("Username: "+config.getUsername());
		      System.out.println("SessionId: "+config.getSessionId());


		      //create new connection to exportData webservice -- no authentication information is included
		      MyWebserviceWSconnection = Connector.newConnection("","");
		      //include session Id (obtained from enterprise api) in exportData webservice
		      MyWebserviceWSconnection.setSessionHeader(config.getSessionId());
		        

		      passReturn  = MyWebserviceWSconnection.getPassword(sfdcId, type);
	    } catch (Exception e) {
	    	
	    	e.printStackTrace();
	    	passReturn = "ERROR";
	    }
	    
	    System.out.println("sfdc return pwd: "+passReturn);
	    
		return passReturn;
	}
		
	/**  
	 * @param args
	 */
	public static void sfdcUpdateStatus(String functionId, String functionSeq, String testcaseId, String runId, 
		    String status, String logFile,String startTime, String endTime) {
		// TODO Auto-generated method stub

		String password = "";
		if (PASSWORD != null && !PASSWORD.equals("")) {
			   
			  entg.util.PasswordEncryption pwp = new entg.util.PasswordEncryption();
			  password = pwp.decrypt(PASSWORD);
			         
		    }
			
	    ConnectorConfig config = new ConnectorConfig();  
	    config.setUsername(USERNAME);
	    config.setPassword(password);

	    System.out.println("PASS="+password);  

	    try {

	      //create a connection to Enterprise API -- authentication occurs
	      enterpriseConnection = com.sforce.soap.enterprise.Connector.newConnection(config);    
	      // display some current settings
	      System.out.println("Auth EndPoint: "+config.getAuthEndpoint());
	      System.out.println("Service EndPoint: "+config.getServiceEndpoint());
	      System.out.println("Username: "+config.getUsername());
	      System.out.println("SessionId: "+config.getSessionId());


	      //create new connection to exportData webservice -- no authentication information is included
	      MyWebserviceWSconnection = Connector.newConnection("","");
	      //include session Id (obtained from enterprise api) in exportData webservice
	      MyWebserviceWSconnection.setSessionHeader(config.getSessionId());
	        
	      System.out.println("In SFDC Set Status");
	      System.out.println("functionId ="+functionId);
	      System.out.println("functionSeq ="+functionSeq);
	      System.out.println("logFile ="+logFile);
	      System.out.println("status ="+status);
	      System.out.println("startTime ="+startTime);
	      System.out.println("endTime ="+endTime);
	      FunctionUpdateClass reqClass = new FunctionUpdateClass();
	      reqClass.setFunctionid(functionId);
	      reqClass.setFuncSeq(functionSeq);
	      reqClass.setLogFile(logFile);
	      reqClass.setStatus(status);
	      reqClass.setStime("1000");
	      reqClass.setEtime("1000");
	      reqClass.setLogFileURL(logFile);
	       
	      String updStatus  = MyWebserviceWSconnection.UpdateTestRunFunction(reqClass);

	      //String result = MyWebserviceWSconnection.receiveData("test");
	      //System.out.println("Result: "+result);

	      
	      

	    } catch (ConnectionException e1) {
	        e1.printStackTrace();  
	    }  
	}
	
	
}