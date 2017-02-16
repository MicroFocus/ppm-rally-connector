
package com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.connector.agilecentral.model.HierarchicalRequirement;
import com.ppm.integration.agilesdk.pm.ExternalTask;

public class PortfolioFeature extends PortfolioItem {

    private PortfolioInitiative initiative;

    private final List<ExternalTask> userStories = new ArrayList<>();

    public PortfolioFeature(JSONObject jsonObject) {
        super(jsonObject);
    }

    public void setInitiative(PortfolioInitiative initiative) {
        this.initiative = initiative;
    }

    public void addUserStory(HierarchicalRequirement hierarchicalRequirement) {
        this.userStories.add(hierarchicalRequirement);
    }

    @Override
    public List<ExternalTask> getChildren() {
        return userStories;
    }

}
