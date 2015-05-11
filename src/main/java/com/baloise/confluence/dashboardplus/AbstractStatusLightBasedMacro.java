package com.baloise.confluence.dashboardplus;

import java.text.NumberFormat;

import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.confluence.core.FormatSettingsManager;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.message.I18nResolver;

public abstract class AbstractStatusLightBasedMacro extends AbstractDPlusMacro {

	public static final String VELOCITY_TEMPLATE = "com/baloise/confluence/dashboardplus/status-light.vm";

	public static final String VELO_PARAM_NAME_TESTINFO = "testInfo";
	public static final String VELO_PARAM_NAME_LASTRUNDURATION = "lastRunDuration";
	public static final String VELO_PARAM_NAME_LASTRUNWHEN = "lastRunWhen";
	public static final String VELO_PARAM_NAME_SHOWDETAILS = "showDetails";
	public static final String VELO_PARAM_NAME_HYPERLINK_URL = "hyperlinkURL";
	public static final String VELO_PARAM_NAME_HYPERLINK_TARGET = "hyperlinkTarget";
	public static final String VELO_PARAM_NAME_COLOR = "color";
	public static final String VELO_PARAM_NAME_LABEL = "label";
	public static final String VELO_PARAM_NAME_APPLY_OUTLINE = "applyOutline";
	public static final String VELO_PARAM_NAME_TESTDETAILS = "testDetails";
	public static final String VELO_PARAM_NAME_SHOWFAILEDTESTDETAILSASTOOLTIP = "showFailedTestDetailsAsTooltip";
	public static final String VELO_PARAM_NAME_FONTSIZE = "fontSize";

	public AbstractStatusLightBasedMacro(Renderer renderer,
			UserAccessor userAccessor,
			FormatSettingsManager formatSettingsManager,
			LocaleManager localeManager, I18nResolver i18n) {
		super(renderer, userAccessor, formatSettingsManager, localeManager,
				i18n);
	}

	public static enum StatusColor {
		// Take care to the position of the constants, it is used on aggregation
		Blue, Grey, Green, Yellow, Red;

		public String getCSSClass(boolean applyOutline) {
			String result;
			switch (StatusColor.valueOf(name())) {
			case Grey:
				result = "";
				break;
			default:
				result = "dplus-light-" + name().toLowerCase();
			}
			if (applyOutline) {
				result += " dplus-light-subtle";
			}
			return result;
		}
	}

	public static String formatDuration(long duration) {
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
		if (minCount < 10 && hourCount > 0) {
			result += "0";
		}
		result += minCount + "'";
		if (secCount < 10) {
			result += "0";
		}
		result += secCount + "''";
		return result;
	}

	public String computeTestInfo(StatusLightData slData) {
		NumberFormat numberFormatter = newNumberFormatter();
		String testInfo = numberFormatter.format(slData.getTestPassCount());
		if (slData.getTestTotalCount() > 0) {
			testInfo += "/"
					+ numberFormatter.format(slData.getTestTotalCount());
		}
		if (slData.getTestTotalCount() <= 1) {
			testInfo += " test"; //$NON-NLS-1$
		} else {
			testInfo += " tests (" //$NON-NLS-1$ //$NON-NLS-2$
					+ newPercentFormatter().format(slData.calcSuccessRatio())
					+ ")"; //$NON-NLS-1$
		}
		return testInfo;
	}
}
