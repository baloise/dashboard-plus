/*
 * Copyright (c) 2013 Rising Oak LLC.
 *
 * Distributed under the MIT license: http://opensource.org/licenses/MIT
 */

package com.offbytwo.jenkins;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.HttpResponseException;

import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.MavenJobWithDetails;

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
			// JobWithDetails job = client.get("/job/" + encode(jobName), JobWithDetails.class);
			JobWithDetails job = client.get("/job/" + jobName,
					JobWithDetails.class);
			job.setClient(client);

			return job;
		} catch (HttpResponseException e) {
			if (e.getStatusCode() == 404) {
				return null;
			}
			throw e;
		}

	}

	public MavenJobWithDetails getMavenJob(String jobName) throws IOException {
		try {
			// MavenJobWithDetails job = client.get("/job/" + encode(jobName), MavenJobWithDetails.class);
			MavenJobWithDetails job = client.get("/job/" + jobName,
					MavenJobWithDetails.class);
			job.setClient(client);

			return job;
		} catch (HttpResponseException e) {
			if (e.getStatusCode() == 404) {
				return null;
			}
			throw e;
		}
	}
	/*
	 * private String encode(String pathPart) { // jenkins doesn't like the +
	 * for space, use %20 instead return
	 * URLEncoder.encode(pathPart).replaceAll("\\+", "%20"); }
	 */
}
