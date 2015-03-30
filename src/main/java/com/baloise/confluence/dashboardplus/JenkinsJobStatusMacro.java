package com.baloise.confluence.dashboardplus;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.confluence.core.FormatSettingsManager;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.i18n.Message;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.sal.api.message.I18nResolver;
import com.baloise.confluence.dashboardplus.exception.ResourceNotFoundException;
import com.baloise.confluence.dashboardplus.exception.ServiceUnavailableException;
import com.baloise.confluence.dashboardplus.jenkins.JenkinsService;
import com.baloise.confluence.dashboardplus.jenkins.bean.JenkinsData;
import com.offbytwo.jenkins.model.TestReport;
import com.offbytwo.jenkins.model.TestReportSuite;
import com.offbytwo.jenkins.model.TestReportSuiteCase;

public class JenkinsJobStatusMacro extends StatusLightBasedMacro {

	private static final String MACRO_PARAM_NAME_HOST = "host"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_JOBNAME = "jobName"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_LABEL = "label"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_PERIOD = "period"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_SHOWDETAILS = "showDetails"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_DOESREFLECTTEST = "doesReflectTest"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_SIMPLETHRESHOLDMODEL = "simpleThresholdModel"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_THRESHOLD1 = "threshold1"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_THRESHOLD2 = "threshold2"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_HYPERLINKURL = "hyperlinkURL"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_INCLSKIPPEDTESTS = "inclSkippedTests"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_HYPERLINKTARGET = "hyperlinkTarget";
	private static final String MACRO_PARAM_NAME_APPLYOUTLINESTYLE = "applyOutlineStyle";
	private static final String MACRO_PARAM_NAME_SHOWFAILEDTESTDETAILSASTOOLTIP = "showFailedTestDetailsAsTooltip";
	private static final String MACRO_PARAM_NAME_FONTSIZE = "fontSize";

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
	private static final String MACRO_PARAM_DEFAULT_DOESREFLECTTEST = Default
			.getString("JenkinsJobStatusMacro.doesReflectTest"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_SIMPLETHRESHOLDMODEL = Default
			.getString("JenkinsJobStatusMacro.simpleThresholdModel"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_THRESHOLD1 = Default
			.getString("JenkinsJobStatusMacro.threshold1"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_THRESHOLD2 = Default
			.getString("JenkinsJobStatusMacro.threshold2"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_HYPERLINKURL = Default
			.getString("JenkinsJobStatusMacro.hyperlinkURL"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_INCLSKIPPEDTESTS = Default
			.getString("JenkinsJobStatusMacro.inclSkippedTests"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_HYPERLINKTARGET = Default
			.getString("JenkinsJobStatusMacro.hyperlinkTarget"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_APPLYOUTLINESTYLE = Default
			.getString("JenkinsJobStatusMacro.applyOutlineStyle"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_SHOWFAILEDTESTDETAILSASTOOLTIP = Default
			.getString("JenkinsJobStatusMacro.showFailedTestDetailsAsTooltip"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_FONTSIZE = Default
			.getString("JenkinsJobStatusMacro.fontSize"); //$NON-NLS-1$

	/* Automatically injected spring components */
	// private final XhtmlContent xhtmlUtils;
	// private ApplicationLinkService applicationLinkService;

	public JenkinsJobStatusMacro(/* XhtmlContent xhtmlUtils, */
	/* ApplicationLinkService applicationLinkService, */Renderer renderer,
			UserAccessor userAccessor,
			FormatSettingsManager formatSettingsManager,
			LocaleManager localeManager, I18nResolver i18n) {
		super(renderer, userAccessor, formatSettingsManager, localeManager,
				i18n);
	}

	@Override
	public String execute(Map<String, String> parameters, String bodyContent,
			ConversionContext conversionContext) throws MacroExecutionException {
		Params params = extractParams(parameters);

		Map<String, Object> veloContext = MacroUtils.defaultVelocityContext();
		try {
			String[] splitMax = params.jobName.split(" \\[=\\] ");
			String[] splitAdd = params.jobName.split(" \\[\\+\\] ");

			if (splitAdd.length > 1 && splitMax.length > 1) {
				throw new MacroExecutionException(
						"Use either ' [=] ' or ' [+] ' as job separator for an aggregation, not both");
			}

			String[] split = splitMax;
			if (splitAdd.length > splitMax.length) {
				split = splitAdd;
			}

			JenkinsData primaryJenkinsData = JenkinsService
					.createServiceAndFetchData(params.host, split[0],
							params.showFailedTestDetailsAsTooltip);
			StatusLightData primarySLData = evaluateJenkinsData(params,
					primaryJenkinsData);

			for (int i = 1; i < split.length; i++) {
				try {
					JenkinsData secondaryJenkinsData = JenkinsService
							.createServiceAndFetchData(params.host, split[i],
									params.showFailedTestDetailsAsTooltip);
					StatusLightData secondarySLData = evaluateJenkinsData(
							params, secondaryJenkinsData);
					primarySLData.aggregateWith(secondarySLData,
							split == splitAdd);
					continue;
				} catch (ResourceNotFoundException e) {
				} catch (ServiceUnavailableException e) {
				}
				StatusLightData slDataOnException = new StatusLightData();
				slDataOnException.setColor(StatusColor.Grey);
				primarySLData.aggregateWith(slDataOnException,
						split == splitAdd);
			}

			populateVeloContext(params, veloContext, primaryJenkinsData,
					primarySLData);

		} catch (ResourceNotFoundException e) {
			veloContext.put(VELO_PARAM_NAME_LABEL, "?"); //$NON-NLS-1$
			veloContext.put(VELO_PARAM_NAME_COLOR, StatusColor.Grey);
			veloContext.put(VELO_PARAM_NAME_HYPERLINK_URL, params.host
					+ "/job/" //$NON-NLS-1$
					+ params.jobName);
			veloContext.put(VELO_PARAM_NAME_SHOWDETAILS, false);
			veloContext.put(VELO_PARAM_NAME_TESTDETAILS, e.getMessage());
			veloContext.put(VELO_PARAM_NAME_SHOWFAILEDTESTDETAILSASTOOLTIP,
					true);
		} catch (ServiceUnavailableException e) {
			veloContext.put(VELO_PARAM_NAME_LABEL, "!"); //$NON-NLS-1$
			veloContext.put(VELO_PARAM_NAME_COLOR, StatusColor.Grey);
			veloContext.put(VELO_PARAM_NAME_HYPERLINK_URL, params.host);
			veloContext.put(VELO_PARAM_NAME_SHOWDETAILS, false);
			veloContext.put(VELO_PARAM_NAME_TESTDETAILS, e.getMessage());
			veloContext.put(VELO_PARAM_NAME_SHOWFAILEDTESTDETAILSASTOOLTIP,
					true);
		}
		veloContext.put(VELO_PARAM_NAME_HYPERLINK_TARGET,
				params.hyperlinkTarget);
		veloContext
				.put(VELO_PARAM_NAME_APPLY_OUTLINE, params.applyOutlineStyle);
		veloContext.put(VELO_PARAM_NAME_FONTSIZE, params.fontSize);

		String result = renderer.render(VelocityUtils.getRenderedTemplate(
				VELOCITY_TEMPLATE, veloContext), conversionContext);
		return result;
	}

	private StatusLightData evaluateJenkinsData(Params params,
			JenkinsData jenkinsData) {
		StatusLightData result = new StatusLightData();
		if (jenkinsData.getLastCompletedBuildDetails() == null) {
			result.setColor(StatusColor.Blue);
		} else {
			result.setColor(determineStatusColor(params, jenkinsData));
			result.setLastRunTimestamp(jenkinsData
					.getLastCompletedBuildDetails().getTimestamp());
			result.setLastRunDurationInMillis(jenkinsData
					.getLastCompletedBuildDetails().getDuration());

			result.setTestPassCount(jenkinsData
					.getLastCompletedBuildTestReport().getPassCount());
			result.setTestTotalCount(jenkinsData
					.getLastCompletedBuildTestReport().getTotalCount(
							params.inclSkippedTests));

			result.setTestDetails(computeTestDetails(jenkinsData
					.getLastCompletedBuildTestReport()));
		}
		return result;
	}

	private static String computeTestDetails(TestReport testReport) {
		String result = "";
		if (testReport.getSuites() != null) {
			for (TestReportSuite suite : testReport.getSuites()) {
				if (suite.getCases() != null) {
					for (TestReportSuiteCase aCase : suite.getCases()) {
						if ("FAILED".equals(aCase.getStatus())) {
							result += "(!) Test " + aCase.getClassName() + "."
									+ aCase.getName() + " FAILED\n";
							result += aCase.getErrorDetails();
						}

					}
				}
			}
		}
		return result;
	}

	private void populateVeloContext(Params params,
			Map<String, Object> veloContext, JenkinsData jenkinsData,
			StatusLightData slData) {

		veloContext.put(VELO_PARAM_NAME_COLOR, slData.getColor());
		veloContext.put(VELO_PARAM_NAME_SHOWFAILEDTESTDETAILSASTOOLTIP,
				params.showFailedTestDetailsAsTooltip);

		if (jenkinsData.getLastCompletedBuildDetails() == null) {
			veloContext.put(VELO_PARAM_NAME_LABEL, "-"); //$NON-NLS-1$
			veloContext.put(VELO_PARAM_NAME_HYPERLINK_URL, params.host
					+ "/job/" //$NON-NLS-1$
					+ params.jobName);
			veloContext.put(VELO_PARAM_NAME_SHOWDETAILS, false);
		} else {
			veloContext.put(VELO_PARAM_NAME_LABEL, params.label != null
					&& params.label.trim().length() > 0 ? params.label
					: jenkinsData.getJobDetails().getDisplayName());
			veloContext.put(VELO_PARAM_NAME_HYPERLINK_URL, jenkinsData
					.getJobDetails().getUrl());

			veloContext.put(VELO_PARAM_NAME_SHOWDETAILS, params.showDetails);
			Message lastRunDateFriendlyFormatted = newFriendlyDateFormatter()
					.getFormatMessage(new Date(slData.getLastRunTimestamp()));
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
			veloContext.put(VELO_PARAM_NAME_LASTRUNWHEN, i18n.getText(
					lastRunDateFriendlyFormatted.getKey(),
					lastRunDateFriendlyFormattedArgs));
			veloContext.put(VELO_PARAM_NAME_LASTRUNDURATION,
					formatDuration(slData.getLastRunDurationInMillis()));

			String testInfo = String.valueOf(slData.getTestPassCount());
			if (slData.getTestTotalCount() == 0) {
				testInfo += " test"; //$NON-NLS-1$
			} else {
				testInfo += "/" + slData.getTestTotalCount()
						+ " tests (" //$NON-NLS-1$ //$NON-NLS-2$
						+ newPercentFormatter().format(
								slData.calcSuccessRatio()) + ")"; //$NON-NLS-1$
			}
			veloContext.put(VELO_PARAM_NAME_TESTINFO, testInfo);
			veloContext.put(VELO_PARAM_NAME_TESTDETAILS,
					slData.getTestDetails());
		}

		if (params.hyperlinkURL.trim().length() > 0) {
			veloContext.put(VELO_PARAM_NAME_HYPERLINK_URL, params.hyperlinkURL);
		}
	}

	private Params extractParams(Map<String, String> parameters)
			throws MacroExecutionException {
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
		params.doesReflectTest = Boolean.parseBoolean(loadDefaultedParamValue(
				parameters, MACRO_PARAM_NAME_DOESREFLECTTEST,
				MACRO_PARAM_DEFAULT_DOESREFLECTTEST));
		params.simpleThresholdModel = Boolean
				.parseBoolean(loadDefaultedParamValue(parameters,
						MACRO_PARAM_NAME_SIMPLETHRESHOLDMODEL,
						MACRO_PARAM_DEFAULT_SIMPLETHRESHOLDMODEL));
		params.threshold1 = parseDoubleParam(
				loadDefaultedParamValue(parameters,
						MACRO_PARAM_NAME_THRESHOLD1,
						MACRO_PARAM_DEFAULT_THRESHOLD1), 0d, 1d);
		params.threshold2 = parseDoubleParam(
				loadDefaultedParamValue(parameters,
						MACRO_PARAM_NAME_THRESHOLD2,
						MACRO_PARAM_DEFAULT_THRESHOLD2), 0d, params.threshold1);
		params.hyperlinkURL = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_HYPERLINKURL, MACRO_PARAM_DEFAULT_HYPERLINKURL);
		params.hyperlinkTarget = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_HYPERLINKTARGET,
				MACRO_PARAM_DEFAULT_HYPERLINKTARGET);
		params.inclSkippedTests = Boolean.parseBoolean(loadDefaultedParamValue(
				parameters, MACRO_PARAM_NAME_INCLSKIPPEDTESTS,
				MACRO_PARAM_DEFAULT_INCLSKIPPEDTESTS));
		params.applyOutlineStyle = Boolean
				.parseBoolean(loadDefaultedParamValue(parameters,
						MACRO_PARAM_NAME_APPLYOUTLINESTYLE,
						MACRO_PARAM_DEFAULT_APPLYOUTLINESTYLE));
		params.showFailedTestDetailsAsTooltip = Boolean
				.parseBoolean(loadDefaultedParamValue(parameters,
						MACRO_PARAM_NAME_SHOWFAILEDTESTDETAILSASTOOLTIP,
						MACRO_PARAM_DEFAULT_SHOWFAILEDTESTDETAILSASTOOLTIP));
		params.fontSize = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_FONTSIZE, MACRO_PARAM_DEFAULT_FONTSIZE);

		return params;
	}

	private StatusColor determineStatusColor(Params params,
			JenkinsData jenkinsData) {
		long howOld = System.currentTimeMillis()
				- jenkinsData.getLastCompletedBuildDetails().getTimestamp();
		if (params.period > 0 && howOld > (params.period * 3600 * 1000)) {
			return StatusColor.Grey;
		} else {
			if (params.doesReflectTest) {
				double successRatio = jenkinsData
						.getLastCompletedBuildTestReport().calcSuccessRatio(
								params.inclSkippedTests);
				if (params.simpleThresholdModel) {
					if (successRatio == 1d) {
						return StatusColor.Green;
					} else if (successRatio == 0d) {
						return StatusColor.Red;
					} else {
						return StatusColor.Yellow;
					}
				} else {
					if (successRatio >= params.threshold1) {
						return StatusColor.Green;
					} else if (successRatio >= params.threshold2) {
						return StatusColor.Yellow;
					} else {
						return StatusColor.Red;
					}
				}
			} else {
				switch (jenkinsData.getLastCompletedBuildDetails().getResult()) {
				case FAILURE:
					return StatusColor.Red;
				case SUCCESS:
					return StatusColor.Green;
				case UNSTABLE:
					return StatusColor.Yellow;
				case ABORTED:
				case BUILDING:
				case REBUILDING:
				case UNKNOWN:
				default:
					return StatusColor.Grey;
				}
			}
		}
	}

	private static class Params {
		String host, jobName, label, hyperlinkURL, hyperlinkTarget, fontSize;
		double period;
		boolean showDetails, doesReflectTest, simpleThresholdModel,
				inclSkippedTests, applyOutlineStyle,
				showFailedTestDetailsAsTooltip;
		double threshold1, threshold2;
	}

}
