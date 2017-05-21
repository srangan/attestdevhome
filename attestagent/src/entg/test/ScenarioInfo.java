
package entg.test;
import java.util.*;


public class ScenarioInfo {
	private String scenarioName;

	private int scenarioId;

	public String scenarioTextFile;


	public void initializeTestcaseStatus (){
		System.out.println("+++ In Init testcase Status");
		
		for (int k=0;k<testcases.size();k++){
			TestcaseInfo tst1 = (TestcaseInfo) testcases.elementAt(k);
			
			System.out.println("Testcase ID: " +tst1.testcaseId);
			System.out.println("Lastpassfunc (before) "+tst1.getLastpassfunc());
			int lastIsSignon = -1;
			for (int j=0;j<functions.size();j++){
				FunctionInfo f = (FunctionInfo) functions.elementAt(j);
				System.out.println("Function "+j+" : "+f.getFunctionName());
				if (j>(tst1.getLastpassfunc()-1))
				  tst1.addFunctionStatus("PENDING"); 
				else if (j==(tst1.getLastpassfunc()-1)) {
				  
				  if (f.isSignon) {
					  tst1.addFunctionStatus("PENDING");
					  tst1.setLastpassfunc((tst1.getLastpassfunc()-1));
					  System.out.println("Function "+j+" : is a signon");
					  if (lastIsSignon !=-1)
							tst1.setFunctionStatus(lastIsSignon, "PASS"	);
					  lastIsSignon = j; 
				  } else {
					  tst1.addFunctionStatus("PASS");  
				  }
				}   else {
					if (f.isSignon) {
						System.out.println("Function "+j+" : is a signon");
						if (lastIsSignon !=-1)
							tst1.setFunctionStatus(lastIsSignon, "PASS"	);
						lastIsSignon = j;  
						tst1.addFunctionStatus("PENDING");
					} else {
					tst1.addFunctionStatus("PASS");
					}
				}
				System.out.println("Function "+j+" : "+f.getFunctionName());
			}
			System.out.println("Lastpassfunc (after) "+tst1.getLastpassfunc());	
			
		}
		
	}
	// Loop through all testcases
	   // Loop through all functions
	      // testcase.addFunctionStatus();
	Vector functions;

	Vector testcases;

	// Function Array (AddFunction)
	public void addTestcase(TestcaseInfo t) {
		testcases.addElement(t);
	}

	public ScenarioInfo() {
		functions = new Vector();
		testcases = new Vector();
	}

	public void addFunction(FunctionInfo f) {
		functions.addElement(f);
	}
	public void setScenarioName(String newscenario) {
		scenarioName = newscenario;
	
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioTextFile(String newtextfile) {
		scenarioTextFile = newtextfile;
	}

	public String getScenarioTextFile() {
		return scenarioTextFile;
	}

	public void setScenarioId(int newscenarioId) {
		scenarioId = newscenarioId;
	}

	public int getScenarioId() {
		return scenarioId;
	}

	public Vector getTestCases() {
		return testcases;
	}

	public Vector getFunctions() {
		return functions;
	}

}  //  @jve:decl-index=0:visual-constraint="264,159"
