
package com.ppm.integration.agilesdk.connector.agilecentral.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioFeature;
import com.ppm.integration.agilesdk.pm.ExternalTask;
import com.ppm.integration.agilesdk.provider.UserProvider;

public class HierarchicalRequirement extends Entity {

    private Iteration iteration;

    private Release release;

    private PortfolioFeature portfolioFeature;

    private HierarchicalRequirement hierarchicalRequirement;

    private User user;

    private final UserProvider userProvider;

    private final List<ExternalTask> hierarchicalRequirements = new ArrayList<ExternalTask>();

    public HierarchicalRequirement(JSONObject jsonObject, UserProvider userProvider) {
        super(jsonObject);
        this.userProvider = userProvider;
    }

    public void addHierarchicalRequirement(HierarchicalRequirement hierarchicalRequirement) {
        hierarchicalRequirement.setHierarchicalRequirement(this);
        this.hierarchicalRequirements.add(hierarchicalRequirement);
    }

    public String getIterationUUID() {
        JSONObject iteration = this.jsonObject.getJSONObject("Iteration");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectUUID");
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

    public String getPortfolioFeatureUUID() {
        JSONObject iteration = this.jsonObject.getJSONObject("PortfolioItem");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectUUID");
        }
        return null;
    }

    public String getProjectUUID() {
        JSONObject project = this.jsonObject.getJSONObject("Project");
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

    public String getParentUUID() {
        JSONObject project = this.jsonObject.getJSONObject("Parent");
        if (!project.isNullObject()) {
            return project.getString("_refObjectUUID");
        }
        return null;
    }

    public String getOwnerUUID() {
        JSONObject iteration = this.jsonObject.getJSONObject("Owner");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectUUID");
        }
        return null;
    }

    public void setIteration(Iteration iteration) {
        this.iteration = iteration;
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    public void setPortfolioFeature(PortfolioFeature portfolioFeature) {
        this.portfolioFeature = portfolioFeature;
    }

    public void setHierarchicalRequirement(HierarchicalRequirement hierarchicalRequirement) {
        this.hierarchicalRequirement = hierarchicalRequirement;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getScheduleStart() {
        return iteration.getScheduleStart();
    }

    public Date getScheduleFinish() {
        return iteration.getScheduleFinish();
    }

    @Override
    public long getOwnerId() {
        com.hp.ppm.user.model.User u = userProvider.getByEmail(this.user.getEmailAddress());
        return u == null ? -1 : u.getUserId();
    }

    @Override
    public String getOwnerRole() {
        // change
        return this.user == null ? null : this.user.getRole();
    }

    @Override
    public TaskStatus getStatus() {
        String state = (check("ScheduleState") ? jsonObject.getString("ScheduleState") : null);
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

    public int getChildrenCount() {
        JSONObject tasks = this.jsonObject.getJSONObject("Children");
        if (!tasks.isNullObject()) {
            return tasks.getInt("Count");
        }
        return 0;
    }

    @Override
    public String getName() {
        String formattedId = check("FormattedID") ? jsonObject.getString("FormattedID") : null;
        String name = check("Name") ? jsonObject.getString("Name") : null;
        return "[" + formattedId + "] " + name;
    }

    @Override
    public List<ExternalTask> getChildren() {
        return hierarchicalRequirements;
    }
}
