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

public abstract class AbstractDPlusMacro implements Macro {

	/* Automatically injected spring components */
	// private final XhtmlContent xhtmlUtils;
	// private ApplicationLinkService applicationLinkService;
	protected final Renderer renderer;
	protected final UserAccessor userAccessor;
	protected final FormatSettingsManager formatSettingsManager;
	protected final LocaleManager localeManager;
	protected final I18nResolver i18n;

	public AbstractDPlusMacro(Renderer renderer, UserAccessor userAccessor,
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

	public static NumberFormat newPercentFormatter() {
		//		ConfluenceUserPreferences prefs = userAccessor
		//				.getConfluenceUserPreferences(AuthenticatedUserThreadLocal
		//						.get());
		//		Locale locale = prefs.getLocale();
		//		NumberFormat result = NumberFormat.getPercentInstance(locale);
		NumberFormat result = NumberFormat.getPercentInstance();
		result.setMinimumFractionDigits(1);
		return result;
	}

	public static String loadDefaultedParamValue(
			Map<String, String> parameters, String paramName,
			String defaultParamValue) {
		String result = parameters.get(paramName);
		if (result == null)
			return defaultParamValue;
		else
			return parameters.get(paramName);
	}

	public static double parseDoubleParam(String paramValue, double minExcl,
			double maxIncl) throws MacroExecutionException {
		double result;
		try {
			result = Double.parseDouble(paramValue);
		} catch (NumberFormatException e) {
			throw new MacroExecutionException("Wrong format: the parameter '" //$NON-NLS-1$
					+ paramValue + "' is not a decimal value"); //$NON-NLS-1$
		}

		if (result <= minExcl) {
			throw new MacroExecutionException("Wrong value: the parameter '" //$NON-NLS-1$
					+ paramValue + "' must be > " + minExcl); //$NON-NLS-1$
		}
		if (result > maxIncl) {
			throw new MacroExecutionException("Wrong value: the parameter '" //$NON-NLS-1$
					+ paramValue + "' must be <= " + maxIncl); //$NON-NLS-1$
		}

		return result;
	}

	public static long parseLongParam(String paramValue, long minExcl,
			long maxIncl) throws MacroExecutionException {
		long result;
		try {
			result = Long.parseLong(paramValue);
		} catch (NumberFormatException e) {
			throw new MacroExecutionException("Wrong format: the parameter '" //$NON-NLS-1$
					+ paramValue + "' is not a long value"); //$NON-NLS-1$
		}

		if (result <= minExcl) {
			throw new MacroExecutionException("Wrong value: the parameter '" //$NON-NLS-1$
					+ paramValue + "' must be > " + minExcl); //$NON-NLS-1$
		}
		if (result > maxIncl) {
			throw new MacroExecutionException("Wrong value: the parameter '" //$NON-NLS-1$
					+ paramValue + "' must be <= " + maxIncl); //$NON-NLS-1$
		}

		return result;
	}

	public FriendlyDateFormatter newFriendlyDateFormatter() {
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

	public static NumberFormat newNumberFormatter() {
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

}
