
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
            case "Planning":
                result = ExternalTask.TaskStatus.READY;
                break;
            case "Active":
                result = ExternalTask.TaskStatus.IN_PROGRESS;
                break;
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
                return this.getTaskEstimateTotal();
            }

            @Override
            public Date getActualStart() {
                return convertToDate(check("ReleaseStartDate") ? jsonObject.getString("ReleaseStartDate") : null);
            }

            @Override
            public Date getActualFinish() {
                return convertToDate(check("ReleaseEndDate") ? jsonObject.getString("ReleaseEndDate") : null);
            }

            @Override
            public double getActualEffort() {
                return this.getTaskActualTotal();
            }

            @Override
            public double getPercentComplete() {
                if (this.getScheduleState().equals("Accepted")) {
                    return 1.0D * 100;
                } else if (this.getTaskEstimateTotal() != 0.0D) {
                    return (1 - this.getTaskRemainingTotal() / this.getTaskEstimateTotal()) * 100;
                }
                return 0.0D;
            }

            @Override
            public long getResourceId() {
                return -1L;
            }

            @Override
            public Double getEstimatedRemainingEffort() {
                return this.getTaskRemainingTotal();
            }

            @Override
            public Date getEstimatedFinishDate() {
                return convertToDate(check("ReleaseEndDate") ? jsonObject.getString("ReleaseEndDate") : null);
            }

            // task
            public double getTaskEstimateTotal() {
                if (jsonObject.getString("TaskEstimateTotal").equals("null")) {
                    return 0.0D;
                }
                return Double.parseDouble(jsonObject.getString("TaskEstimateTotal"));
            }

            public double getTaskActualTotal() {
                if (jsonObject.getString("TaskActualTotal").equals("null")) {
                    return 0.0D;
                }
                return Double.parseDouble(jsonObject.getString("TaskActualTotal"));
            }

            public double getTaskRemainingTotal() {
                if (jsonObject.getString("TaskRemainingTotal").equals("null")) {
                    return 0.0D;
                }
                return Double.parseDouble(jsonObject.getString("TaskRemainingTotal"));
            }

            // state
            public String getScheduleState() {
                return check("State") ? jsonObject.getString("State") : null;
            }
        });
        return actuals;
    }
}
