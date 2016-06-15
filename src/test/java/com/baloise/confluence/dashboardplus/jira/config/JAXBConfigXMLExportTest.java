package com.baloise.confluence.dashboardplus.jira.config;

import org.junit.Test;

import com.baloise.confluence.dashboardplus.jira.config.gen.JiraEvalSpec;
import com.baloise.confluence.dashboardplus.jira.config.gen.JiraEvalSpec.DefaultProjectSpec;
import com.baloise.confluence.dashboardplus.jira.config.gen.JiraEvalSpec.SpecificProjectSpec;

public class JAXBConfigXMLExportTest {

	@Test
	public void testConfigXStreamSerializationXMLPretty() throws Exception {
		JiraEvalSpec jes = new JiraEvalSpec();
		DefaultProjectSpec dps = new DefaultProjectSpec();
		//		dps.set
		jes.setDefaultProjectSpec(dps);
		SpecificProjectSpec sps = new SpecificProjectSpec();
		sps.setProjectKey("GALRE");

		jes.getSpecificProjectSpec().add(sps);

	}

}
