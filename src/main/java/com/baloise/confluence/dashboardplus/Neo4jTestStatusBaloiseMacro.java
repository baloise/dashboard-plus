package com.baloise.confluence.dashboardplus;

import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.confluence.core.FormatSettingsManager;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.message.I18nResolver;
import com.baloise.confluence.dashboardplus.neo4jteststatus.TestResultEvaluation;
import com.baloise.confluence.dashboardplus.neo4jteststatus.TestResultEvaluation.Status;
import com.baloise.confluence.dashboardplus.neo4jteststatus.TestResultEvaluation.TestResult;

public class Neo4jTestStatusBaloiseMacro extends AbstractDPlusMacro {

	public Neo4jTestStatusBaloiseMacro(Renderer renderer,
			UserAccessor userAccessor,
			FormatSettingsManager formatSettingsManager,
			LocaleManager localeManager, I18nResolver i18n) {
		super(renderer, userAccessor, formatSettingsManager, localeManager,
				i18n);
	}

	private String createToolTip(TestResultEvaluation testResultEval) {
		TestResult testResult = testResultEval.asTestResult();
		String result;
		if (testResult.status == Status.error) {
			result = "Error occured while querying the Neo4j DB!";
			if (testResult.error == null) {
				result += "\nError message: " + testResult.error.getMessage();
			}
			if (testResultEval.getCypher() != null) {
				result += "\nNeo4j Cypher: " + testResultEval.getCypher();
			}
		} else if (testResult.status == Status.unknown) {
			result = "The test status cannot be evaluated properly!";
			if (testResultEval.getCypher() != null) {
				result += "\nNeo4j Cypher: " + testResultEval.getCypher();
			}
			result += "\nPlease contact your test supporter.";
			result += "\n";
		} else {
			String runs = "Runs: ";
			for (String run : testResult.runs) {
				runs = runs + run + ", ";
			}
			result = runs + "\nPassed: " + testResult.totalPassed
					+ ", Failed: " + testResult.totalFailed + ", Skipped: "
					+ testResult.totalSkipped + ", Ignored: "
					+ testResult.totalIgnored;
		}
		return result;
	}

	public String debug(String query) {
		return "No debugging implemented: " + query;
	}

	public String execute(Map<String, String> parameters, String body,
			ConversionContext context) throws MacroExecutionException {
		String runnames = getString(parameters.get("runnames"));
		String criterias = getString(parameters.get("criterias"));
		if (runnames.equalsIgnoreCase("debug")) {
			return renderer.render(debug(criterias), context);
		}
		double greenIfEqualOrAbove = getDouble(
				parameters.get("greenifequalorabove"), 100.0) / 100.0;
		double redIfEqualOrBelow = getDouble(
				parameters.get("redifequalorbelow"), 99.9) / 100.0;
		try {
			TestResultEvaluation tre = new TestResultEvaluation(runnames,
					criterias, greenIfEqualOrAbove, redIfEqualOrBelow);
			TestResult tr = tre.asTestResult();
			String result = "";
			String tooltip = createToolTip(tre);
			switch (tr.status) {
			case unknown:
				result = "question";
				break;
			case error:
				result = "sad";
				break;
			case red:
				result = "cross";
				break;
			case yellow:
				result = "warning";
				break;
			case green:
				result = "tick";
				break;
			case grey:
				result = "light-off";
				break;
			case orange:
				result = "light-on";
				break;
			default:
				break;
			}
			String label = getString(parameters.get("label")).trim();
			if (!label.isEmpty()) {
				label = " "
						+ label.replace(
								"#t",
								new Long(tr.totalPassed + tr.totalFailed
										+ tr.totalIgnored + tr.totalSkipped)
										.toString())
								.replace("#p",
										new Long(tr.totalPassed).toString())
								.replace("#f",
										new Long(tr.totalFailed).toString())
								.replace("#s",
										new Long(tr.totalSkipped).toString())
								.replace("#i",
										new Long(tr.totalIgnored).toString());
			}
			return renderer
					.render("<span style=\"font-size:0.8em\" title=\""
							+ StringEscapeUtils.escapeHtml4(tooltip)
							+ "\"><ac:emoticon ac:name=\"" + result + "\"/>"
							+ StringEscapeUtils.escapeHtml4(label) + "</span>",
							context);
		} catch (Exception e) {
			return renderer.render("Exception occured: " + e.getMessage(),
					context);
		}
	}

	private double getDouble(String s, double defaultValue) {
		s = getString(s);
		double result = defaultValue;
		try {
			result = Double.parseDouble(s);
		} catch (Exception e) {
		}
		return result;
	}

	private String getString(String s) {
		if (s == null) {
			return "";
		}
		return s.trim();
	}

}
