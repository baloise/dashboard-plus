package com.baloise.confluence.dashboardplus;

import java.io.Serializable;
import java.util.Date;
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
import com.atlassian.confluence.util.i18n.Message;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.user.User;
import com.baloise.confluence.dashboardplus.exception.ResourceNotFoundException;
import com.baloise.confluence.dashboardplus.exception.ServiceUnavailableException;
import com.baloise.confluence.dashboardplus.jenkins.JenkinsService;
import com.baloise.confluence.dashboardplus.jenkins.bean.JenkinsData;

public class JenkinsJobStatusMacro extends StatusLightBasedMacro {

	private static final String MACRO_PARAM_NAME_HOST = "host"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_JOBNAME = "jobName"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_LABEL = "label"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_PERIOD = "period"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_SHOWDETAILS = "showDetails"; //$NON-NLS-1$

	private static final String MACRO_PARAM_DEFAULT_HOST = Default
			.getString("JenkinsJobStatusMacro.host"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_JOBNAME = Default
			.getString("JenkinsJobStatusMacro.jobName"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_LABEL = Default
			.getString("JenkinsJobStatusMacro.label"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_PERIOD = Default
			.getString("JenkinsJobStatusMacro.period"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_SHOWDETAILS = Default
			.getString("JenkinsJobStatusMacro.showDetails"); //$NON-NLS-1$

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
				context.put(VELO_PARAM_NAME_LABEL, "?"); //$NON-NLS-1$
				context.put(VELO_PARAM_NAME_COLOR, StatusColor.Grey);
				context.put(VELO_PARAM_NAME_HYPERLINK, params.host + "/job/" //$NON-NLS-1$
						+ params.jobName);
				context.put(VELO_PARAM_NAME_SHOWDETAILS, false);
			} else {
				context.put(VELO_PARAM_NAME_LABEL, params.label != null
						&& params.label.trim().length() > 0 ? params.label
						: jenkinsData.getJobDetails().getDisplayName());
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
				context.put(VELO_PARAM_NAME_LASTRUNDURATION,
						formatDuration(jenkinsData
								.getLastCompletedBuildDetails().getDuration()));

				int testFailCount = jenkinsData
						.getLastCompletedBuildTestReport().getFailCount();
				int testPassCount = jenkinsData
						.getLastCompletedBuildTestReport().getPassCount();
				;
				int testTotalCount = jenkinsData
						.getLastCompletedBuildTestReport().getTotalCount();
				// Because all Jenkins test reports do not provide always with the same data, sometimes failCount+passCount, sometimes failCount+totalCount
				if (testTotalCount == 0) {
					testTotalCount = testFailCount + testPassCount;
				} else {
					testPassCount = testTotalCount - testFailCount;
				}
				String testInfo = String.valueOf(testPassCount);
				if (testTotalCount == 0) {
					testInfo += " test"; //$NON-NLS-1$
				} else {
					double ratio = ((double) testPassCount)
							/ ((double) testTotalCount);
					testInfo += "/" + testTotalCount + " tests (" //$NON-NLS-1$ //$NON-NLS-2$
							+ newPercentFormatter().format(ratio) + ")"; //$NON-NLS-1$
				}
				context.put(VELO_PARAM_NAME_TESTINFO, testInfo);
			}
		} catch (ResourceNotFoundException e) {
			context.put(VELO_PARAM_NAME_LABEL, "Job not found !"); //$NON-NLS-1$
			context.put(VELO_PARAM_NAME_COLOR, StatusColor.Grey);
			context.put(VELO_PARAM_NAME_HYPERLINK, params.host + "/job/" //$NON-NLS-1$
					+ params.jobName);
			context.put(VELO_PARAM_NAME_SHOWDETAILS, false);
		} catch (ServiceUnavailableException e) {
			context.put(VELO_PARAM_NAME_LABEL, "Service unavailable !"); //$NON-NLS-1$
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

	private FriendlyDateFormatter newFriendlyDateFormatter() {
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
