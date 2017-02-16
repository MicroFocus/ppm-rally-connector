
package com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.pm.ExternalTask;

public class PortfolioTheme extends PortfolioItem {

    private final List<ExternalTask> initiatives = new ArrayList<ExternalTask>();

    public PortfolioTheme(JSONObject jsonObject) {
        super(jsonObject);
    }

    public void addInitiative(PortfolioInitiative initiative) {
        initiative.setTheme(this);
        this.initiatives.add(initiative);
    }

    @Override
    public List<ExternalTask> getChildren() {
        return initiatives;
    }
}
