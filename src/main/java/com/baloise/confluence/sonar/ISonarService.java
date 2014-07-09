package com.baloise.confluence.sonar;

public interface ISonarService {

	public SonarData fetchData(String host, String resourceId) throws SonarServiceException;

}
