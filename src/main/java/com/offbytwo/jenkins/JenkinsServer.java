/*
 * Copyright (c) 2013 Rising Oak LLC.
 *
 * Distributed under the MIT license: http://opensource.org/licenses/MIT
 */

package com.offbytwo.jenkins;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.http.client.HttpResponseException;

import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.model.JobWithDetails;

/**
 * The main starting point for interacting with a Jenkins server.
 */
public class JenkinsServer {

	private final JenkinsHttpClient client;

	/**
	 * Create a new Jenkins server reference given only the server address
	 * 
	 * @param serverUri
	 *            address of jenkins server (ex. http://localhost:8080/jenkins)
	 */
	public JenkinsServer(URI serverUri) {
		this(new JenkinsHttpClient(serverUri));
	}

	/**
	 * Create a new Jenkins server reference given the address and credentials
	 * 
	 * @param serverUri
	 *            address of jenkins server (ex. http://localhost:8080/jenkins)
	 * @param username
	 *            username to use when connecting
	 * @param passwordOrToken
	 *            password (not recommended) or token (recommended)
	 */
	public JenkinsServer(URI serverUri, String username, String passwordOrToken) {
		this(new JenkinsHttpClient(serverUri, username, passwordOrToken));
	}

	/**
	 * Create a new Jenkins server directly from an HTTP client (ADVANCED)
	 * 
	 * @param client
	 *            Specialized client to use.
	 */
	public JenkinsServer(JenkinsHttpClient client) {
		this.client = client;
	}

	/**
	 * Get a single Job from the server.
	 * 
	 * @return A single Job, null if not present
	 * @throws IOException
	 */
	public JobWithDetails getJob(String jobName) throws IOException {
		try {
			JobWithDetails job = client.get("/job/" + encode(jobName),
					JobWithDetails.class);
			// JobWithDetails job = client.get("/job/" + jobName, JobWithDetails.class);
			job.setClient(client);

			return job;
		} catch (HttpResponseException e) {
			if (e.getStatusCode() == 404) {
				return null;
			}
			throw e;
		}

	}

	public static String encode(String path) {
		// seems Jenkins prefers some characters in ASCII representation, especially / and : that are encountered in case of Maven multi-module projects
		String result = URLEncoder.encode(path).replaceAll("\\+", "%20")
				.replaceAll("\\%2F", "/").replaceAll("\\%3A", ":");
		return result;
	}

}
