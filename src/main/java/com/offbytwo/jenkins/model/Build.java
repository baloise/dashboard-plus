/*
 * Copyright (c) 2013 Rising Oak LLC.
 *
 * Distributed under the MIT license: http://opensource.org/licenses/MIT
 */

package com.offbytwo.jenkins.model;

import java.io.IOException;

import org.apache.http.client.HttpResponseException;

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

	public BuildWithTestReport testReport() throws IOException {
		BuildWithTestReport result = null;
		try {
			result = client.get(url + "/testReport", BuildWithTestReport.class);
		} catch (HttpResponseException e) {
			//			if (e.getStatusCode() == 404) {
			result = new BuildWithTestReport();
			//			}
		}
		result.setClient(client);
		return result;
	}
}
