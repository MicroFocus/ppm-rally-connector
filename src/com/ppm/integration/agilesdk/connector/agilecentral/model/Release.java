
package com.ppm.integration.agilesdk.connector.agilecentral.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.pm.ExternalTask;

public class Release extends Entity {

    private final List<ExternalTask> iterationAndUSs = new ArrayList<ExternalTask>();

    public Release(JSONObject jsonObject) {
        super(jsonObject);
    }

    public void addHierarchicalRequirement(HierarchicalRequirement hierarchicalRequirement) {
        if (hierarchicalRequirement.getReleaseUUID() != null
                && this.getUUID().equals(hierarchicalRequirement.getReleaseUUID())) {
            hierarchicalRequirement.setRelease(this);
            this.iterationAndUSs.add(hierarchicalRequirement);
        }
    }

    public void addIteration(Iteration iteration) {
        iteration.setRelease(this);
        this.iterationAndUSs.add(iteration);
    }

    public Date getScheduleStart() {
        return convertToDate(check("ReleaseStartDate") ? jsonObject.getString("ReleaseStartDate") : null);
    }

    public Date getScheduleFinish() {
        return convertToDate(check("ReleaseDate") ? jsonObject.getString("ReleaseDate") : null);
    }

    @Override
    public List<ExternalTask> getChildren() {
        return iterationAndUSs;
    }

    @Override
    public TaskStatus getStatus() {
        String state = (check("State") ? jsonObject.getString("State") : null);
        ExternalTask.TaskStatus result = ExternalTask.TaskStatus.UNKNOWN;
        // Defined,In-Progress,Completed,Accepted
        switch (state) {
            case "Defined":
                result = ExternalTask.TaskStatus.READY;
                break;
            case "In-Progress":
                result = ExternalTask.TaskStatus.IN_PROGRESS;
                break;
            case "Completed":
            case "Accepted":
                result = ExternalTask.TaskStatus.COMPLETED;
                break;
        }
        return result;
    }
}
