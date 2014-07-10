package com.baloise.confluence.sonar.bean;

import java.util.Date;

import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;

import com.baloise.confluence.sonar.SonarService;

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
		return resource
				.getMeasure(SonarService.SONAR_METRIC_KEY_TEST_EXECUTION_TIME)
				.getValue().longValue();
	}

	public int getTestCount() {
		return resource.getMeasureIntValue(SonarService.SONAR_METRIC_KEY_TESTS)
				.intValue();
	}

	public int getTestSuccessCount() {
		return resource.getMeasureIntValue(SonarService.SONAR_METRIC_KEY_TESTS)
				.intValue()
				- resource.getMeasureIntValue(
						SonarService.SONAR_METRIC_KEY_TEST_FAILURES).intValue()
				- resource.getMeasureIntValue(
						SonarService.SONAR_METRIC_KEY_TEST_ERRORS).intValue();
	}

	public Measure getTestSuccessDensity() {
		return resource
				.getMeasure(SonarService.SONAR_METRIC_KEY_TEST_SUCCESS_DENSITY);
	}

	public Resource getResource() {
		return resource;
	}

}
