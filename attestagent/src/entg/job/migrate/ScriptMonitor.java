package entg.job.migrate;
import java.io.*;
import java.sql.PreparedStatement;
import java.util.*;
public class ScriptMonitor extends Thread  {

		String commonFileName;
		String currentFunctionStatus;
		PrintWriter out;
		FunctionInfo functionInfo;
		
		 public ScriptMonitor( String CommonFileName, PrintWriter out,FunctionInfo f) {
			 this.commonFileName= CommonFileName;
			 currentFunctionStatus="NEW";
			 this.out=out;
			 functionInfo = f;
		 } 
		 
		 public void run() {
			try {
				for (int i = 0; i < 2; i++) {
					out.println("In Script Mon");
					boolean functionComplete = false;
					int currentFunction = i +1;
					out.println("Looking for status of "+currentFunction);
					out.print(".");
					while (!functionComplete) {
						out.print(".");	
						FileInputStream fs=null;
						boolean fileOpen = false;
						for (int k=0;k<5&&!fileOpen;k++) {
	 						try {
								  
								  fs = new FileInputStream(new File(commonFileName));
								  fileOpen=true;
								} catch (FileNotFoundException ex) {
									Thread.sleep(2000);								
								}
							
						}
						if (!fileOpen) {
							out.println("********** Could not find comm file");
						}
							
						Properties commfile = new Properties();
						commfile.load(fs);
						//fs.close();
						currentFunctionStatus = commfile.getProperty("TEST"
								+ currentFunction + ".status");

						String currentFunctionLog = commfile.getProperty("TEST" + 
	 							currentFunction
								+ ".logdirectory");
						if (currentFunctionStatus != null 
								  && currentFunctionLog != null) {
							out.println("Found status of "+currentFunction);
							out.println("Status is:"+currentFunctionStatus);
							
							out.println("Log "+currentFunctionLog);


							if (currentFunctionStatus.equals("PASS")) {
								if (currentFunction==1) 
									out.println("Logon Successful");
								else
									out.println("Script Successful");
							} else {
								if (currentFunction==1) 
									out.println("Logon Failed");
								else
									out.println("Script Failed");
							}
								
							
							functionInfo.uploadLogDir(currentFunctionLog, currentFunction, out);
							
							functionComplete =  true;
							
					
						} else {
							Thread.sleep(1000);
						}
					}
					if (currentFunctionStatus.equals("FAIL"))
						break;
				}
				functionInfo.setScriptStatus(currentFunctionStatus);
			} catch (Exception ex) {
				try { 
				  functionInfo.setScriptStatus(currentFunctionStatus);
				} catch (Exception e) {
					
				}
				ex.printStackTrace();
			}
			

		}
}

