package com.baloise.confluence;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;

import com.baloise.confluence.exception.ResourceNotFoundException;
import com.baloise.confluence.exception.ServiceUnavailableException;
import com.baloise.confluence.jenkins.JenkinsService;
import com.baloise.confluence.jenkins.bean.JenkinsData;
import com.baloise.confluence.jenkins.ext.JenkinsClientExt;
import com.baloise.confluence.jenkins.ext.BuildWithTestReport;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;

public class JenkinsJobStatus {

	public JenkinsJobStatus() {
		// TODO Auto-generated constructor stub
	}

	public static void main2(String[] args) {
		try {
			JenkinsServer jenkins = new JenkinsServer(new URI(
					"http://localhost"));
			//			Map<String, Job> jobs = jenkins.getJobs();
			//			JobWithDetails details = jobs.get("PolicyCenter_CI".toLowerCase()).details();

			String jobName = "AP_GeschaeftService_RD_Tests";
			//			String jobName = "AP_Opal";
			JobWithDetails job = jenkins.getJob(jobName);

			System.out.println(job.getDisplayName());
			//JobWithDetails job = jenkins.getJob("PolicyCenter_CI");
			//Build build = job.getLastCompletedBuild();
			Build build = job.getLastStableBuild();

			System.out.println(build.getNumber());
			BuildWithDetails details = build.details();
			BuildResult result = details.getResult();
			System.out.println(result);

			BuildWithTestReport testReport = JenkinsClientExt
					.getTestReport(build);
			//			System.out.println(testReport == null ? "n/a" : testReport
			//					.getDuration());
			//			System.out.println(testReport.getDuration());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		//		String jobName = "AP_GeschaeftService_RD_Tests";
		//		String jobName = "AP_Opal";
		String jobName = "PolicyCenter_CI";
		String host = "http://localhost";

		try {
			JenkinsData data = JenkinsService.createServiceAndFetchData(host,
					jobName);
			System.out.println(data.getJobDetails().getDisplayName());
			System.out.println("Build #"
					+ data.getLastCompletedBuildDetails().getNumber()
					+ " duration="
					+ data.getLastCompletedBuildDetails().getTimestamp());
			System.out.println(System.currentTimeMillis());
			
			NumberFormat percentInstance = NumberFormat.getPercentInstance();
			percentInstance.setMinimumFractionDigits(1);
			String format = percentInstance.format(0.12546964654);
			System.out.println(format);

		} catch (ServiceUnavailableException e) {
			e.printStackTrace();
		} catch (ResourceNotFoundException e) {
			e.printStackTrace();
		}
	}
}
