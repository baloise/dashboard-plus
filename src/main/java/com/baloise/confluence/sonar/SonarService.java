package com.baloise.confluence.sonar;

import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

public class SonarService implements ISonarService {

	public static final String SONAR_METRIC_KEY_TEST_SUCCESS_DENSITY = "test_success_density";
	public static final String SONAR_METRIC_KEY_TEST_FAILURES = "test_failures";
	public static final String SONAR_METRIC_KEY_TEST_ERRORS = "test_errors";
	public static final String SONAR_METRIC_KEY_TESTS = "tests";
	public static final String SONAR_METRIC_KEY_TEST_EXECUTION_TIME = "test_execution_time";

	public static SonarData createServiceAndFetchData(String host,
			String resourceId) throws SonarServiceException {
		return new SonarService().fetchData(host, resourceId);
	}

	@Override
	public SonarData fetchData(String host, String resourceId)
			throws SonarServiceException {
		Sonar sonar = Sonar.create(host);

		Resource resource = sonar.find(ResourceQuery.createForMetrics(
				resourceId, SONAR_METRIC_KEY_TESTS,
				SONAR_METRIC_KEY_TEST_ERRORS, SONAR_METRIC_KEY_TEST_FAILURES,
				SONAR_METRIC_KEY_TEST_SUCCESS_DENSITY,
				SONAR_METRIC_KEY_TEST_EXECUTION_TIME));

		if (resource == null) {
			throw new SonarServiceException();
		}

		return new SonarData(resource);
	}

}
