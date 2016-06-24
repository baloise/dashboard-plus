package com.baloise.confluence.dashboardplus.neo4jteststatus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Neo4JConnector {

	private static final String NEO4J_REST_API_URL = "http://test-itq.balgroupit.com:7474/db/data/cypher";

	public Neo4JConnector() {
	}

	public String getResult(String cypher) {
		try {
			HttpURLConnection connection;
			URL url;
			url = new URL(NEO4J_REST_API_URL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-type", "application/json");
			connection.setDoOutput(true);
			cypher = "{\"query\" : \"" + cypher + "\", \"params\" : { }}";
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.writeBytes(cypher);
			wr.flush();
			wr.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			connection.disconnect();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
