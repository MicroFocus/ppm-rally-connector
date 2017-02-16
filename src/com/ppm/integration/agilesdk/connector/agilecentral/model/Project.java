package com.ppm.integration.agilesdk.connector.agilecentral.model;

import net.sf.json.JSONObject;

public class Project extends Entity {

    public Project(JSONObject jsonObject) {
        super(jsonObject);
    }

    public String getIterationsRef() {
        return this.jsonObject.getJSONObject("Iterations").getString("_ref");
    }

    public String getReleasesRef() {
        return this.jsonObject.getJSONObject("Releases").getString("_ref");
    }

    public int getChildrenCount() {
        return this.jsonObject.getJSONObject("Children").getInt("Count");
    }

    public String getParentUUID() {
        JSONObject iteration = this.jsonObject.getJSONObject("Parent");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectUUID");
        }
        return null;
    }
}
