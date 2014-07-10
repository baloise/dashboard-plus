package com.baloise.confluence.sonar;

import com.baloise.confluence.sonar.bean.SonarData;
import com.baloise.confluence.sonar.exception.SonarResourceNotFoundException;
import com.baloise.confluence.sonar.exception.SonarInstanceUnavailableException;

public interface ISonarService {

	/**
	 * Does fetch in realtime the data for a given resource (project) and from a
	 * given Sonar instance, returns a populated object in case of success,
	 * throws a checked exception otherwise.
	 * 
	 * @param host
	 *            the URL of the Sonar instance
	 * @param resourceId
	 *            the identifier of the Sonar resource (project)
	 * @return a bean populated with the realtime data
	 * @throws SonarInstanceUnavailableException
	 *             if the specified Sonar instance cannot be reached
	 * @throws SonarResourceNotFoundException
	 *             if the Sonar instance does not know the specified resource
	 *             (project)
	 */
	public SonarData fetchData(String host, String resourceId)
			throws SonarInstanceUnavailableException, SonarResourceNotFoundException;

}
