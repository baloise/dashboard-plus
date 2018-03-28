/*
 * Copyright (c) 2013 Cosmin Stejerean, Karl Heinz Marbaise, and contributors.
 *
 * Distributed under the MIT license: http://opensource.org/licenses/MIT
 */

package com.offbytwo.jenkins;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.ContentType;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.client.util.EncodingUtils;
import com.offbytwo.jenkins.helper.JenkinsVersion;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.Computer;
import com.offbytwo.jenkins.model.ComputerSet;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobConfiguration;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.LabelWithDetails;
import com.offbytwo.jenkins.model.MainView;
import com.offbytwo.jenkins.model.MavenJobWithDetails;
import com.offbytwo.jenkins.model.PluginManager;
import com.offbytwo.jenkins.model.Queue;
import com.offbytwo.jenkins.model.QueueItem;
import com.offbytwo.jenkins.model.QueueReference;
import com.offbytwo.jenkins.model.View;

/**
 * The main starting point for interacting with a Jenkins server.
 */
public class JenkinsServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsServer.class);

    private final JenkinsHttpClient client;

    /**
     * Create a new Jenkins server reference given only the server address
     *
     * @param serverUri address of jenkins server (ex.
     *            http://localhost:8080/jenkins)
     */
    public JenkinsServer(URI serverUri) {
        this(new JenkinsHttpClient(serverUri));
    }

    /**
     * Create a new Jenkins server reference given the address and credentials
     *
     * @param serverUri address of jenkins server (ex.
     *            http://localhost:8080/jenkins)
     * @param username username to use when connecting
     * @param passwordOrToken password (not recommended) or token (recommended)
     */
    public JenkinsServer(URI serverUri, String username, String passwordOrToken) {
        this(new JenkinsHttpClient(serverUri, username, passwordOrToken));
    }

    /**
     * Create a new Jenkins server directly from an HTTP client (ADVANCED)
     *
     * @param client Specialized client to use.
     */
    public JenkinsServer(JenkinsHttpClient client) {
        this.client = client;
    }

	public static String encode(String path) {
		// seems Jenkins prefers some characters in ASCII representation, especially / and : that are encountered in case of Maven multi-module projects
		String result  = path;
		try {
			result = URLEncoder.encode(path, "UTF-8").replaceAll("\\+", "%20")
					.replaceAll("\\%2F", "/").replaceAll("\\%3A", ":");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Encoding UTF-8 seems to be unsupported.", e);
		}
		return result;
	}
    
    /**
     * Get the current status of the Jenkins end-point by pinging it.
     *
     * @return true if Jenkins is up and running, false otherwise
     */
    public boolean isRunning() {
        try {
            client.get("/");
            return true;
        } catch (IOException e) {
            LOGGER.debug("isRunning()", e);
            return false;
        }
    }

    /**
     * @return {@link JenkinsVersion}
     */
    public JenkinsVersion getVersion() {
        if (client.getJenkinsVersion().isEmpty()) {
            // Force a request to get at least once
            // HttpHeader
            isRunning();
        }
        JenkinsVersion jv = new JenkinsVersion(client.getJenkinsVersion());
        return jv;
    }

    /**
     * Get a list of all the defined jobs on the server (at the summary level)
     *
     * @return list of defined jobs (summary level, for details @see Job#details
     * @throws IOException in case of an error.
     */
    public Map<String, Job> getJobs() throws IOException {
        return getJobs(null, null);
    }

    /**
     * Get a list of all the defined jobs on the server (in the given folder)
     *
     * @param folder {@link FolderJob}
     * @return list of defined jobs (summary level, for details @see Job#details
     * @throws IOException in case of an error.
     */
    public Map<String, Job> getJobs(FolderJob folder) throws IOException {
        return getJobs(folder, null);
    }

    /**
     * Get a list of all the defined jobs on the server (at the specified view
     * level)
     *
     * @param view The view to get jobs from.
     * @return list of defined jobs (view level, for details @see Job#details
     * @throws IOException in case of an error.
     */
    public Map<String, Job> getJobs(String view) throws IOException {
        return getJobs(null, view);
    }

    /**
     * Get a list of all the defined jobs on the server (at the specified view
     * level and in the specified folder)
     * 
     * @param folder {@link FolderJob}
     * @param view The view to use.
     * @return list of defined jobs (view level, for details @see Job#details
     * @throws IOException in case of an error.
     */
    public Map<String, Job> getJobs(FolderJob folder, String view) throws IOException {
        String path = toBaseUrl(folder);
        Class<? extends MainView> viewClass = MainView.class;
        if (view != null) {
            path = path + "view/" + EncodingUtils.encode(view) + "/";
            viewClass = View.class;
        }
        List<Job> jobs = client.get(path, viewClass).getJobs();
        return Maps.uniqueIndex(jobs, new Function<Job, String>() {
            @Override
            public String apply(Job job) {
                job.setClient(client);
                return job.getName();
            }
        });
    }

    /**
     * Get a list of all the defined views on the server (at the summary level)
     *
     * @return list of defined views
     * @throws IOException in case of an error.
     */
    public Map<String, View> getViews() throws IOException {
        return getViews(null);
    }

    /**
     * Get a list of all the defined views on the server (at the summary level
     * and in the given folder)
     * 
     * @param folder {@link FolderJob}
     * @return list of defined views
     * @throws IOException in case of an error.
     */
    public Map<String, View> getViews(FolderJob folder) throws IOException {
        List<View> views = client.get(toBaseUrl(folder) + "?depth=1", MainView.class).getViews();
        return Maps.uniqueIndex(views, new Function<View, String>() {
            @Override
            public String apply(View view) {

                view.setClient(client);
                // TODO: Think about the following? Does there exists a
                // simpler/more elegant method?
                for (Job job : view.getJobs()) {
                    job.setClient(client);
                }
                for (View item : view.getViews()) {
                    item.setClient(client);
                }

                // return view.getName().toLowerCase();
                return view.getName();
            }
        });
    }

    /**
     * Get a single view object from the server
     *
     * @param name name of the view in Jenkins
     * @return the view object
     * @throws IOException in case of an error.
     */
    public View getView(String name) throws IOException {
        return getView(null, name);
    }

    /**
     * Get a single view object from the given folder
     * 
     * @param folder The name of the folder.
     * @param name name of the view in Jenkins
     * @return the view object
     * @throws IOException in case of an error.
     */
    public View getView(FolderJob folder, String name) throws IOException {
        try {
            View resultView = client.get(toViewBaseUrl(folder, name) + "/", View.class);
            resultView.setClient(client);

            // TODO: Think about the following? Does there exists a simpler/more
            // elegant method?
            for (Job job : resultView.getJobs()) {
                job.setClient(client);
            }
            for (View view : resultView.getViews()) {
                view.setClient(client);
            }
            return resultView;
        } catch (HttpResponseException e) {
            LOGGER.debug("getView(folder={}, name={}) status={}", folder, name, e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                // TODO: Think hard about this.
                return null;
            }
            throw e;
        }
    }

    /**
     * Get a single Job from the server.
     * 
     * @param jobName name of the job to get details of.
     * @return A single Job, null if not present
     * @throws IOException in case of an error.
     */
    public JobWithDetails getJob(String jobName) throws IOException {
        return getJob(null, jobName);
    }

    /**
     * Get a single Job from the given folder.
     * 
     * @param folder {@link FolderJob}
     * @param jobName name of the job to get details of.
     * @return A single Job, null if not present
     * @throws IOException in case of an error.
     */
    public JobWithDetails getJob(FolderJob folder, String jobName) throws IOException {
        try {
            JobWithDetails job = client.get(toJobBaseUrl(folder, jobName), JobWithDetails.class);
            job.setClient(client);

            return job;
        } catch (HttpResponseException e) {
            LOGGER.debug("getJob(folder={}, jobName={}) status={}", folder, jobName, e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                // TODO: Think hard about this.
                return null;
            }
            throw e;
        }
    }

    public MavenJobWithDetails getMavenJob(String jobName) throws IOException {
        return getMavenJob(null, jobName);
    }

    public MavenJobWithDetails getMavenJob(FolderJob folder, String jobName) throws IOException {
        try {
            MavenJobWithDetails job = client.get(toJobBaseUrl(folder, jobName), MavenJobWithDetails.class);
            job.setClient(client);

            return job;
        } catch (HttpResponseException e) {
            LOGGER.debug("getMavenJob(jobName={}) status={}", jobName, e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    public Optional<FolderJob> getFolderJob(Job job) throws IOException {
        try {
            FolderJob folder = client.get(job.getUrl(), FolderJob.class);
            if (!folder.isFolder()) {
                return Optional.absent();
            }
            folder.setClient(client);

            return Optional.of(folder);
        } catch (HttpResponseException e) {
            LOGGER.debug("getForlderJob(job={}) status={}", job, e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                // TODO: Check if this is a good idea ? What about
                // Optional.absent() ?
                return null;
            }
            throw e;
        }
    }

    /**
     * Create a job on the server using the provided xml
     * 
     * @param jobName name of the job to be created.
     * @param jobXml the <code>config.xml</code> which should be used to create
     *            the job.
     * @throws IOException in case of an error.
     */
    public void createJob(String jobName, String jobXml) throws IOException {
        createJob(null, jobName, jobXml, false);
    }

    /**
     * Create a job on the server using the provided xml
     * 
     * @param jobName name of the job to be created.
     * @param jobXml the <code>config.xml</code> which should be used to create
     *            the job.
     * @param crumbFlag <code>true</code> to add <b>crumbIssuer</b>
     *            <code>false</code> otherwise.
     * @throws IOException in case of an error.
     */
    public void createJob(String jobName, String jobXml, Boolean crumbFlag) throws IOException {
        createJob(null, jobName, jobXml, crumbFlag);
    }

    /**
     * Create a job on the server using the provided xml and in the provided
     * folder
     *
     * @param folder {@link FolderJob}
     * @param jobName name of the job to be created.
     * @param jobXml the <code>config.xml</code> which should be used to create
     *            the job.
     * @throws IOException in case of an error.
     */
    public void createJob(FolderJob folder, String jobName, String jobXml) throws IOException {
        createJob(folder, jobName, jobXml, false);
    }

    /**
     * Create a job on the server using the provided xml and in the provided
     * folder
     *
     * @param folder {@link FolderJob}
     * @param jobName name of the job to be created.
     * @param jobXml the <code>config.xml</code> which should be used to create
     *            the job.
     * @param crumbFlag <code>true</code> to add <b>crumbIssuer</b>
     *            <code>false</code> otherwise.
     * @throws IOException in case of an error.
     */
    public void createJob(FolderJob folder, String jobName, String jobXml, Boolean crumbFlag) throws IOException {
        client.post_xml(toBaseUrl(folder) + "createItem?name=" + EncodingUtils.encodeParam(jobName), jobXml, crumbFlag);
    }

    /**
     * Create a view on the server using the provided xml
     * 
     * @param viewName name of the view to be created.
     * @param viewXml The configuration for the view.
     * @throws IOException in case of an error.
     */
    public void createView(String viewName, String viewXml) throws IOException {
        createView(null, viewName, viewXml, false);
    }

    /**
     * Create a view on the server using the provided xml.
     *
     * @param viewName name of the view to be created.
     * @param viewXml The configuration for the view.
     * @param crumbFlag <code>true</code> to add <b>crumbIssuer</b>
     *            <code>false</code> otherwise.
     * @throws IOException in case of an error.
     */
    public void createView(String viewName, String viewXml, Boolean crumbFlag) throws IOException {
        createView(null, viewName, viewXml, crumbFlag);
    }

    /**
     * Create a view on the server using the provided xml and in the provided
     * folder.
     * 
     * @param folder {@link FolderJob}
     * @param viewName name of the view to be created.
     * @param viewXml The configuration for the view.
     * @throws IOException in case of an error.
     */
    public void createView(FolderJob folder, String viewName, String viewXml) throws IOException {
        createView(folder, viewName, viewXml, false);
    }

    /**
     * Create a view on the server using the provided xml and in the provided
     * folder.
     * 
     * @param folder the folder.
     * @param viewName the view name.
     * @param viewXml the view xml.
     * @param crumbFlag <code>true</code> to add <b>crumbIssuer</b>
     *            <code>false</code> otherwise.
     * @throws IOException in case of an error.
     */
    public void createView(FolderJob folder, String viewName, String viewXml, Boolean crumbFlag) throws IOException {
        client.post_xml(toBaseUrl(folder) + "createView?name=" + EncodingUtils.encodeParam(viewName), viewXml,
                crumbFlag);
    }

    /**
     * Create a folder on the server (in the root)
     * 
     * @param folderName name of the folder.
     * @throws IOException in case of an error.
     */
    public void createFolder(String folderName) throws IOException {
        createFolder(null, folderName, false);
    }

    /**
     * Create a folder on the server (in the root)
     * 
     * @param folderName name of the folder.
     * @param crumbFlag <code>true</code> to add <b>crumbIssuer</b>
     *            <code>false</code> otherwise.
     * @throws IOException in case of an error.
     */
    public void createFolder(String folderName, Boolean crumbFlag) throws IOException {
        createFolder(null, folderName, crumbFlag);
    }

    /**
     * Create a job on the server (in the given folder)
     *
     * @param folder {@link FolderJob}
     * @param jobName name of the job.
     * @throws IOException in case of an error.
     */
    public void createFolder(FolderJob folder, String jobName) throws IOException {
        createFolder(folder, jobName, false);
    }

    /**
     * Create a job on the server (in the given folder)
     *
     * @param folder {@link FolderJob}
     * @param jobName name of the job.
     * @param crumbFlag <code>true</code> to add <b>crumbIssuer</b>
     *            <code>false</code> otherwise.
     * @throws IOException in case of an error.
     */
    public void createFolder(FolderJob folder, String jobName, Boolean crumbFlag) throws IOException {
        // https://gist.github.com/stuart-warren/7786892 was slightly helpful
        // here
        ImmutableMap<String, String> params = ImmutableMap.of("mode", "com.cloudbees.hudson.plugins.folder.Folder",
                "name", EncodingUtils.encodeParam(jobName), "from", "", "Submit", "OK");
        client.post_form(toBaseUrl(folder) + "createItem?", params, crumbFlag);
    }

    /**
     * Get the xml description of an existing job
     *
     * @param jobName name of the job.
     * @return the new job object
     * @throws IOException in case of an error.
     */
    public String getJobXml(String jobName) throws IOException {
        return getJobXml(null, jobName);
    }

    /**
     * Get the xml description of an existing job.
     * 
     * @param jobName name of the job.
     * @param folder {@link FolderJob}
     * @return the new job object
     * @throws IOException in case of an error.
     */
    public String getJobXml(FolderJob folder, String jobName) throws IOException {
        return client.get(toJobBaseUrl(folder, jobName) + "/config.xml");
    }

    /**
     * Get the description of an existing Label
     * 
     * @param labelName name of the label.
     * @return {@link LabelWithDetails}
     * @throws IOException in case of an error.
     */
    public LabelWithDetails getLabel(String labelName) throws IOException {
        return client.get("/label/" + EncodingUtils.encode(labelName), LabelWithDetails.class);
    }

    /**
     * Get a list of all the computers on the server (at the summary level)
     *
     * @return map of defined computers (summary level, for details @see
     *         Computer#details
     * @throws IOException in case of an error.
     */
    public Map<String, Computer> getComputers() throws IOException {
        List<Computer> computers = client.get("computer/", Computer.class).getComputers();
        return Maps.uniqueIndex(computers, new Function<Computer, String>() {
            @Override
            public String apply(Computer computer) {
                computer.setClient(client);
                return computer.getDisplayName().toLowerCase();
            }
        });
    }

    /**
     * The ComputerSet class will give informations like
     * {@link ComputerSet#getBusyExecutors()} or the
     * {@link ComputerSet#getTotalExecutors()}.
     * 
     * @return {@link ComputerSet}
     * @throws IOException in case of an error.
     */
    public ComputerSet getComputerSet() throws IOException {
        return client.get("computer/?depth=2", ComputerSet.class);
    }

    /**
     * This will give you back the {@link PluginManager}.
     * 
     * @return {@link PluginManager}
     * @throws IOException in case of a failure.
     */
    public PluginManager getPluginManager() throws IOException {
        return client.get("pluginManager/?depth=2", PluginManager.class);
    }

    /**
     * Update the xml description of an existing view
     *
     * @throws IOException in case of an error.
     * @param viewName name of the view.
     * @param viewXml the view configuration.
     * @throws IOException in case of an error.
     */
    public void updateView(String viewName, String viewXml) throws IOException {
        this.updateView(viewName, viewXml, true);
    }

    public void updateView(String viewName, String viewXml, boolean crumbFlag) throws IOException {
        client.post_xml("/view/" + EncodingUtils.encode(viewName) + "/config.xml", viewXml, crumbFlag);
    }

    /**
     * Update the xml description of an existing job
     *
     * @param jobName name of the job to be updated.
     * @param jobXml the configuration to be used for updating.
     * @throws IOException in case of an error.
     */
    public void updateJob(String jobName, String jobXml) throws IOException {
        this.updateJob(jobName, jobXml, true);
    }

    /**
     * Update the xml description of an existing job
     *
     * @param jobName name of the job to be updated.
     * @param jobXml the configuration to be used for updating.
     * @param crumbFlag true/false.
     * @throws IOException in case of an error.
     */
    public void updateJob(String jobName, String jobXml, boolean crumbFlag) throws IOException {
        updateJob(null, jobName, jobXml, crumbFlag);
    }

    /**
     * Update the xml description of an existing job
     *
     * @param folder the folder.
     * @param jobName the name of the job.
     * @param jobXml the job xml configuration.
     * @param crumbFlag true/false.
     * @throws IOException in case of an error.
     */
    public void updateJob(FolderJob folder, String jobName, String jobXml, boolean crumbFlag) throws IOException {
        client.post_xml(toJobBaseUrl(folder, jobName) + "/config.xml", jobXml, crumbFlag);
    }

    /**
     * @param jobName name of the job.
     * @param name name of the parameter.
     * @param description of the parameters.
     * @param defaultValue the defaultValue for the parameters.
     * @throws IOException in case of an error.
     * @throws JAXBException in case of an error.
     * @throws DocumentException in case of an error.
     */
    public void addStringParam(String jobName, String name, String description, String defaultValue)
            throws IOException, JAXBException, DocumentException {
        String jobXml = this.getJobXml(jobName);
        JobConfiguration jobConf = new JobConfiguration(jobXml);
        jobXml = jobConf.addStringParam(name, description, defaultValue).asXml();
        this.updateJob(jobName, jobXml);
    }

    /**
     * Sends the Quiet Down (Prepare for shutdown) message
     * 
     * @throws IOException in case of an error.
     */
    public void quietDown() throws IOException {
        try {
            client.get("/quietDown/");
        } catch (org.apache.http.client.ClientProtocolException e) {
            LOGGER.error("quietDown()", e);
        }

    }

    /**
     * Cancels the Quiet Down (Prepare for shutdown) message
     * 
     * @throws IOException in case of an error.
     */
    public void cancelQuietDown() throws IOException {
        try {
            client.post("/cancelQuietDown/");
        } catch (org.apache.http.client.ClientProtocolException e) {
            LOGGER.error("cancelQuietDown()", e);
        }
    }

    /**
     * Delete a job from Jenkins within a folder.
     *
     * @param folder The folder where the given job is located.
     * @param jobName The job which should be deleted.
     *
     * @throws IOException in case of an error.
     */
    public void deleteJob(FolderJob folder, String jobName) throws IOException {
        deleteJob(folder, jobName, false);
    }

    /**
     * Delete a job from Jenkins within a folder.
     * 
     * @param folder The folder where the given job is located.
     * @param jobName The job which should be deleted.
     * @param crumbFlag The crumbFlag
     * @throws IOException in case of problems.
     */
    public void deleteJob(FolderJob folder, String jobName, boolean crumbFlag) throws IOException {
        client.post(toJobBaseUrl(folder, jobName) + "/doDelete", crumbFlag);
    }

    /**
     * Delete a job from jenkins
     * 
     * @param jobName The name of the job which should be deleted.
     * @throws IOException in case of an error.
     */
    public void deleteJob(String jobName) throws IOException {
        client.post("/job/" + EncodingUtils.encode(jobName) + "/doDelete");
    }

    /**
     * Delete a job from Jenkins.
     * 
     * @param jobName The name of the job to be deleted.
     * @param crumbFlag <code>true</code> to add <b>crumbIssuer</b>
     *            <code>false</code> otherwise.
     * @throws IOException In case of an failure.
     */
    public void deleteJob(String jobName, boolean crumbFlag) throws IOException {
        client.post("/job/" + EncodingUtils.encode(jobName) + "/doDelete", crumbFlag);
    }

    /**
     * Disable a job from jenkins
     * 
     * @param jobName The name of the job which should be disabled.
     * @throws IOException in case of an error.
     */
    public void disableJob(String jobName) throws IOException {
        client.post("/job/" + EncodingUtils.encode(jobName) + "/disable");
    }

    /**
     * Disable a job from Jenkins.
     *
     * @param jobName The name of the job to be deleted.
     * @param crumbFlag <code>true</code> to add <b>crumbIssuer</b>
     *            <code>false</code> otherwise.
     * @throws IOException In case of an failure.
     */
    public void disableJob(String jobName, boolean crumbFlag) throws IOException {
        client.post("/job/" + EncodingUtils.encode(jobName) + "/disable", crumbFlag);
    }

    /**
     * Enable a job from jenkins
     * 
     * @param jobName name of the job which should be enabled.
     * @throws IOException In case of an failure.
     */
    public void enableJob(String jobName) throws IOException {
        client.post("/job/" + EncodingUtils.encode(jobName) + "/enable");
    }

    /**
     * Enable a job from Jenkins.
     *
     * @param jobName The name of the job to be deleted.
     * @param crumbFlag <code>true</code> to add <b>crumbIssuer</b>
     *            <code>false</code> otherwise.
     * @throws IOException In case of an failure.
     */
    public void enableJob(String jobName, boolean crumbFlag) throws IOException {
        client.post("/job/" + EncodingUtils.encode(jobName) + "/enable", crumbFlag);
    }

    /**
     * Runs the provided groovy script on the server and returns the result.
     *
     * This is similar to running groovy scripts using the script console.
     *
     * In the instance where your script causes an exception, the server still
     * returns a 200 status, so detecting errors is very challenging. It is
     * recommended to use heuristics to check your return string for stack
     * traces by detecting strings like "groovy.lang.(something)Exception".
     *
     * @param script The script to be executed.
     * @return results The results of the script.
     * @throws IOException in case of an error.
     */
    public String runScript(String script) throws IOException {
        return runScript(script, false);
    }

    /**
     * Runs the provided groovy script on the server and returns the result.
     *
     * This is similar to running groovy scripts using the script console.
     *
     * In the instance where your script causes an exception, the server still
     * returns a 200 status, so detecting errors is very challenging. It is
     * recommended to use heuristics to check your return string for stack
     * traces by detecting strings like "groovy.lang.(something)Exception".
     *
     * @param script The script to run.
     * @param crumbFlag <code>true</code> to add <b>crumbIssuer</b>
     *            <code>false</code> otherwise.
     * @return results The results of the run of the script.
     * @throws IOException in case of an error.
     */
    public String runScript(String script, boolean crumbFlag) throws IOException {
        return client.post_text("/scriptText", "script=" + script, ContentType.APPLICATION_FORM_URLENCODED, crumbFlag);
    }

    public Queue getQueue() throws IOException {
        // TODO: Check if using depth=1 is a good idea?
        return client.get("queue/?depth=1", Queue.class);
    }

    public QueueItem getQueueItem(QueueReference ref) throws IOException {
        try {
            String url = ref.getQueueItemUrlPart();
            // "/queue/item/" + id
            QueueItem queueItem = client.get(url, QueueItem.class);
            queueItem.setClient(client);

            return queueItem;
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    public Build getBuild(QueueItem q) throws IOException {
        // http://ci.seitenbau.net/job/rainer-ansible-build/51/
        try {
            String url = q.getExecutable().getUrl();
            // "/queue/item/" + id
            Build job = client.get(url, Build.class);
            job.setClient(client);

            return job;
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    /**
     * Rename a job
     *
     * @param oldJobName existing job name.
     * @param newJobName The new job name.
     * @throws IOException In case of a failure.
     */
    public void renameJob(String oldJobName, String newJobName) throws IOException {
        renameJob(null, oldJobName, newJobName, false);
    }

    /**
     * Rename a job
     *
     * @param oldJobName existing job name.
     * @param newJobName The new job name.
     * @param crumbFlag <code>true</code> to add <b>crumbIssuer</b>
     *            <code>false</code> otherwise.
     * @throws IOException In case of a failure.
     */
    public void renameJob(String oldJobName, String newJobName, Boolean crumbFlag) throws IOException {
        renameJob(null, oldJobName, newJobName, crumbFlag);
    }

    /**
     * Rename a job
     *
     * @param folder {@link FolderJob}
     * @param oldJobName existing job name.
     * @param newJobName The new job name.
     * @throws IOException In case of a failure.
     */
    public void renameJob(FolderJob folder, String oldJobName, String newJobName) throws IOException {
        renameJob(folder, oldJobName, newJobName, false);
    }

    /**
     * Rename a job
     *
     * @param folder {@link FolderJob}
     * @param oldJobName existing job name.
     * @param newJobName The new job name.
     * @param crumbFlag <code>true</code> to add <b>crumbIssuer</b>
     *            <code>false</code> otherwise.
     * @throws IOException In case of a failure.
     */
    public void renameJob(FolderJob folder, String oldJobName, String newJobName, Boolean crumbFlag)
            throws IOException {
        client.post(toJobBaseUrl(folder, oldJobName) + "/doRename?newName=" + EncodingUtils.encodeParam(newJobName),
                crumbFlag);
    }

    /**
     * Helper to create a base url in case a folder is given
     * 
     * @param folder the folder or {@code null}
     * @return The created base url.
     */
    private String toBaseUrl(FolderJob folder) {
        String path = "/";
        if (folder != null) {
            path = folder.getUrl();
        }
        return path;
    }

    /**
     * Helper to create the base url for a job, with or without a given folder
     * 
     * @param folder the folder or {@code null}
     * @param jobName the name of the job.
     * @return converted base url.
     */
    private String toJobBaseUrl(FolderJob folder, String jobName) {
        return toBaseUrl(folder) + "job/" + EncodingUtils.encode(jobName);
    }

    /**
     * Helper to create the base url for a view, with or without a given folder
     * 
     * @param folder the folder or {@code null}
     * @param name the of the view.
     * @return converted view url.
     */
    private String toViewBaseUrl(FolderJob folder, String name) {
        return toBaseUrl(folder) + "view/" + EncodingUtils.encode(name);
    }

}
