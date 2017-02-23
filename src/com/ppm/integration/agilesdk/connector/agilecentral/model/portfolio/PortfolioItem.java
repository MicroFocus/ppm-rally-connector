
package com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.connector.agilecentral.model.Entity;
import com.ppm.integration.agilesdk.pm.ExternalTask;
import com.ppm.integration.agilesdk.pm.ExternalTaskActuals;

public class PortfolioItem extends Entity {

    public PortfolioItem(JSONObject jsonObject) {
        super(jsonObject);
    }

    @Override
    public String getName() {
        String formattedId = check("FormattedID") ? jsonObject.getString("FormattedID") : null;
        String name = check("Name") ? jsonObject.getString("Name") : null;
        return formattedId + ": " + name;
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

    @Override
    public Date getScheduledStart() {
        if (check("PlannedStartDate")) {
            if (!jsonObject.getString("PlannedStartDate").equals("null")) {
                return convertToDate(jsonObject.getString("PlannedStartDate"));
            }
        }
        return convertToDate(check("CreationDate") ? jsonObject.getString("CreationDate") : null);
    }

    @Override
    public Date getScheduledFinish() {
        if (check("PlannedEndDate")) {
            if (!jsonObject.getString("PlannedEndDate").equals("null")) {
                return convertToDate(jsonObject.getString("PlannedEndDate"));
            }
        }
        return adjustFinishDateTime(new Date());
    }

    @Override
    public List<ExternalTaskActuals> getActuals() {
        List<ExternalTaskActuals> actuals = new ArrayList<>();

        actuals.add(new ExternalTaskActuals() {

            @Override
            public double getScheduledEffort() {
                return 0.0D;
            }

            @Override
            public Date getActualStart() {
                return convertToDate(check("ActualStartDate") ? jsonObject.getString("ActualStartDate") : null);
            }

            @Override
            public Date getActualFinish() {
                return convertToDate(check("ActualEndDate") ? jsonObject.getString("ActualEndDate") : null);
            }

            @Override
            public double getActualEffort() {
                return 0.0D;
            }

            @Override
            public double getPercentComplete() {
                return jsonObject.getDouble("PercentDoneByStoryPlanEstimate") * 100;
            }

            @Override
            public long getResourceId() {
                return -1L;
            }

            @Override
            public Double getEstimatedRemainingEffort() {
                return null;
            }

            @Override
            public Date getEstimatedFinishDate() {
                return null;
            }
        });
        return actuals;
    }

}
