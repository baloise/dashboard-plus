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

public class StaticStatusLightMacro extends StatusLightBasedMacro {

	private static final String MACRO_PARAM_NAME_LABEL = "label"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_COLOR = "color"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_HYPERLINKURL = "hyperlinkURL"; //$NON-NLS-1$
	private static final String MACRO_PARAM_NAME_HYPERLINKTARGET = "hyperlinkTarget";
	private static final String MACRO_PARAM_NAME_APPLYOUTLINESTYLE = "applyOutlineStyle";

	private static final String MACRO_PARAM_DEFAULT_LABEL = Default
			.getString("StaticStatusLightMacro.label"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_COLOR = Default
			.getString("StaticStatusLightMacro.color"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_HYPERLINKURL = Default
			.getString("StaticStatusLightMacro.hyperlinkURL"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_HYPERLINKTARGET = Default
			.getString("StaticStatusLightMacro.hyperlinkTarget"); //$NON-NLS-1$
	private static final String MACRO_PARAM_DEFAULT_APPLYOUTLINESTYLE = Default
			.getString("StaticStatusLightMacro.applyOutlineStyle"); //$NON-NLS-1$

	public StaticStatusLightMacro(/* XhtmlContent xhtmlUtils, */
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
		veloContext.put(VELO_PARAM_NAME_LABEL, params.label); //$NON-NLS-1$
		veloContext.put(VELO_PARAM_NAME_COLOR,
				StatusColor.valueOf(params.color));
		veloContext.put(VELO_PARAM_NAME_HYPERLINK_URL, params.hyperlinkURL);
		veloContext.put(VELO_PARAM_NAME_HYPERLINK_TARGET,
				params.hyperlinkTarget);
		veloContext
				.put(VELO_PARAM_NAME_APPLY_OUTLINE, params.applyOutlineStyle);
		veloContext.put(VELO_PARAM_NAME_SHOWDETAILS, false);
		veloContext.put(VELO_PARAM_NAME_SHOWFAILEDTESTDETAILSASTOOLTIP, false);

		String result = renderer.render(VelocityUtils.getRenderedTemplate(
				VELOCITY_TEMPLATE, veloContext), conversionContext);

		return result;
	}

	private Params extractParams(Map<String, String> parameters)
			throws MacroExecutionException {
		Params params = new Params();
		params.color = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_COLOR, MACRO_PARAM_DEFAULT_COLOR);
		params.label = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_LABEL, MACRO_PARAM_DEFAULT_LABEL);
		params.hyperlinkURL = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_HYPERLINKURL, MACRO_PARAM_DEFAULT_HYPERLINKURL);
		params.hyperlinkTarget = loadDefaultedParamValue(parameters,
				MACRO_PARAM_NAME_HYPERLINKTARGET,
				MACRO_PARAM_DEFAULT_HYPERLINKTARGET);
		params.applyOutlineStyle = Boolean
				.parseBoolean(loadDefaultedParamValue(parameters,
						MACRO_PARAM_NAME_APPLYOUTLINESTYLE,
						MACRO_PARAM_DEFAULT_APPLYOUTLINESTYLE));
		return params;
	}

	private static class Params {
		String label, color, hyperlinkURL, hyperlinkTarget;
		boolean applyOutlineStyle;
	}

}
