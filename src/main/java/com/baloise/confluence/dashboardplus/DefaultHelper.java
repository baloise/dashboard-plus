package com.baloise.confluence.dashboardplus;

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;

import com.baloise.confluence.dashboardplus.jira.config.gen.JiraEvalSpec;

public abstract class DefaultHelper {
	private static final String BUNDLE_NAME = "com.baloise.confluence.dashboardplus.default"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static String getDefaultJiraEvalSpec() throws IOException {
		InputStream resourceAsStream = JiraEvalSpec.class
				.getResourceAsStream("/com/baloise/confluence/dashboardplus/jira/config/JiraEvalSpec_default.xml");
		return IOUtils.toString(resourceAsStream);
	}
}
