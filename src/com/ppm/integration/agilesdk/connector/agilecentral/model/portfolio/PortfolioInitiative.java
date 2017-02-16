
package com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.pm.ExternalTask;

public class PortfolioInitiative extends PortfolioItem {

    private PortfolioTheme theme;

    private final List<ExternalTask> features = new ArrayList<ExternalTask>();

    public PortfolioInitiative(JSONObject jsonObject) {
        super(jsonObject);
    }

    public void addFeature(PortfolioFeature feature) {
        feature.setInitiative(this);
        this.features.add(feature);
    }

    public void setTheme(PortfolioTheme theme) {
        this.theme = theme;
    }

    @Override
    public List<ExternalTask> getChildren() {
        return features;
    }

}
