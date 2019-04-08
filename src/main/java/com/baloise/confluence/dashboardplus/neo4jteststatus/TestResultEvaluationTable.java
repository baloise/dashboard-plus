package com.baloise.confluence.dashboardplus.neo4jteststatus;

import com.baloise.confluence.dashboardplus.neo4jteststatus.TestResultEvaluation.TestResult;

public class TestResultEvaluationTable {

  private String runnames;
  private String criterias;
  private String from;
  private String until;
  private double greenIfEqualOrAbove;
  private double redIfEqualOrBelow;
  private String xAxis;
  private String yAxis;

  public TestResultEvaluationTable(String runnames, String criterias, String from, String until,
      double greenIfEqualOrAbove, double redIfEqualOrBelow, String xAxis, String yAxis) {
    this.runnames = runnames;
    this.criterias = criterias;
    this.from = from;
    this.until = until;
    this.greenIfEqualOrAbove = greenIfEqualOrAbove;
    this.redIfEqualOrBelow = redIfEqualOrBelow;
    this.xAxis = xAxis;
    this.yAxis = yAxis;
  }

  public static void main(String[] args) {
    System.out.println(new TestResultEvaluationTable("NLGW9Gpe1T,NLGW9Gpe4T", "", "", "", 0.99, 0.8, "stack", "gevo")
        .asConfluenceMarkup("#t"));
  }

  public String asConfluenceMarkup(String label) {
    String result = "<table><tbody>";
    result = result + "<tr>";
    result = result + "<th>" + yAxis + " --> " + xAxis + "</th>";
    for (String x : getXValues()) {
      result = result + "<th>" + x + "</th>";
    }
    result = result + "</tr>";
    for (String y : getYValues()) {
      result = result + "<tr>";
      result = result + "<td>" + y + "</td>";
      for (String x : getXValues()) {
        result = result + "<td>";
        TestResultEvaluation tre = getTRE(x, y);
        result = result + tre.asConfluenceMarkup(label);
        result = result + "</td>";
      }
      result = result + "</tr>";
    }
    return result + "</tbody></table>";
  }

  private String[] getXValues() {
    TestResultEvaluation tre = new TestResultEvaluation(runnames, criterias, from, until, greenIfEqualOrAbove,
        redIfEqualOrBelow);
    return tre.getSortedValuesFor(xAxis);
  }

  private String[] getYValues() {
    TestResultEvaluation tre = new TestResultEvaluation(runnames, criterias, from, until, greenIfEqualOrAbove,
        redIfEqualOrBelow);
    return tre.getSortedValuesFor(yAxis);
  }

  private TestResultEvaluation getTRE(String xValue, String yValue) {
    String additionalCriteria = xAxis + "=" + xValue + "," + yAxis + "=" + yValue;
    return new TestResultEvaluation(runnames, criterias + additionalCriteria, from, until, greenIfEqualOrAbove,
        redIfEqualOrBelow);
  }
}
