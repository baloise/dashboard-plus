/*
 * Copyright (c) 2013 Rising Oak LLC.
 *
 * Distributed under the MIT license: http://opensource.org/licenses/MIT
 */

package com.offbytwo.jenkins.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.HttpResponseException;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;

public class Build extends BaseModel {

	int number;
	String url;

	public Build() {
	}

	public Build(Build from) {
		this(from.getNumber(), from.getUrl());
	}

	public Build(int number, String url) {
		this.number = number;
		this.url = url;
	}

	public int getNumber() {
		return number;
	}

	public String getUrl() {
		return url;
	}

	public BuildWithDetails details() throws IOException {
		return client.get(url, BuildWithDetails.class);
	}

	public TestReport testReport(boolean recursiveChildLoading)
			throws IOException {
		return testReport(client, url, recursiveChildLoading);
	}

	private static TestReport testReport(JenkinsHttpClient client, String url,
			boolean recursiveChildLoading) throws IOException {
		TestReport result = null;
		result = client.get(JenkinsServer.encode(url + "/testReport"),
				TestReport.class);
		result.setMissingValues();

		if (recursiveChildLoading && result.getChildReports() != null) {
			List<TestReportSuite> suites = new ArrayList<TestReportSuite>();
			if (result.getSuites() != null) {
				suites.addAll(Arrays.asList(result.getSuites()));
			}
			for (TestReportChildReport report : result.getChildReports()) {
				TestReport testReport = testReport(client, report.getChild()
						.getUrl(), recursiveChildLoading);
				suites.addAll(Arrays.asList(testReport.getSuites()));
			}
			TestReportSuite[] resultSuites = new TestReportSuite[suites.size()];
			result.setSuites(suites.toArray(resultSuites));
		}
		result.setClient(client);
		return result;
	}

}
