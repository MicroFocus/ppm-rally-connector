package com.ppm.integration.agilesdk.connector.agilecentral.model;

import net.sf.json.JSONObject;

import java.util.Date;

public class Release extends Entity {

    public Release(JSONObject jsonObject) {
        super(jsonObject);
    }

    public Date getScheduleStart() {
        return convertToDate(check("ReleaseStartDate") ? jsonObject.getString("ReleaseStartDate") : null);
    }

    public Date getScheduleFinish() {
        return convertToDate(check("ReleaseDate") ? jsonObject.getString("ReleaseDate") : null);
    }

}
