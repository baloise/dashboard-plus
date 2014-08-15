package com.baloise.confluence.dashboardplus.sonar.bean;

import java.util.Date;

import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;

import com.baloise.confluence.dashboardplus.sonar.SonarService;

public class SonarData {

	private Resource resource;

	public SonarData(Resource resource) {
		super();
		this.resource = resource;
	}

	public Date getLastRunDate() {
		return resource.getDate();
	}

	public long getLastRunDuration() {
		Measure measure = resource
				.getMeasure(SonarService.SONAR_METRIC_KEY_TEST_EXECUTION_TIME);
		if (measure == null) {
			return 0l;
		} else {
			return measure.getValue().longValue();
		}
	}

	public int getTestCount() {
		Integer measure = resource
				.getMeasureIntValue(SonarService.SONAR_METRIC_KEY_TESTS);
		if (measure == null) {
			return 0;
		} else {
			return measure.intValue();
		}
	}

	public int getTestFailureCount() {
		Integer measure = resource
				.getMeasureIntValue(SonarService.SONAR_METRIC_KEY_TEST_FAILURES);
		if (measure == null) {
			return 0;
		} else {
			return measure.intValue();
		}
	}

	public int getTestErrorCount() {
		Integer measure = resource
				.getMeasureIntValue(SonarService.SONAR_METRIC_KEY_TEST_ERRORS);
		if (measure == null) {
			return 0;
		} else {
			return measure.intValue();
		}
	}

	public int getTestSuccessCount() {
		int result = getTestCount() - getTestFailureCount() - getTestErrorCount();
		// Ensure preventively we dont return a value below 0, is probably not necessary but we never know
		if (result < 0) {
			result = 0;
		}
		return result;
	}

	public Measure getTestSuccessDensity() {
		return resource
				.getMeasure(SonarService.SONAR_METRIC_KEY_TEST_SUCCESS_DENSITY);
	}

	public Resource getResource() {
		return resource;
	}

}
