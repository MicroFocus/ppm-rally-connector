package com.ppm.integration.agilesdk.connector.agilecentral.model;

import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.pm.ExternalTask;
import com.ppm.integration.agilesdk.pm.ExternalTaskActuals;

public class Entity extends ExternalTask {

	protected JSONObject jsonObject;

	public Entity(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}
	protected boolean check(String key) {
		return jsonObject != null && jsonObject.containsKey(key);
	}

	protected Date convertToDate(String date) {
		return date != null && date != "null" ? DatatypeConverter.parseDateTime(date).getTime() : null;
	}

	public String getUUID() {
		return check("_refObjectUUID") ? jsonObject.getString("_refObjectUUID") : null;
	}

	public String getType(){
		return check("_type") ? jsonObject.getString("_type") : null;
	}

	@Override
	public String getId() {
		return check("ObjectID") ? jsonObject.getString("ObjectID") : null;
	}

	@Override
	public String getName() {
		return check("Name") ? jsonObject.getString("Name") : null;
	}

	@Override
	public long getOwnerId() {
		return -1;
	}

	@Override
	public String getOwnerRole() {
		return null;
	}

	@Override
    public List<ExternalTask> getChildren() {
		return null;
	}

	@Override
	public TaskStatus getStatus() {
		return null;
	}

	@Override
    public List<ExternalTaskActuals> getActuals() {
		return null;
	}

	@Override
	public boolean isMilestone() {
		return false;
	}

}
