package com.baloise.confluence.jenkins;

import com.baloise.confluence.exception.ResourceNotFoundException;
import com.baloise.confluence.exception.ServiceUnavailableException;
import com.baloise.confluence.jenkins.bean.JenkinsData;

public interface IJenkinsService {

	/**
	 * Does fetch in realtime the data for a given job and from a given Jenkins
	 * instance, returns a populated object in case of success, throws a checked
	 * exception otherwise.
	 * 
	 * @param host
	 *            the URL of the Jenkins instance
	 * @param jobName
	 *            the name of the Jenkins job
	 * @return a bean populated with the realtime data
	 * @throws ServiceUnavailableException
	 *             if the specified Jenkins instance cannot be reached
	 * @throws ResourceNotFoundException
	 *             if the Jenkins instance does not know the specified job
	 */
	public JenkinsData fetchData(String host, String jobName)
			throws ServiceUnavailableException, ResourceNotFoundException;
}
