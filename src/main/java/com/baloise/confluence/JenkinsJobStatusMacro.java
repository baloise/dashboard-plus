package com.baloise.confluence;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

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
import com.baloise.confluence.jenkins.JenkinsService;
import com.baloise.confluence.jenkins.bean.JenkinsData;

public class JenkinsJobStatusMacro extends StatusLightBasedMacro {

	private static final String MACRO_PARAM_NAME_HOST = "host";
	private static final String MACRO_PARAM_NAME_JOBNAME = "jobName";
	private static final String MACRO_PARAM_NAME_LABEL = "label";
	private static final String MACRO_PARAM_NAME_PERIOD = "period";
	private static final String MACRO_PARAM_NAME_SHOWDETAILS = "showDetails";

	private static final String MACRO_PARAM_DEFAULT_HOST = "http://svx-build04.bvch.ch:8080";
	private static final String MACRO_PARAM_DEFAULT_JOBNAME = "PolicyCenter_CI";
	private static final String MACRO_PARAM_DEFAULT_LABEL = null;
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

	public JenkinsJobStatusMacro(/* XhtmlContent xhtmlUtils, */
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
		params.host = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_HOST, MACRO_PARAM_DEFAULT_HOST);
		params.jobName = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_JOBNAME, MACRO_PARAM_DEFAULT_JOBNAME);

		params.label = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_LABEL, MACRO_PARAM_DEFAULT_LABEL);
		params.period = parseDoubleParam(
				loadDefaultedParamValue(parameters, MACRO_PARAM_NAME_PERIOD,
						MACRO_PARAM_DEFAULT_PERIOD), -Double.MAX_VALUE,
				Double.MAX_VALUE);

		params.showDetails = Boolean.parseBoolean(loadDefaultedParamValue(
				parameters, MACRO_PARAM_NAME_SHOWDETAILS,
				MACRO_PARAM_DEFAULT_SHOWDETAILS));

		Map<String, Object> context = MacroUtils.defaultVelocityContext();
		try {
			JenkinsData jenkinsData = JenkinsService.createServiceAndFetchData(
					params.host, params.jobName);

			if (jenkinsData.getLastCompletedBuildDetails() == null) {
				context.put(VELO_PARAM_NAME_LABEL, "?");
				context.put(VELO_PARAM_NAME_COLOR, StatusColor.Grey);
				context.put(VELO_PARAM_NAME_HYPERLINK, params.host + "/job/"
						+ params.jobName);
				context.put(VELO_PARAM_NAME_SHOWDETAILS, false);
			} else {
				context.put(VELO_PARAM_NAME_LABEL,
						params.label != null ? params.label : jenkinsData
								.getJobDetails().getDisplayName());
				context.put(VELO_PARAM_NAME_COLOR,
						determineStatusColor(params, jenkinsData));
				context.put(VELO_PARAM_NAME_HYPERLINK, jenkinsData
						.getJobDetails().getUrl());

				context.put(VELO_PARAM_NAME_SHOWDETAILS, params.showDetails);
				Message lastRunDateFriendlyFormatted = newFriendlyDateFormatter()
						.getFormatMessage(
								new Date(jenkinsData
										.getLastCompletedBuildDetails()
										.getTimestamp()));
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
						.getCompactDuration(jenkinsData
								.getLastCompletedBuildDetails().getDuration()));
				int testFailCount = jenkinsData
						.getLastCompletedBuildTestReport().getFailCount();
				int testPassCount = jenkinsData
						.getLastCompletedBuildTestReport().getPassCount();
				;
				int testTotalCount = jenkinsData
						.getLastCompletedBuildTestReport().getTotalCount();
				// Because all Jenkins test reports do not have always the same data, sometimes failCount+passCount, sometimes failCount+totalCount
				if (testTotalCount == 0) {
					testTotalCount = testFailCount + testPassCount;
				} else {
					testPassCount = testTotalCount - testFailCount;
				}
				String testInfo = String.valueOf(testPassCount);
				if (testTotalCount == 0) {
					testInfo += " test";
				} else {
					double ratio = ((double) testPassCount)
							/ ((double) testTotalCount);
					testInfo += "/" + testTotalCount + " tests ("
							+ newPercentFormatter().format(ratio) + ")";
				}
				context.put(VELO_PARAM_NAME_TESTINFO, testInfo);
			}
		} catch (ResourceNotFoundException e) {
			context.put(VELO_PARAM_NAME_LABEL, "Job not found !");
			context.put(VELO_PARAM_NAME_COLOR, StatusColor.Grey);
			context.put(VELO_PARAM_NAME_HYPERLINK, params.host + "/job/"
					+ params.jobName);
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

	private String loadDefaultedParamValue(Map<String, String> parameters,
			String paramName, String defaultParamValue) {
		String result = parameters.get(paramName);
		if (result == null)
			return defaultParamValue;
		else
			return parameters.get(paramName);
	}

	private StatusColor determineStatusColor(Params params,
			JenkinsData jenkinsData) {
		long howOld = System.currentTimeMillis()
				- jenkinsData.getLastCompletedBuildDetails().getTimestamp();
		if (params.period > 0 && howOld > (params.period * 3600 * 1000)) {
			return StatusColor.Grey;
		} else {
			switch (jenkinsData.getLastCompletedBuildDetails().getResult()) {
			case ABORTED:
				return StatusColor.Red;
			case FAILURE:
				return StatusColor.Red;
			case SUCCESS:
				return StatusColor.Green;
			case UNSTABLE:
				return StatusColor.Yellow;
			case BUILDING:
			case REBUILDING:
			case UNKNOWN:
			default:
				return StatusColor.Red;
			}
		}
	}

	private static class Params {
		String host, jobName, label;
		double period;
		boolean showDetails;
	}

	private static enum StatusColor {
		Grey, Red, Yellow, Green
	}

}
