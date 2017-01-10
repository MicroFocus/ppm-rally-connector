package com.ppm.integration.agilesdk.connector.agilecentral.model;

import net.sf.json.JSONObject;

public class Workspace extends Entity {

    public Workspace(JSONObject jsonObject) {
        super(jsonObject);
    }

    public String getProjectsRef() {
        return this.jsonObject.getJSONObject("Projects").getString("_ref");
    }
}
