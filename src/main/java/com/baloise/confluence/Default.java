package com.baloise.confluence;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Default {
	private static final String BUNDLE_NAME = "com.baloise.confluence.default"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Default() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
