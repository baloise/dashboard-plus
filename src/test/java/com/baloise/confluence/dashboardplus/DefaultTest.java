package com.baloise.confluence.dashboardplus;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;

public class DefaultTest {

	@Test
	public void testGetStringWithExistingKey() {
		String key = "StaticStatusLightMacro.label";
		assertTrue(!Default.getString(key).contains(key));
	}
	
	@Test
	public void testGetStringWithMissingKey() {
		String key = "MissingKey";
		assertTrue(Default.getString(key).contains(key));
	}
}
