package com.ppm.integration.agilesdk.connector.agilecentral.ui;

import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.ui.DynamicDropdown;

import java.util.List;

public class RallyEntityDropdown extends DynamicDropdown {

    public RallyEntityDropdown(String name, String labelKey, boolean isRequired) {
        super(name, labelKey, isRequired);
    }

    public RallyEntityDropdown(String name, String labelKey, String defaultValue, String display, boolean isRequired) {
        super(name, labelKey, defaultValue, display, isRequired);
    }

    @Override public List<String> getDependencies() {
        return null;
    }

    @Override public List<Option> getDynamicalOptions(ValueSet paramValueSet) {
        return null;
    }

}
