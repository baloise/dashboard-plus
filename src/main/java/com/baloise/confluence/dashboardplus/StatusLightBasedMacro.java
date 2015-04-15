package com.baloise.confluence.dashboardplus;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.confluence.core.DateFormatter;
import com.atlassian.confluence.core.FormatSettingsManager;
import com.atlassian.confluence.core.TimeZone;
import com.atlassian.confluence.core.datetime.FriendlyDateFormatter;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUserPreferences;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.user.User;

public abstract class StatusLightBasedMacro implements Macro {

	protected static final String VELOCITY_TEMPLATE = "com/baloise/confluence/dashboardplus/status-light.vm";

	protected static final String VELO_PARAM_NAME_TESTINFO = "testInfo";
	protected static final String VELO_PARAM_NAME_LASTRUNDURATION = "lastRunDuration";
	protected static final String VELO_PARAM_NAME_LASTRUNWHEN = "lastRunWhen";
	protected static final String VELO_PARAM_NAME_SHOWDETAILS = "showDetails";
	protected static final String VELO_PARAM_NAME_HYPERLINK_URL = "hyperlinkURL";
	protected static final String VELO_PARAM_NAME_HYPERLINK_TARGET = "hyperlinkTarget";
	protected static final String VELO_PARAM_NAME_COLOR = "color";
	protected static final String VELO_PARAM_NAME_LABEL = "label";
	protected static final String VELO_PARAM_NAME_APPLY_OUTLINE = "applyOutline";
	protected static final String VELO_PARAM_NAME_TESTDETAILS = "testDetails";
	protected static final String VELO_PARAM_NAME_SHOWFAILEDTESTDETAILSASTOOLTIP = "showFailedTestDetailsAsTooltip";
	protected static final String VELO_PARAM_NAME_FONTSIZE = "fontSize";

	/* Automatically injected spring components */
	// private final XhtmlContent xhtmlUtils;
	// private ApplicationLinkService applicationLinkService;
	protected final Renderer renderer;
	protected final UserAccessor userAccessor;
	protected final FormatSettingsManager formatSettingsManager;
	protected final LocaleManager localeManager;
	protected final I18nResolver i18n;

	public StatusLightBasedMacro(Renderer renderer, UserAccessor userAccessor,
			FormatSettingsManager formatSettingsManager,
			LocaleManager localeManager, I18nResolver i18n) {
		super();
		this.renderer = renderer;
		this.userAccessor = userAccessor;
		this.formatSettingsManager = formatSettingsManager;
		this.localeManager = localeManager;
		this.i18n = i18n;
	}

	@Override
	public BodyType getBodyType() {
		return BodyType.NONE;
	}

	@Override
	public OutputType getOutputType() {
		return OutputType.BLOCK;
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
		if (secCount < 10) {
			result += "0";
		}
		result += secCount + "''";
		return result;
	}

	protected static String loadDefaultedParamValue(
			Map<String, String> parameters, String paramName,
			String defaultParamValue) {
		String result = parameters.get(paramName);
		if (result == null)
			return defaultParamValue;
		else
			return parameters.get(paramName);
	}

	protected static double parseDoubleParam(String paramValue, double minExcl,
			double maxIncl) throws MacroExecutionException {
		double result;
		try {
			result = Double.parseDouble(paramValue);
		} catch (NumberFormatException e) {
			throw new MacroExecutionException("Wrong format: the parameter '" //$NON-NLS-1$
					+ paramValue + "' is not a decimal value"); //$NON-NLS-1$
		}

		if (result <= minExcl || result > maxIncl) {
			throw new MacroExecutionException("Wrong value: the parameter '" //$NON-NLS-1$
					+ paramValue + "' is out of the expected range " + minExcl //$NON-NLS-1$
					+ "-" + maxIncl); //$NON-NLS-1$
		}

		return result;
	}

	protected FriendlyDateFormatter newFriendlyDateFormatter() {
		// Get current user's timezone, or default one
		User authUser = AuthenticatedUserThreadLocal.getUser();
		TimeZone timeZone;
		if (authUser == null) {
			// anonymous
			timeZone = TimeZone.getDefault();
		} else {
			ConfluenceUserPreferences prefs = userAccessor
					.getConfluenceUserPreferences(authUser);
			timeZone = prefs.getTimeZone();
		}
		// Build date formatter
		DateFormatter dateFormatter = new DateFormatter(timeZone,
				formatSettingsManager, localeManager);

		// Build "friendly" date formatter
		FriendlyDateFormatter friendlyDateFormatter = new FriendlyDateFormatter(
				new Date(), dateFormatter);
		return friendlyDateFormatter;
	}

	protected NumberFormat newNumberFormatter() {
		// Get current user's timezone, or default one
		/*
		User authUser = AuthenticatedUserThreadLocal.getUser();
		NumberFormat result = null;
		if (authUser != null) {
			System.out.println("authUser = " + authUser.getFullName());
			ConfluenceUserPreferences prefs = userAccessor
					.getConfluenceUserPreferences(authUser);
			Locale locale = prefs.getLocale();
			//			System.out.println("prefs.getLocale() = "+locale);
			//			if (locale == null)  {
			//				//locale = new Locale("de", "CH");
			//				locale = new Locale("en", "US");
			//				System.out.println("use locale "+locale);
			//			}
			if (prefs != null && prefs.getLocale() != null) {
				result = DecimalFormat.getNumberInstance(locale);
			}
		}
		if (result == null) {
			// anonymous
			result = NumberFormat.getNumberInstance();
		}*/
		DecimalFormat result = (DecimalFormat) DecimalFormat
				.getInstance(new Locale("de", "CH"));
		DecimalFormatSymbols symbols = result.getDecimalFormatSymbols();
		symbols.setGroupingSeparator('\'');
		result.setDecimalFormatSymbols(symbols);
		return result;
	}

	protected String computeTestInfo(StatusLightData slData) {
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
