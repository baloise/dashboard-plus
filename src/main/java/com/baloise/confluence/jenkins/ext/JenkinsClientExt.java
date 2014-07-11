package com.baloise.confluence.jenkins.ext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.HttpResponseException;

import com.offbytwo.jenkins.model.Build;

public class JenkinsClientExt {

	public static BuildWithTestReport getTestReport(Build build)
			throws IOException {
		if (build == null) {
			return new BuildWithTestReport();
		} else {
			BuildWithTestReport result = null;
			try {
				result = build.getClient().get(build.getUrl() + "/testReport",
						BuildWithTestReport.class);
			} catch (HttpResponseException e) {
				//			if (e.getStatusCode() == 404) {
				result = new BuildWithTestReport();
				//			}
			}
			result.setClient(build.getClient());
			return result;
		}
	}

	public static String encode(String pathPart)
			throws UnsupportedEncodingException {
		// jenkins doesn't like the + for space, use %20 instead
		return URLEncoder.encode(pathPart, "UTF-8").replaceAll("\\+", "%20");
	}

}
