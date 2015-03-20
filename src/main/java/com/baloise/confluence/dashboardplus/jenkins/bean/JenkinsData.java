package com.baloise.confluence.dashboardplus.jenkins.bean;

import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.BuildWithTestReport;
import com.offbytwo.jenkins.model.JobWithDetails;

public class JenkinsData {

	private JobWithDetails jobDetails;

	private BuildWithDetails lastCompletedBuildDetails;

	private BuildWithTestReport lastCompletedBuildTestReport;

	public JobWithDetails getJobDetails() {
		return jobDetails;
	}

	public void setJobDetails(JobWithDetails jobDetails) {
		this.jobDetails = jobDetails;
	}

	public BuildWithDetails getLastCompletedBuildDetails() {
		return lastCompletedBuildDetails;
	}

	public void setLastCompletedBuildDetails(
			BuildWithDetails lastCompletedBuildDetails) {
		this.lastCompletedBuildDetails = lastCompletedBuildDetails;
	}

	public BuildWithTestReport getLastCompletedBuildTestReport() {
		return lastCompletedBuildTestReport;
	}

	public void setLastCompletedBuildTestReport(
			BuildWithTestReport lastCompletedBuildTestReport) {
		this.lastCompletedBuildTestReport = lastCompletedBuildTestReport;
	}

}
