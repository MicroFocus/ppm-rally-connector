package com.ppm.integration.agilesdk.connector.agilecentral.model;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.provider.UserProvider;

public class Defect extends Backlog {

    public Defect(JSONObject jsonObject, UserProvider userProvider) {
        super(jsonObject, userProvider);
    }
	
}
