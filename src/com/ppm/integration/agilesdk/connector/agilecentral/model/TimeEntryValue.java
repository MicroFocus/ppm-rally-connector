package com.ppm.integration.agilesdk.connector.agilecentral.model;

import net.sf.json.JSONObject;

import java.util.Date;

public class TimeEntryValue extends Entity {

    public TimeEntryValue(JSONObject jsonObject) {
        super(jsonObject);
    }

    public Date getDateVal() {
        return convertToDate(check("DateVal") ? jsonObject.getString("DateVal") : null);
    }

    public int getHours() {
        return check("Hours") ? jsonObject.getInt("Hours") : 0;
    }

    // add
    public String getItemUUID() {
        JSONObject iteration = this.jsonObject.getJSONObject("TimeEntryItem");
        if (!iteration.isNullObject()) {
            return iteration.getString("_refObjectUUID");
        }
        return null;
    }
}
