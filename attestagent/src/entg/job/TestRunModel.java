package entg.job;

import java.util.List;
import java.util.ArrayList;

public class TestRunModel {
	
	private Scenario Scenario;
	private List<TestCaseWithParameters> TestCaseWithParameters = new ArrayList<TestCaseWithParameters>();

	public Scenario getScenario() {
		return Scenario;
	}

	public void setScenario(Scenario scenario) {
		Scenario = scenario;
	}

	public List<TestCaseWithParameters> getTestCaseWithParameters() {
		return TestCaseWithParameters;
	}

	public void setTestCaseWithParameters(
			List<TestCaseWithParameters> testCaseWithParameters) {
		TestCaseWithParameters = testCaseWithParameters;
	}
	


}
