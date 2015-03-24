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
import com.baloise.confluence.dashboardplus.sonar.SonarService;
import com.baloise.confluence.dashboardplus.sonar.bean.SonarData;

public class SonarTestStatusMacro extends StatusLightBasedMacro {

	private static final String MACRO_PARAM_NAME_HOST = "host"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_RESOURCEID = "resourceId"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_LABEL = "label"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_SIMPLETHRESHOLDMODEL = "simpleThresholdModel"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_THRESHOLD1 = "threshold1"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_THRESHOLD2 = "threshold2"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_PERIOD = "period"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_SHOWDETAILS = "showDetails"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_HYPERLINKURL = "hyperlinkURL"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_INCLSKIPPEDTESTS = "inclSkippedTests"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_HYPERLINKTARGET = "hyperlinkTarget";
	private static final String MACRO_PARAM_NAME_APPLYOUTLINESTYLE = "applyOutlineStyle";

	private static final String MACRO_PARAM_DEFAULT_HOST = Default
			.getString("SonarTestStatusMacro.host"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_RESOURCEID = Default
			.getString("SonarTestStatusMacro.resourceId"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_LABEL = Default
			.getString("SonarTestStatusMacro.label"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_SIMPLETHRESHOLDMODEL = Default
			.getString("SonarTestStatusMacro.simpleThresholdModel"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_THRESHOLD1 = Default
			.getString("SonarTestStatusMacro.threshold1"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_THRESHOLD2 = Default
			.getString("SonarTestStatusMacro.threshold2"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_PERIOD = Default
			.getString("SonarTestStatusMacro.period"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_SHOWDETAILS = Default
			.getString("SonarTestStatusMacro.showDetails"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_HYPERLINKURL = Default
			.getString("SonarTestStatusMacro.hyperlinkURL"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_INCLSKIPPEDTESTS = Default
			.getString("SonarTestStatusMacro.inclSkippedTests"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_HYPERLINKTARGET = Default
			.getString("SonarTestStatusMacro.hyperlinkTarget"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_APPLYOUTLINESTYLE = Default
			.getString("SonarTestStatusMacro.applyOutlineStyle"); //$NON-NLS-1$

	public SonarTestStatusMacro(/* XhtmlContent xhtmlUtils, */
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
			String[] splitMax = params.resourceId.split(" \\[=\\] ");
			String[] splitAdd = params.resourceId.split(" \\[\\+\\] ");

			if (splitAdd.length > 1 && splitMax.length > 1) {
				throw new MacroExecutionException(
						"Use either ' [=] ' or ' [+] ' as job separator for an aggregation, not both");
			}

			String[] split = splitMax;
			if (splitAdd.length > splitMax.length) {
				split = splitAdd;
			}

			SonarData primarySonarData = SonarService
					.createServiceAndFetchData(params.host, split[0]);
			StatusLightData primarySLData = evaluateSonarData(params,
					primarySonarData);

			for (int i = 1; i < split.length; i++) {
				try {
					SonarData secondarySonarData = SonarService
							.createServiceAndFetchData(params.host, split[i]);
					StatusLightData secondarySLData = evaluateSonarData(params,
							secondarySonarData);
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

			populateVeloContext(params, veloContext, primarySonarData,
					primarySLData);

		} catch (ResourceNotFoundException e) {
			veloContext.put(VELO_PARAM_NAME_LABEL, "?"); //$NON-NLS-1$
			veloContext.put(VELO_PARAM_NAME_COLOR, StatusColor.Grey);
			veloContext.put(VELO_PARAM_NAME_HYPERLINK_URL, params.host
					+ "/dashboard/index/" + params.resourceId); //$NON-NLS-1$
			veloContext.put(VELO_PARAM_NAME_SHOWDETAILS, false);
		} catch (ServiceUnavailableException e) {
			veloContext.put(VELO_PARAM_NAME_LABEL, "!"); //$NON-NLS-1$
			veloContext.put(VELO_PARAM_NAME_COLOR, StatusColor.Grey);
			veloContext.put(VELO_PARAM_NAME_HYPERLINK_URL, params.host);
			veloContext.put(VELO_PARAM_NAME_SHOWDETAILS, false);
		}
		veloContext.put(VELO_PARAM_NAME_HYPERLINK_TARGET,
				params.hyperlinkTarget);
		veloContext
				.put(VELO_PARAM_NAME_APPLY_OUTLINE, params.applyOutlineStyle);
		veloContext.put(VELO_PARAM_NAME_SHOWFAILEDTESTDETAILSASTOOLTIP, false);

		String result = renderer.render(VelocityUtils.getRenderedTemplate(
				VELOCITY_TEMPLATE, veloContext), conversionContext);
		return result;
	}

	private StatusLightData evaluateSonarData(Params params, SonarData sonarData) {
		StatusLightData result = new StatusLightData();
		result.setColor(determineStatusColor(params, sonarData));
		result.setLastRunDurationInMillis(sonarData.getLastRunDuration());
		result.setLastRunTimestamp(sonarData.getLastRunDate().getTime());

		result.setTestPassCount(sonarData
				.getTestSuccessCount(params.inclSkippedTests));
		result.setTestTotalCount(sonarData
				.getTestCount(params.inclSkippedTests));
		return result;
	}

	private void populateVeloContext(Params params,
			Map<String, Object> veloContext, SonarData sonarData,
			StatusLightData slData) {
		veloContext.put(VELO_PARAM_NAME_LABEL, params.label != null
				&& params.label.trim().length() > 0 ? params.label : sonarData
				.getResource().getName());
		veloContext.put(VELO_PARAM_NAME_COLOR, slData.getColor());
		if (params.hyperlinkURL.trim().length() > 0) {
			veloContext.put(VELO_PARAM_NAME_HYPERLINK_URL, params.hyperlinkURL);
		} else {
			veloContext.put(VELO_PARAM_NAME_HYPERLINK_URL, params.host
					+ "/dashboard/index/" + sonarData.getResource().getId()); //$NON-NLS-1$
		}
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
						.valueOf(lastRunDateFriendlyFormatted.getArguments()[i]);
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
			testInfo += "/" //$NON-NLS-1$
					+ slData.getTestTotalCount()
					+ " tests (" //$NON-NLS-1$
					+ newPercentFormatter().format(slData.calcSuccessRatio())
					+ ")"; //$NON-NLS-1$
		}
		veloContext.put(VELO_PARAM_NAME_TESTINFO, testInfo);
	}

	private Params extractParams(Map<String, String> parameters)
			throws MacroExecutionException {
		Params params = new Params();
		params.host = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_HOST, MACRO_PARAM_DEFAULT_HOST);
		params.resourceId = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_RESOURCEID, MACRO_PARAM_DEFAULT_RESOURCEID);

		params.label = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_LABEL, MACRO_PARAM_DEFAULT_LABEL);
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
		params.period = parseDoubleParam(
				loadDefaultedParamValue(parameters, MACRO_PARAM_NAME_PERIOD,
						MACRO_PARAM_DEFAULT_PERIOD), -Double.MAX_VALUE,
				Double.MAX_VALUE);

		params.showDetails = Boolean.parseBoolean(loadDefaultedParamValue(
				parameters, MACRO_PARAM_NAME_SHOWDETAILS,
				MACRO_PARAM_DEFAULT_SHOWDETAILS));

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
		return params;
	}

	private StatusColor determineStatusColor(Params params, SonarData sonarData) {
		long howOld = System.currentTimeMillis()
				- sonarData.getLastRunDate().getTime();
		double testSuccessRate = sonarData
				.getTestSuccessRate(params.inclSkippedTests);
		if (params.period > 0 && howOld > (params.period * 3600 * 1000)) {
			return StatusColor.Grey;
		} else if (testSuccessRate < 0d) {
			return StatusColor.Grey;
		} else {
			if (params.simpleThresholdModel) {
				if (testSuccessRate == 1d) {
					return StatusColor.Green;
				} else if (testSuccessRate == 0d) {
					return StatusColor.Red;
				} else {
					return StatusColor.Yellow;
				}
			} else {
				if (testSuccessRate >= params.threshold1) {
					return StatusColor.Green;
				} else if (testSuccessRate >= params.threshold2) {
					return StatusColor.Yellow;
				} else {
					return StatusColor.Red;
				}
			}
		}
	}

	private static class Params {
		String host, resourceId, label, hyperlinkURL, hyperlinkTarget;
		double threshold1, threshold2, period;
		boolean showDetails, simpleThresholdModel, inclSkippedTests,
				applyOutlineStyle;
	}

}
