package com.offbytwo.jenkins.model;

public class TestReportSuite extends BaseModel {

	TestReportSuiteCase[] cases;

	public TestReportSuite() {
	}

	public TestReportSuiteCase[] getCases() {
		return cases;
	}

	public void setCases(TestReportSuiteCase[] cases) {
		this.cases = cases;
	}

}
