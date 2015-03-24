package com.baloise.confluence.dashboardplus;

import com.baloise.confluence.dashboardplus.StatusLightBasedMacro.StatusColor;

public class StatusLightData {

	private int testPassCount, testTotalCount;
	private long lastRunDurationInMillis, lastRunTimestamp;
	private StatusColor color;
	private String testDetails = "";

	public void aggregateWith(StatusLightData data, boolean add) {
		if (add) {
			testPassCount += data.getTestPassCount();
			testTotalCount += data.getTestTotalCount();
			lastRunDurationInMillis += data.getLastRunDurationInMillis();
		} else {
			testPassCount = Math.max(testPassCount, data.getTestPassCount());
			testTotalCount = Math.max(testTotalCount, data.getTestTotalCount());
			lastRunDurationInMillis = Math.max(lastRunDurationInMillis,
					data.getLastRunDurationInMillis());
		}
		color = StatusColor.values()[Math.max(color.ordinal(), data.getColor()
				.ordinal())];
		lastRunTimestamp = Math.max(lastRunTimestamp,
				data.getLastRunTimestamp());
		if (data.testDetails != null) {
			testDetails += data.testDetails;
		}
	}

	public double calcSuccessRatio() {
		return ((double) testPassCount) / ((double) testTotalCount);
	}

	public int getTestPassCount() {
		return testPassCount;
	}

	public void setTestPassCount(int testPassCount) {
		this.testPassCount = testPassCount;
	}

	public int getTestTotalCount() {
		return testTotalCount;
	}

	public void setTestTotalCount(int testTotalCount) {
		this.testTotalCount = testTotalCount;
	}

	public long getLastRunDurationInMillis() {
		return lastRunDurationInMillis;
	}

	public void setLastRunDurationInMillis(long lastRunDurationInMillis) {
		this.lastRunDurationInMillis = lastRunDurationInMillis;
	}

	public long getLastRunTimestamp() {
		return lastRunTimestamp;
	}

	public void setLastRunTimestamp(long lastRunTimestamp) {
		this.lastRunTimestamp = lastRunTimestamp;
	}

	public StatusColor getColor() {
		return color;
	}

	public void setColor(StatusColor color) {
		this.color = color;
	}

	public String getTestDetails() {
		return testDetails;
	}

	public void setTestDetails(String testDetails) {
		this.testDetails = testDetails;
	}

}
