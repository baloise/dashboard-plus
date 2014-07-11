package com.baloise.confluence.sonar;

import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import com.baloise.confluence.exception.ResourceNotFoundException;
import com.baloise.confluence.exception.ServiceUnavailableException;
import com.baloise.confluence.sonar.bean.SonarData;

public class SonarService implements ISonarService {

	private static final int SONAR_TIMEOUT_IN_SECONDS = 1;
	public static final String SONAR_METRIC_KEY_TEST_SUCCESS_DENSITY = "test_success_density";
	public static final String SONAR_METRIC_KEY_TEST_FAILURES = "test_failures";
	public static final String SONAR_METRIC_KEY_TEST_ERRORS = "test_errors";
	public static final String SONAR_METRIC_KEY_TESTS = "tests";
	public static final String SONAR_METRIC_KEY_TEST_EXECUTION_TIME = "test_execution_time";

	public static SonarData createServiceAndFetchData(String host,
			String resourceId) throws ServiceUnavailableException,
			ResourceNotFoundException {
		return new SonarService().fetchData(host, resourceId);
	}

	@Override
	public SonarData fetchData(String host, String resourceId)
			throws ServiceUnavailableException, ResourceNotFoundException {
		Sonar sonar = Sonar.create(host);
		SonarData result = null;

		try {
			ResourceQuery query = ResourceQuery.createForMetrics(resourceId,
					SONAR_METRIC_KEY_TESTS, SONAR_METRIC_KEY_TEST_ERRORS,
					SONAR_METRIC_KEY_TEST_FAILURES,
					SONAR_METRIC_KEY_TEST_SUCCESS_DENSITY,
					SONAR_METRIC_KEY_TEST_EXECUTION_TIME);
			query.setTimeoutMilliseconds(SONAR_TIMEOUT_IN_SECONDS * 1000);
			Resource resource = sonar.find(query);

			if (resource == null) {
				throw new ResourceNotFoundException();
			} else {
				result = new SonarData(resource);
			}
		} catch (RuntimeException /*
								 * org.sonar.wsclient.connectors.ConnectionException
								 */e) {
			throw new ServiceUnavailableException();
		}

		return result;
	}

}
