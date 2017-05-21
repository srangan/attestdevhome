package entg.test;

import java.util.StringTokenizer;

public class AppSignInfo extends FunctionInfo{
private String appSignOnScript;
private java.util.Vector dataVect;

public AppSignInfo(){
	super();
	super.setFunctionName("Application Login");
	dataVect = new java.util.Vector();
	super.isSignon=true;
	
}
public void setAppSignOnScript(String newScript){
	appSignOnScript=newScript;
	super.setFunctionName(newScript);
	
	super.setFunctionScript(newScript);
	
}

public void addData(String data) {
	System.out.println("In APPSIGN" + data);
	dataVect.addElement(data);


	if (TCAgentMain.sfdcConsole()) {
		
		StringTokenizer st= new StringTokenizer(data,"=");
        String key = st.nextToken();
        String val = data.substring(key.length()+1);
        
        if (key.equalsIgnoreCase("instance_sfdc_id")) {
        	dataVect.addElement("password="+entg.test.UpdateSFDCStatus.getPasswordFromSfdc(val, key));
        }
        if (key.equalsIgnoreCase("database_sfdc_id")) {
        	dataVect.addElement("database_password="+entg.test.UpdateSFDCStatus.getPasswordFromSfdc(val, key));
        }
		
	}
    
    
}


public String getAppSignOnScript(){
	return appSignOnScript;
}
   



public java.util.Hashtable getSignonDataMap () {
	java.util.Hashtable dataMap = new java.util.Hashtable();
	
    for (int c=0;c<dataVect.size();c++) {
    	
        
        System.out.println("In APPSIGN1: " + (String)dataVect.elementAt(c));
        String str = (String)dataVect.elementAt(c);
        StringTokenizer st= new StringTokenizer(str,"=");
        String key = st.nextToken();
        
        
        
        String val = str.substring(key.length()+1);
        
        /*if (st.hasMoreTokens()) 
        	val = st.nextToken();*/
        
        
        //String eqIdx = str.indexOf("=");
        System.out.println("In APPSIGN1: " + key + "="+val);
        dataMap.put(key, val);
        
        

        
    }
        
	return dataMap;
	
}
public java.util.Vector getDataVect() {
	return dataVect;
}
public void setDataVect(java.util.Vector dataVect) {
	this.dataVect = dataVect;
}


}
