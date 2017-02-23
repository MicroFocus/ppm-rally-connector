
package com.ppm.integration.agilesdk.connector.agilecentral.model;

import net.sf.json.JSONObject;

public class TimeEntryItem extends Entity {

    public TimeEntryItem(JSONObject jsonObject) {
        super(jsonObject);
    }

    public String getTaskUUID() {
        JSONObject iteration = this.jsonObject.getJSONObject("Task");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectUUID");
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

    public String getWorkProductDisplayString() {
        return this.jsonObject.getString("WorkProductDisplayString");
    }

    public String getWorkProductType() {
        JSONObject iteration = this.jsonObject.getJSONObject("WorkProduct");
        if (!iteration.isNullObject()) {
            return iteration.getString("_type");
        }
        return null;
    }

    public String getTaskDisplayString() {
        return this.jsonObject.getString("TaskDisplayString");
    }

    public String getProjectId() {
        JSONObject iteration = this.jsonObject.getJSONObject("Project");
        if (!iteration.isNullObject()) {
            return iteration.getString("_ref").split("/")[iteration.getString("_ref").split("/").length - 1];
        }
        return null;
    }
}
