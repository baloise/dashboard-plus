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
import com.baloise.confluence.dashboardplus.neo4jteststatus.TestResultEvaluationTable;

public class Neo4jTestStatusBaloiseMacro extends AbstractDPlusMacro {

  public Neo4jTestStatusBaloiseMacro(Renderer renderer, UserAccessor userAccessor,
      FormatSettingsManager formatSettingsManager, LocaleManager localeManager, I18nResolver i18n) {
    super(renderer, userAccessor, formatSettingsManager, localeManager, i18n);
  }

  public String debug(String query) {
    return "No debugging implemented: " + query;
  }

  public String execute(Map<String, String> parameters, String body, ConversionContext context)
      throws MacroExecutionException {
    String runnames = getString(parameters.get("runnames"));
    String criterias = getString(parameters.get("criterias"));
    String from = getString(parameters.get("from"));
    String until = getString(parameters.get("until"));
    if (runnames.equalsIgnoreCase("debug")) {
      return renderer.render(debug(criterias), context);
    }
    double greenIfEqualOrAbove = getDouble(parameters.get("greenifequalorabove"), 100.0) / 100.0;
    double redIfEqualOrBelow = getDouble(parameters.get("redifequalorbelow"), 99.9) / 100.0;
    try {
      String label = getString(parameters.get("label")).trim();
      String xAxis = getString(parameters.get("xaxis"));
      String yAxis = getString(parameters.get("yaxis"));
      if (xAxis.isEmpty() || yAxis.isEmpty()) {
        TestResultEvaluation tre = new TestResultEvaluation(runnames, criterias, from, until, greenIfEqualOrAbove,
            redIfEqualOrBelow);
        return renderer.render(tre.asConfluenceMarkup(label), context);
      }
      else {
        TestResultEvaluationTable tret = new TestResultEvaluationTable(runnames, criterias, from, until,
            greenIfEqualOrAbove, redIfEqualOrBelow, xAxis, yAxis);
        return renderer.render(tret.asConfluenceMarkup(label), context);
      }
    }
    catch (Exception e) {
      return renderer.render("Exception occured: " + e.getMessage(), context);
    }
  }

  private double getDouble(String s, double defaultValue) {
    s = getString(s);
    double result = defaultValue;
    try {
      result = Double.parseDouble(s);
    }
    catch (Exception e) {}
    return result;
  }

  private String getString(String s) {
    if (s == null) {
      return "";
    }
    return s.trim();
  }

}
