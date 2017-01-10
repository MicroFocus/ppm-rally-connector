package com.ppm.integration.agilesdk.connector.agilecentral.model;

import java.util.Date;

import net.sf.json.JSONObject;

public class Release extends Entity {
	
	public Release(JSONObject jsonObject) {
		super(jsonObject);
	}
	
	@Override
	public Date getScheduleStart() {
		return convertToDate(check("ReleaseStartDate") ? jsonObject.getString("ReleaseStartDate") : null);
	}

	@Override
	public Date getScheduleFinish() {
		return convertToDate(check("ReleaseDate") ? jsonObject.getString("ReleaseDate") : null);
	}


}
