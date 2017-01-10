package com.ppm.integration.agilesdk.connector.agilecentral.model;

import net.sf.json.JSONObject;

public class Project extends Entity{

	public Project(JSONObject jsonObject) {
		super(jsonObject);
	}

	public String getIterationsRef() {
		return this.jsonObject.getJSONObject("Iterations").getString("_ref");
	}
	public String getReleasesRef() {
		return this.jsonObject.getJSONObject("Releases").getString("_ref");
	}
}
