package com.ppm.integration.agilesdk.connector.agilecentral.model;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.provider.UserProvider;

public class Testset extends Backlog {

    public Testset(JSONObject jsonObject, UserProvider userProvider) {
        super(jsonObject, userProvider);
    }
	
}
