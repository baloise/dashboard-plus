package com.baloise.confluence;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.sonar.wsclient.services.Measure;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.confluence.core.DateFormatter;
import com.atlassian.confluence.core.FormatSettingsManager;
import com.atlassian.confluence.core.TimeZone;
import com.atlassian.confluence.core.datetime.FriendlyDateFormatter;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUserPreferences;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.confluence.util.i18n.Message;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.sal.api.message.I18nResolver;
import com.baloise.confluence.exception.ResourceNotFoundException;
import com.baloise.confluence.exception.ServiceUnavailableException;
import com.baloise.confluence.sonar.SonarService;
import com.baloise.confluence.sonar.bean.SonarData;

public class SonarTestStatusMacro extends StatusLightBasedMacro {

	private static final String MACRO_PARAM_NAME_HOST = "host";
	private static final String MACRO_PARAM_NAME_RESOURCEID = "resourceId";
	private static final String MACRO_PARAM_NAME_LABEL = "label";
	private static final String MACRO_PARAM_NAME_THRESHOLD1 = "threshold1";
	private static final String MACRO_PARAM_NAME_THRESHOLD2 = "threshold2";
	private static final String MACRO_PARAM_NAME_PERIOD = "period";
	private static final String MACRO_PARAM_NAME_SHOWDETAILS = "showDetails";

	private static final String MACRO_PARAM_DEFAULT_HOST = "http://itq.bvch.ch/sonar";
	private static final String MACRO_PARAM_DEFAULT_RESOURCEID = "ch.baloise.testing.st:gwpc-st-TEST";
	private static final String MACRO_PARAM_DEFAULT_LABEL = null;
	private static final String MACRO_PARAM_DEFAULT_THRESHOLD1 = "1.0";
	private static final String MACRO_PARAM_DEFAULT_THRESHOLD2 = "0.75";
	private static final String MACRO_PARAM_DEFAULT_PERIOD = "0";
	private static final String MACRO_PARAM_DEFAULT_SHOWDETAILS = "true";

	/* Automatically injected spring components */
	// private final XhtmlContent xhtmlUtils;
	// private ApplicationLinkService applicationLinkService;
	private final Renderer renderer;
	private final UserAccessor userAccessor;
	private final FormatSettingsManager formatSettingsManager;
	private final LocaleManager localeManager;
	private final I18nResolver i18n;

	public SonarTestStatusMacro(/* XhtmlContent xhtmlUtils, */
	/* ApplicationLinkService applicationLinkService, */Renderer renderer,
			UserAccessor userAccessor,
			FormatSettingsManager formatSettingsManager,
			LocaleManager localeManager, I18nResolver i18n) {
		super();
		//this.xhtmlUtils = xhtmlUtils;
		// this.applicationLinkService = applicationLinkService;
		this.renderer = renderer;
		this.userAccessor = userAccessor;
		this.formatSettingsManager = formatSettingsManager;
		this.localeManager = localeManager;
		this.i18n = i18n;
	}

	@Override
	public String execute(Map<String, String> parameters, String bodyContent,
			ConversionContext conversionContext) throws MacroExecutionException {
		Params params = new Params();
		params.host = loadParamValue(parameters, MACRO_PARAM_NAME_HOST,
				MACRO_PARAM_DEFAULT_HOST);
		params.resourceId = loadParamValue(parameters,
				MACRO_PARAM_NAME_RESOURCEID, MACRO_PARAM_DEFAULT_RESOURCEID);

		params.label = loadParamValue(parameters, MACRO_PARAM_NAME_LABEL,
				MACRO_PARAM_DEFAULT_LABEL);
		params.threshold1 = parseDoubleParam(
				loadParamValue(parameters, MACRO_PARAM_NAME_THRESHOLD1,
						MACRO_PARAM_DEFAULT_THRESHOLD1), 0d, 1d);
		params.threshold2 = parseDoubleParam(
				loadParamValue(parameters, MACRO_PARAM_NAME_THRESHOLD2,
						MACRO_PARAM_DEFAULT_THRESHOLD2), 0d, params.threshold1);
		params.period = parseDoubleParam(
				loadParamValue(parameters, MACRO_PARAM_NAME_PERIOD,
						MACRO_PARAM_DEFAULT_PERIOD), -Double.MAX_VALUE,
				Double.MAX_VALUE);

		params.showDetails = Boolean.parseBoolean(loadParamValue(parameters,
				MACRO_PARAM_NAME_SHOWDETAILS, MACRO_PARAM_DEFAULT_SHOWDETAILS));

		Map<String, Object> context = MacroUtils.defaultVelocityContext();
		try {
			SonarData sonarData = SonarService.createServiceAndFetchData(
					params.host, params.resourceId);

			context.put(VELO_PARAM_NAME_LABEL,
					params.label != null ? params.label : sonarData
							.getResource().getName());
			context.put(VELO_PARAM_NAME_COLOR,
					determineStatusColor(params, sonarData));
			context.put(VELO_PARAM_NAME_HYPERLINK, params.host
					+ "/dashboard/index/" + sonarData.getResource().getId());

			context.put(VELO_PARAM_NAME_SHOWDETAILS, params.showDetails);
			Message lastRunDateFriendlyFormatted = newFriendlyDateFormatter()
					.getFormatMessage(sonarData.getLastRunDate());
			Serializable[] lastRunDateFriendlyFormattedArgs;
			if (lastRunDateFriendlyFormatted.getArguments() == null) {
				lastRunDateFriendlyFormattedArgs = new Serializable[0];
			} else {
				lastRunDateFriendlyFormattedArgs = new Serializable[lastRunDateFriendlyFormatted
						.getArguments().length];
				for (int i = 0; i < lastRunDateFriendlyFormattedArgs.length; i++) {
					lastRunDateFriendlyFormattedArgs[i] = String
							.valueOf(lastRunDateFriendlyFormatted
									.getArguments()[i]);
				}
			}
			context.put(VELO_PARAM_NAME_LASTRUNWHEN, i18n.getText(
					lastRunDateFriendlyFormatted.getKey(),
					lastRunDateFriendlyFormattedArgs));
			context.put(VELO_PARAM_NAME_LASTRUNDURATION, GeneralUtil
					.getCompactDuration(sonarData.getLastRunDuration()));
			String testInfo;
			if (sonarData.getTestCount() == 0) {
				testInfo = "0 test";
			} else {
				testInfo = sonarData.getTestSuccessCount() + "/"
						+ sonarData.getTestCount() + " tests";
			}
			Measure testSuccessDensity = sonarData.getTestSuccessDensity();
			if (testSuccessDensity != null) {
				testInfo += " ("
						+ newPercentFormatter()
								.format(testSuccessDensity.getValue()
										.doubleValue() / 100d) + ")";
			}
			context.put(VELO_PARAM_NAME_TESTINFO, testInfo);
		} catch (ResourceNotFoundException e) {
			context.put(VELO_PARAM_NAME_LABEL, "Project not found !");
			context.put(VELO_PARAM_NAME_COLOR, StatusColor.Grey);
			context.put(VELO_PARAM_NAME_HYPERLINK, params.host
					+ "/dashboard/index/" + params.resourceId);
			context.put(VELO_PARAM_NAME_SHOWDETAILS, false);
		} catch (ServiceUnavailableException e) {
			context.put(VELO_PARAM_NAME_LABEL, "Service unavailable !");
			context.put(VELO_PARAM_NAME_COLOR, StatusColor.Grey);
			context.put(VELO_PARAM_NAME_HYPERLINK, params.host);
			context.put(VELO_PARAM_NAME_SHOWDETAILS, false);
		}

		String result = renderer.render(
				VelocityUtils.getRenderedTemplate(VELOCITY_TEMPLATE, context),
				conversionContext);
		return result;
	}

	@Override
	public BodyType getBodyType() {
		return BodyType.NONE;
	}

	@Override
	public OutputType getOutputType() {
		return OutputType.BLOCK;
	}

	private double parseDoubleParam(String paramValue, double minExcl,
			double maxIncl) throws MacroExecutionException {
		double result;
		try {
			result = Double.parseDouble(paramValue);
		} catch (NumberFormatException e) {
			throw new MacroExecutionException("Wrong format: the parameter '"
					+ paramValue + "' is not a decimal value");
		}

		if (result <= minExcl || result > maxIncl) {
			throw new MacroExecutionException("Wrong value: the parameter '"
					+ paramValue + "' is out of the expected range " + minExcl
					+ "-" + maxIncl);
		}

		return result;
	}

	private FriendlyDateFormatter newFriendlyDateFormatter() {
		// Get current user's timezone.
		ConfluenceUserPreferences prefs = userAccessor
				.getConfluenceUserPreferences(AuthenticatedUserThreadLocal
						.get());
		TimeZone userTimezone = prefs.getTimeZone();

		// Build date formatter
		DateFormatter dateFormatter = new DateFormatter(userTimezone,
				formatSettingsManager, localeManager);

		// Build "friendly" date formatter
		FriendlyDateFormatter friendlyDateFormatter = new FriendlyDateFormatter(
				new Date(), dateFormatter);
		return friendlyDateFormatter;
	}

	private String loadParamValue(Map<String, String> parameters,
			String paramName, String defaultParamValue) {
		String result = parameters.get(paramName);
		if (result == null)
			return defaultParamValue;
		else
			return parameters.get(paramName);
	}

	private StatusColor determineStatusColor(Params params, SonarData sonarData) {
		long howOld = System.currentTimeMillis()
				- sonarData.getLastRunDate().getTime();
		Measure testSuccessDensity = sonarData.getTestSuccessDensity();
		if (params.period > 0 && howOld > (params.period * 3600 * 1000)) {
			return StatusColor.Grey;
		} else if (testSuccessDensity == null) {
			return StatusColor.Grey;
		} else if (testSuccessDensity.getValue().doubleValue() >= params.threshold1 * 100) {
			return StatusColor.Green;
		} else if (testSuccessDensity.getValue().doubleValue() >= params.threshold2 * 100) {
			return StatusColor.Yellow;
		} else {
			return StatusColor.Red;
		}
	}

	private static class Params {
		String host, resourceId, label;
		double threshold1, threshold2, period;
		boolean showDetails;
	}

}
