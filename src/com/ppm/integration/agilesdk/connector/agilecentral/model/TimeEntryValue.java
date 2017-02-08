package com.ppm.integration.agilesdk.connector.agilecentral.model;

import net.sf.json.JSONObject;

public class TimeEntryValue extends Entity {

    public TimeEntryValue(JSONObject jsonObject) {
        super(jsonObject);
    }

    public String getDateVal() {
        return check("DateVal") ? jsonObject.getString("DateVal") : null;
    }

    public int getHours() {
        return check("Hours") ? jsonObject.getInt("Hours") : 0;
    }

    public String getTimeEntryItemUUID() {
        JSONObject iteration = this.jsonObject.getJSONObject("TimeEntryItem");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectUUID");
        }
        return null;
    }
}
