
package com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.connector.agilecentral.model.Entity;
import com.ppm.integration.agilesdk.pm.ExternalTask;

public class PortfolioItem extends Entity {

    public PortfolioItem(JSONObject jsonObject) {
        super(jsonObject);
    }

    @Override
    public String getName() {
        String formattedId = check("FormattedID") ? jsonObject.getString("FormattedID") : null;
        String name = check("Name") ? jsonObject.getString("Name") : null;
        return "[" + formattedId + "] " + name;
    }

    public String getChildrenUUID() {
        JSONObject project = (check("Children") ? this.jsonObject.getJSONObject("Children") : null);
        if (!project.isNullObject()) {
            return project.getString("_refObjectUUID");
        }
        return null;
    }

    public String getParentUUID() {
        JSONObject project = (check("Parent") ? this.jsonObject.getJSONObject("Parent") : null);
        if (!project.isNullObject()) {
            return project.getString("_refObjectUUID");
        }
        return null;
    }

    public String getProjectID() {
        JSONObject project = this.jsonObject.getJSONObject("Project");
        if (!project.isNullObject()) {
            return project.getString("_ref").split("/")[project.getString("_ref").split("/").length - 1];
        }
        return null;
    }

    @Override
    public TaskStatus getStatus() {
        ExternalTask.TaskStatus result = ExternalTask.TaskStatus.UNKNOWN;
        return result;
    }

}
