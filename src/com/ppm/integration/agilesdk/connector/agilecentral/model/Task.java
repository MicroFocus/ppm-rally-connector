
package com.ppm.integration.agilesdk.connector.agilecentral.model;

import net.sf.json.JSONObject;

public class Task extends Entity {

    public Task(JSONObject jsonObject) {
        super(jsonObject);
    }

    public String getIterationUUID() {
        JSONObject iteration = this.jsonObject.getJSONObject("Iteration");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectUUID");
        }
        return null;
    }

    public String getIterationName() {
        JSONObject iteration = this.jsonObject.getJSONObject("Iteration");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectName");
        }
        return null;
    }

    public String getReleaseUUID() {
        JSONObject iteration = this.jsonObject.getJSONObject("Release");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectUUID");
        }
        return null;
    }

    public String getReleaseName() {
        JSONObject iteration = this.jsonObject.getJSONObject("Release");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectName");
        }
        return null;
    }

    public String getWorkProductUUID() {
        JSONObject iteration = this.jsonObject.getJSONObject("WorkProduct");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectUUID");
        }
        return null;
    }

    public String getWorkProductType() {
        JSONObject iteration = this.jsonObject.getJSONObject("WorkProduct");
        if (!iteration.isNullObject()) {
            return iteration.getString("_type");
        }
        return null;
    }

    public String getProjectUUID() {
        JSONObject iteration = this.jsonObject.getJSONObject("Project");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectUUID");
        }
        return null;
    }

    public String getProjectId() {
        JSONObject iteration = this.jsonObject.getJSONObject("Project");
        if (!iteration.isNullObject()) {
            return iteration.getString("_ref").split("/")[iteration.getString("_ref").split("/").length - 1];
        }
        return null;
    }
}
