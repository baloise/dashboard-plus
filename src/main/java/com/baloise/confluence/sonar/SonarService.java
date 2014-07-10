package com.baloise.confluence.sonar;

import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import com.baloise.confluence.sonar.bean.SonarData;
import com.baloise.confluence.sonar.exception.SonarResourceNotFoundException;
import com.baloise.confluence.sonar.exception.SonarInstanceUnavailableException;

public class SonarService implements ISonarService {

	public static final String SONAR_METRIC_KEY_TEST_SUCCESS_DENSITY = "test_success_density";
	public static final String SONAR_METRIC_KEY_TEST_FAILURES = "test_failures";
	public static final String SONAR_METRIC_KEY_TEST_ERRORS = "test_errors";
	public static final String SONAR_METRIC_KEY_TESTS = "tests";
	public static final String SONAR_METRIC_KEY_TEST_EXECUTION_TIME = "test_execution_time";

	public static SonarData createServiceAndFetchData(String host,
			String resourceId) throws SonarInstanceUnavailableException,
			SonarResourceNotFoundException {
		return new SonarService().fetchData(host, resourceId);
	}

	@Override
	public SonarData fetchData(String host, String resourceId)
			throws SonarInstanceUnavailableException,
			SonarResourceNotFoundException {
		Sonar sonar = Sonar.create(host);
		SonarData result = null;

		try {
			ResourceQuery query = ResourceQuery.createForMetrics(resourceId,
					SONAR_METRIC_KEY_TESTS, SONAR_METRIC_KEY_TEST_ERRORS,
					SONAR_METRIC_KEY_TEST_FAILURES,
					SONAR_METRIC_KEY_TEST_SUCCESS_DENSITY,
					SONAR_METRIC_KEY_TEST_EXECUTION_TIME);
			query.setTimeoutMilliseconds(1 * 1000);
			Resource resource = sonar.find(query);

			if (resource == null) {
				throw new SonarResourceNotFoundException();
			} else {
				result = new SonarData(resource);
			}
		} catch (RuntimeException /*
								 * org.sonar.wsclient.connectors.ConnectionException
								 */e) {
			throw new SonarInstanceUnavailableException();
		}

		return result;
	}

}
