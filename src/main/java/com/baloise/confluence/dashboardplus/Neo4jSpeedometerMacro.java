package com.baloise.confluence.dashboardplus;

import java.util.Map;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.confluence.core.FormatSettingsManager;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.sal.api.message.I18nResolver;

public class Neo4jSpeedometerMacro extends AbstractDPlusMacro {

	public static final String VELOCITY_TEMPLATE = "com/baloise/confluence/dashboardplus/neo4j-speedometer.vm";

	private static final String MACRO_PARAM_NAME_HOST = "host"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_CYPHER = "cypher"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_WIDTHPIXEL = "widthPixel"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_HEIGHTPIXEL = "heightPixel"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_TOOLTIPTEXT = "tooltipText"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_REFRESHINTERVALINMILLIS = "refreshIntervalInMillis"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_ANIMDURATIONINMILLIS = "animDurationInMillis"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_ANIMEASINGSTYLE = "animEasingStyle"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_HYPERLINKURL = "hyperlinkURL"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_HYPERLINKTARGET = "hyperlinkTarget";

	private static final String MACRO_PARAM_DEFAULT_HOST = DefaultHelper
			.getString("Neo4jSpeedometerMacro.host"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_CYPHER = DefaultHelper
			.getString("Neo4jSpeedometerMacro.cypher"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_WIDTHPIXEL = DefaultHelper
			.getString("Neo4jSpeedometerMacro.widthPixel"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_HEIGHTPIXEL = DefaultHelper
			.getString("Neo4jSpeedometerMacro.heightPixel"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_TOOLTIPTEXT = DefaultHelper
			.getString("Neo4jSpeedometerMacro.tooltipText"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_REFRESHINTERVALINMILLIS = DefaultHelper
			.getString("Neo4jSpeedometerMacro.refreshIntervalInMillis"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_ANIMDURATIONINMILLIS = DefaultHelper
			.getString("Neo4jSpeedometerMacro.animDurationInMillis"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_ANIMEASINGSTYLE = DefaultHelper
			.getString("Neo4jSpeedometerMacro.animEasingStyle"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_HYPERLINKURL = DefaultHelper
			.getString("Neo4jSpeedometerMacro.hyperlinkURL"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_HYPERLINKTARGET = DefaultHelper
			.getString("Neo4jSpeedometerMacro.hyperlinkTarget"); //$NON-NLS-1$

	public Neo4jSpeedometerMacro(Renderer renderer, UserAccessor userAccessor,
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
		veloContext.put("someID", System.nanoTime());
		veloContext.put("neo4jURL", params.host);
		veloContext.put("cypher", params.cypher.replaceAll("'", "\\\\'"));
		veloContext.put("refreshIntervalInMillis",
				params.refreshIntervalInMillis);
		veloContext.put("widthPixel", params.widthPixel);
		veloContext.put("heightPixel", params.heightPixel);
		veloContext.put("tooltipText", params.tooltipText);
		veloContext.put("animDurationInMillis", params.animDurationInMillis);
		veloContext.put("animEasingStyle", params.animEasingStyle);
		veloContext.put("hyperlinkURL", params.hyperlinkURL);
		veloContext.put("hyperlinkTarget", params.hyperlinkTarget);

		String result = renderer.render(VelocityUtils.getRenderedTemplate(
				VELOCITY_TEMPLATE, veloContext), conversionContext);
		return result;
	}

	private Params extractParams(Map<String, String> parameters)
			throws MacroExecutionException {
		Params params = new Params();
		params.host = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_HOST, MACRO_PARAM_DEFAULT_HOST);
		params.cypher = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_CYPHER, MACRO_PARAM_DEFAULT_CYPHER);
		params.widthPixel = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_WIDTHPIXEL, MACRO_PARAM_DEFAULT_WIDTHPIXEL);
		params.heightPixel = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_HEIGHTPIXEL, MACRO_PARAM_DEFAULT_HEIGHTPIXEL);
		params.tooltipText = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_TOOLTIPTEXT, MACRO_PARAM_DEFAULT_TOOLTIPTEXT);
		params.refreshIntervalInMillis = parseLongParam(
				loadDefaultedParamValue(parameters,
						MACRO_PARAM_NAME_REFRESHINTERVALINMILLIS,
						MACRO_PARAM_DEFAULT_REFRESHINTERVALINMILLIS), -1l,
				Long.MAX_VALUE);
		params.animDurationInMillis = parseLongParam(
				loadDefaultedParamValue(parameters,
						MACRO_PARAM_NAME_ANIMDURATIONINMILLIS,
						MACRO_PARAM_DEFAULT_ANIMDURATIONINMILLIS), -1l,
				Long.MAX_VALUE);
		params.animEasingStyle = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_ANIMEASINGSTYLE,
				MACRO_PARAM_DEFAULT_ANIMEASINGSTYLE);
		params.hyperlinkURL = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_HYPERLINKURL, MACRO_PARAM_DEFAULT_HYPERLINKURL);
		params.hyperlinkTarget = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_HYPERLINKTARGET,
				MACRO_PARAM_DEFAULT_HYPERLINKTARGET);
		return params;
	}

	private static class Params {
		String host, cypher, widthPixel, heightPixel, tooltipText,
				animEasingStyle, hyperlinkURL, hyperlinkTarget;
		long refreshIntervalInMillis, animDurationInMillis;
	}
}
