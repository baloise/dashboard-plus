package com.offbytwo.jenkins.model;

public class TestReport extends BaseModel {

	//	double duration;

	int failCount;
	int passCount;
	int totalCount;
	int skipCount;

	TestReportChildReport[] childReports;
	TestReportSuite[] suites;

	public void setMissingValues() {
		// Because Jenkins test reports dont have the same fields for
		// - job: failCount/skipCount/totalCount (passCount missing)
		// - job's maven module: failCount/passCount/skipCount (totalCount missing)

		// Note that skipCount is considered here while deriving totalCount or passCount that needs to be set
		if (totalCount == 0) {
			totalCount = failCount + passCount + skipCount;
		} else {
			passCount = totalCount - failCount - skipCount;
		}
	}

	public double calcSuccessRatio(boolean inclSkippedTests) {
		return ((double) passCount)
				/ ((double) getTotalCount(inclSkippedTests));
	}

	public int getFailCount() {
		return failCount;
	}

	public void setFailCount(int failCount) {
		this.failCount = failCount;
	}

	public int getPassCount() {
		return passCount;
	}

	public void setPassCount(int passCount) {
		this.passCount = passCount;
	}

	public int getTotalCount(boolean inclSkippedTests) {
		if (inclSkippedTests)
			return totalCount;
		else
			return totalCount - skipCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getSkipCount() {
		return skipCount;
	}

	public void setSkipCount(int skipCount) {
		this.skipCount = skipCount;
	}

	public TestReportChildReport[] getChildReports() {
		return childReports;
	}

	public void setChildReports(TestReportChildReport[] childReports) {
		this.childReports = childReports;
	}

	public TestReportSuite[] getSuites() {
		return suites;
	}

	public void setSuites(TestReportSuite[] suites) {
		this.suites = suites;
	}

}
