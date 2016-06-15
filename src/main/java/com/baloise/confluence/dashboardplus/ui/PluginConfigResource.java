package com.baloise.confluence.dashboardplus.ui;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.baloise.confluence.dashboardplus.DefaultHelper;

@Path("/PluginConfig")
public class PluginConfigResource {
	private static Logger log = LoggerFactory
			.getLogger(PluginConfigResource.class);

	private final UserManager userManager;
	private final PluginSettingsFactory pluginSettingsFactory;
	private final TransactionTemplate transactionTemplate;

	public PluginConfigResource(UserManager userManager,
			PluginSettingsFactory pluginSettingsFactory,
			TransactionTemplate transactionTemplate) {
		this.userManager = userManager;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context HttpServletRequest request) {
		UserProfile user = userManager.getRemoteUser(request);
		log.warn("HALLO");
		if (user == null || !userManager.isSystemAdmin(user.getUserKey())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		final String defaultJiraEvalSpec;
		try {
			defaultJiraEvalSpec = DefaultHelper.getDefaultJiraEvalSpec();
		} catch (IOException e) {
			log.warn(
					"Unexpected error occurred while loading the default Jira Eval Specification (XML) from the dashboard-plus plugin",
					e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		}

		return Response.ok(
				transactionTemplate
						.execute(new TransactionCallback<PluginConfig>() {
							public PluginConfig doInTransaction() {
								PluginSettings settings = pluginSettingsFactory
										.createGlobalSettings();
								PluginConfig config = new PluginConfig();
								//								config.setGlobalJiraEvalSpec((String) settings
								//										.get(PluginConfig.class.getName()
								//												+ ".globalJiraEvalSpec"));
								config.setGlobalJiraEvalSpec(defaultJiraEvalSpec);
								return config;
							}
						})).build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response put(final PluginConfig config,
			@Context HttpServletRequest request) {
		if (true) {
			return Response.notAcceptable(null).build();
		}
		UserProfile user = userManager.getRemoteUser(request);
		if (user == null || !userManager.isSystemAdmin(user.getUserKey())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		transactionTemplate.execute(new TransactionCallback<Void>() {
			public Void doInTransaction() {
				PluginSettings pluginSettings = pluginSettingsFactory
						.createGlobalSettings();
				pluginSettings.put(PluginConfig.class.getName()
						+ ".globalJiraEvalSpec", config.getGlobalJiraEvalSpec());
				return null;
			}
		});
		return Response.noContent().build();
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static final class PluginConfig {
		@XmlElement
		private String globalJiraEvalSpec;

		public String getGlobalJiraEvalSpec() {
			return globalJiraEvalSpec;
		}

		public void setGlobalJiraEvalSpec(String globalJiraEvalSpec) {
			this.globalJiraEvalSpec = globalJiraEvalSpec;
		}

	}
}