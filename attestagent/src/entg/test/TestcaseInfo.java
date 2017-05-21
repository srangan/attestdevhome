package entg.test;

import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.*;
import java.util.zip.*;
import java.util.*;

public class TestcaseInfo {
	protected String testcaseId;

	private String testcaseName;

	private String logDirectory;

	private int scenarioId;

	private Object testcaseContext;
	
	private String Status = "In Progress";

	private int lastpassfunc=0;
	
	private String overrideDataFile;

	public Vector functionStatus;

	public String functionRunIds; 
	
	private java.util.Vector dataVect;
	
	public TestcaseInfo() {
		functionStatus = new Vector();
		dataVect = new Vector();
	}

	public void addFunctionStatus(String stat) {

		functionStatus.addElement(stat);
	}

	public String getFunctionStatus(int index) {
		return (String) functionStatus.elementAt(index);
	}

	public void setTestcaseContext(Object tcContext) {
		this.testcaseContext = tcContext;
	}
	
	public Object getTestcaseContext() {
		return testcaseContext;
	}
	
	public void setFunctionStatus(int index, String status) {
		functionStatus.setElementAt(status, index);
		if (status.equals("FAIL"))
			this.Status = "FAIL";
		else {
			if ((index + 1) == functionStatus.size()) {
				this.Status = "PASS";
			} else {
				this.Status = "In Progress";
			}
		}
	}

	public String getOverrideDataFile() {
		return overrideDataFile;
	}
 
	public void setOverridedatafile(String OverrideDataFile) {
		System.out.println("In setOverride "+OverrideDataFile);
		overrideDataFile = OverrideDataFile;
	}
	
	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	public void setTestcaseId(String string) {
		testcaseId = string;
	}

	public String getTestcaseId() {
		return testcaseId;
	}

	public void setScenarioId(int newScenarioId) {
		scenarioId = newScenarioId;
	}

	public int getScenarioId() {
		return scenarioId;
	}

	public void setTestcaseName(String newtestcaseName) {

		System.out.println("******+++ In set newtestcaseName "+newtestcaseName);
		testcaseName = newtestcaseName;
		if (testcaseName.startsWith("TEST")) 
		  setTestcaseId(testcaseName.substring(4));
		else 
		  setTestcaseId(testcaseName);		
	}

	public String getTestcaseName() {
		return testcaseName;
	}

	public void setLogDirectory(String newlogDirectory) {
		logDirectory = newlogDirectory;
	}

	public String getLogDirectory() {
		return logDirectory;
	}

	public void setLastpassfunc(int val) {
		
		System.out.println("******+++ In set lastpassfunc "+lastpassfunc);
		lastpassfunc = val;

	}
	
	public int getLastpassfunc() {
		return this.lastpassfunc;
	}

	public void addData(String data) {
		
		dataVect.addElement(
			org.apache.commons.lang.StringEscapeUtils.unescapeXml(data));
	}

	public java.util.Vector getDataVect() {
		return dataVect;
	}

	public void setDataVect(java.util.Vector dataVect) {
		this.dataVect = dataVect;
	}
	

	public java.util.Hashtable getDataMap () {
		java.util.Hashtable dataMap = new java.util.Hashtable();
		
	    for (int c=0;c<dataVect.size();c++) {

	        String str = (String) dataVect.elementAt(c);
			StringTokenizer st = new StringTokenizer(str, "=");

			String key = st.nextToken();
			String val = "";

	  		try {
					val = str.substring((key.length() + 1));
			} catch (Exception ex) {
			}	        
	        
	        dataMap.put(key, val);
	    }
	        
		return dataMap;
		
	}

	String[] functionRunIdList;
	
	public String getFunctionRunIds() {
		return functionRunIds;
	}

	public void setFunctionRunIds(String functionRunIds) {
		System.out.println("In setFunctionRunIds "+functionRunIds);
		System.out.println("In setFunctionRunIds "+functionRunIds.replaceAll("\"", ""));
		this.functionRunIds = functionRunIds.replaceAll("\"", "");
			
		functionRunIdList = this.functionRunIds.split(",");
	}

	public String getFunctionRunId(int seqNo) {
		System.out.println("In getFunctionRunId .. " + seqNo);
		System.out.println("functionRunIds.length = " +
				functionRunIdList.length);
		for (int i=0;i<functionRunIdList.length;i++) {
			System.out.println("functionRunIds["+i+"]="+functionRunIdList[i]);
		}
		if (functionRunIdList!=null && functionRunIdList.length>=seqNo) 
			return functionRunIdList[seqNo-1];
		else 
			return "";
	}	
 }
