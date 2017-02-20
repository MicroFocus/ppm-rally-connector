
package com.ppm.integration.agilesdk.connector.agilecentral.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.pm.ExternalTask;
import com.ppm.integration.agilesdk.pm.ExternalTaskActuals;

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

    @Override
    public Date getScheduledStart() {
        return convertToDate(check("ReleaseStartDate") ? jsonObject.getString("ReleaseStartDate") : null);
    }

    @Override
    public Date getScheduledFinish() {
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

    @Override
    public List<ExternalTaskActuals> getActuals() {
        List<ExternalTaskActuals> actuals = new ArrayList<>();

        actuals.add(new ExternalTaskActuals() {

            @Override
            public double getScheduledEffort() {
                if (jsonObject.getString("TaskEstimateTotal").equals("null")) {
                    return 0.0D;
                }
                return Double.parseDouble(jsonObject.getString("TaskEstimateTotal"));
            }

            @Override
            public Date getActualStart() {
                return null;
            }

            @Override
            public Date getActualFinish() {
                return null;
            }

            @Override
            public double getActualEffort() {
                System.out.println("1# " + jsonObject.getString("TaskActualTotal").equals("null"));
                if (jsonObject.getString("TaskActualTotal").equals("null")) {
                    return 0.0D;
                }
                return Double.parseDouble(jsonObject.getString("TaskActualTotal"));
            }

            @Override
            public double getPercentComplete() {
                return 0.0D;
            }

            @Override
            public long getResourceId() {
                return -1L;
            }

            @Override
            public Double getEstimatedRemainingEffort() {
                if (jsonObject.getString("TaskRemainingTotal").equals("null")) {
                    return 0.0D;
                }
                return Double.parseDouble(jsonObject.getString("TaskRemainingTotal"));
            }

            @Override
            public Date getEstimatedFinishDate() {
                return null;
            }
        });
        return actuals;
    }
}
