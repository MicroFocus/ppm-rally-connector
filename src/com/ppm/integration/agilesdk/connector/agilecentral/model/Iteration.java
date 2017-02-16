
package com.ppm.integration.agilesdk.connector.agilecentral.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.pm.ExternalTask;

public class Iteration extends Entity {

    private Release release;

    private final List<ExternalTask> hierarchicalRequirements = new ArrayList<ExternalTask>();

    public Iteration(JSONObject jsonObject) {
        super(jsonObject);
    }

    public void addHierarchicalRequirement(HierarchicalRequirement hierarchicalRequirement) {
        if (hierarchicalRequirement.getIterationUUID() != null
                && this.getUUID().equals(hierarchicalRequirement.getIterationUUID())) {
            hierarchicalRequirement.setIteration(this);
            this.hierarchicalRequirements.add(hierarchicalRequirement);
        }
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    public Date getScheduleStart() {
        return convertToDate(check("StartDate") ? jsonObject.getString("StartDate") : null);
    }

    public Date getScheduleFinish() {
        return convertToDate(check("EndDate") ? jsonObject.getString("EndDate") : null);
    }

    @Override
    public TaskStatus getStatus() {
        String status = (check("State") ? jsonObject.getString("State") : null);
        ExternalTask.TaskStatus result = ExternalTask.TaskStatus.UNKNOWN;
        switch (status) {
            case "Planning":
                result = ExternalTask.TaskStatus.IN_PLANNING;
                break;
            case "Committed":
                result = ExternalTask.TaskStatus.READY;
                break;
            case "Accepted":
                result = ExternalTask.TaskStatus.COMPLETED;
                break;
        }
        return result;
    }

    @Override
    public List<ExternalTask> getChildren() {
        return hierarchicalRequirements;
    }

}
