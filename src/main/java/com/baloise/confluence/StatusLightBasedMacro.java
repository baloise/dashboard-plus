package com.baloise.confluence;

import java.text.NumberFormat;

import com.atlassian.confluence.macro.Macro;

public abstract class StatusLightBasedMacro implements Macro {

	protected static final String VELOCITY_TEMPLATE = "com/baloise/confluence/status-light.vm";

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

}
