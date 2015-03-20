package com.offbytwo.jenkins.model;

import com.offbytwo.jenkins.model.BaseModel;

public class BuildWithTestReport extends BaseModel {

	//	double duration;

	int failCount;
	int passCount;
	int totalCount;

	//	int skipCount;

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

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

}
