package com.baloise.confluence.dashboardplus.sonar;

import com.baloise.confluence.dashboardplus.exception.ResourceNotFoundException;
import com.baloise.confluence.dashboardplus.exception.ServiceUnavailableException;
import com.baloise.confluence.dashboardplus.sonar.bean.SonarData;

public interface ISonarService {

	/**
	 * Does fetch in realtime the data for a given resource (project) and from a
	 * given Sonar instance, returns a populated object in case of success or
	 * throws a checked exception otherwise.
	 * 
	 * @param host
	 *            the URL of the Sonar instance
	 * @param resourceId
	 *            the identifier of the Sonar resource (project)
	 * @return a bean populated with the realtime data
	 * @throws ServiceUnavailableException
	 *             if the specified Sonar instance cannot be reached
	 * @throws ResourceNotFoundException
	 *             if the Sonar instance does not know the specified resource
	 *             (project)
	 */
	public SonarData fetchData(String host, String resourceId)
			throws ServiceUnavailableException, ResourceNotFoundException;

}
