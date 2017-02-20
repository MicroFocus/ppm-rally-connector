
package com.ppm.integration.agilesdk.connector.agilecentral.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.pm.ExternalTask;
import com.ppm.integration.agilesdk.pm.ExternalTaskActuals;

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

    @Override
    public Date getScheduledStart() {
        return convertToDate(check("StartDate") ? jsonObject.getString("StartDate") : null);
    }

    @Override
    public Date getScheduledFinish() {
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
