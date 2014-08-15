package com.baloise.confluence.dashboardplus.jenkins;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.baloise.confluence.dashboardplus.exception.ResourceNotFoundException;
import com.baloise.confluence.dashboardplus.exception.ServiceUnavailableException;
import com.baloise.confluence.dashboardplus.jenkins.bean.JenkinsData;
import com.baloise.confluence.dashboardplus.jenkins.ext.JenkinsClientExt;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.JobWithDetails;

public class JenkinsService implements IJenkinsService {

	public static JenkinsData createServiceAndFetchData(String host,
			String jobName) throws ServiceUnavailableException,
			ResourceNotFoundException {
		return new JenkinsService().fetchData(host, jobName);
	}

	@Override
	public JenkinsData fetchData(String host, String jobName)
			throws ServiceUnavailableException, ResourceNotFoundException {
		JenkinsData result;
		try {
			JenkinsServer jenkins = new JenkinsServer(new URI(host));
			JobWithDetails job = jenkins.getJob(jobName);

			if (job == null) {
				throw new ResourceNotFoundException();
			}

			result = new JenkinsData();
			result.setJobDetails(job.details());

			Build lastCompletedBuild = null;
			try {
				lastCompletedBuild = job.getLastCompletedBuild();
			} catch (NullPointerException e) {
				// Possibly thrown by job.getLastCompletedBuild() when no completed build available
			}
			if (lastCompletedBuild != null) {
				result.setLastCompletedBuildDetails(lastCompletedBuild
						.details());
				result.setLastCompletedBuildTestReport(JenkinsClientExt
						.getTestReport(lastCompletedBuild));
			}

		} catch (IOException e) {
			throw new ServiceUnavailableException();
		} catch (URISyntaxException e) {
			throw new ServiceUnavailableException();
		}
		return result;
	}

}
