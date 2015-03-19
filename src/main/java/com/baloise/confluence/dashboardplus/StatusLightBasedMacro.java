package com.baloise.confluence.dashboardplus;

import java.text.NumberFormat;

import com.atlassian.confluence.macro.Macro;

public abstract class StatusLightBasedMacro implements Macro {

	protected static final String VELOCITY_TEMPLATE = "com/baloise/confluence/dashboardplus/status-light.vm";

	protected static final String VELO_PARAM_NAME_TESTINFO = "testInfo";
	protected static final String VELO_PARAM_NAME_LASTRUNDURATION = "lastRunDuration";
	protected static final String VELO_PARAM_NAME_LASTRUNWHEN = "lastRunWhen";
	protected static final String VELO_PARAM_NAME_SHOWDETAILS = "showDetails";
	protected static final String VELO_PARAM_NAME_HYPERLINK = "hyperlink";
	protected static final String VELO_PARAM_NAME_COLOR = "color";
	protected static final String VELO_PARAM_NAME_LABEL = "label";

	protected static enum StatusColor {
		Grey, Red, Yellow, Green
	}

	protected static NumberFormat newPercentFormatter() {
		//		ConfluenceUserPreferences prefs = userAccessor
		//				.getConfluenceUserPreferences(AuthenticatedUserThreadLocal
		//						.get());
		//		Locale locale = prefs.getLocale();
		//		NumberFormat result = NumberFormat.getPercentInstance(locale);
		NumberFormat result = NumberFormat.getPercentInstance();
		result.setMinimumFractionDigits(1);
		return result;
	}

	protected static String formatDuration(long duration) {
		long dayCount = duration / (1000 * 60 * 60 * 24);
		long hourCount = (duration - dayCount * 1000 * 60 * 60 * 24)
				/ (1000 * 60 * 60);
		long minCount = (duration - dayCount * 1000 * 60 * 60 * 24 - hourCount * 1000 * 60 * 60)
				/ (1000 * 60);
		long secCount;
		if (duration < 1000) {
			secCount = 1;
		} else {
			secCount = (duration - dayCount * 1000 * 60 * 60 * 24 - hourCount
					* 1000 * 60 * 60 - minCount * 1000 * 60) / 1000;
		}
		//		long millisCount = duration - dayCount * 1000 * 60 * 60 * 24
		//				- hourCount * 1000 * 60 * 60 - minCount * 1000 * 60 - secCount
		//				* 1000;

		String result = "";
		if (dayCount > 0)
			result += dayCount + "d";
		if (hourCount > 0)
			result += hourCount + "h";
		result += minCount + "'";
		if (secCount<10) {
			result += "0";
		}
		result += secCount + "''";
		return result;
	}
}
