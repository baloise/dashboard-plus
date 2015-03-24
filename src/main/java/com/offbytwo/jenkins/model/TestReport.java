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
		// Because all Jenkins test reports do not provide always with the same data, sometimes failCount+passCount, sometimes failCount+totalCount
		if (totalCount == 0) {
			totalCount = failCount + passCount;
		} else {
			passCount = totalCount - failCount;
		}
	}

	public double calcSuccessRatio(boolean inclSkippedTests) {
		if (inclSkippedTests)
			return ((double) passCount) / ((double) (totalCount + skipCount));
		else
			return ((double) passCount) / ((double) totalCount);
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
			return totalCount + skipCount;
		else
			return totalCount;
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
