package com.hp.ppm.integration.rally.model;

import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import net.sf.json.JSONObject;

import com.hp.ppm.integration.pm.IExternalTask;
import com.hp.ppm.integration.pm.ITaskActual;

public class Entity implements IExternalTask{

	protected JSONObject jsonObject;

	public Entity(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	//====================================================================================

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
	//====================================================================================

	@Override
	public String getId() {
		return check("ObjectID") ? jsonObject.getString("ObjectID") : null;
	}

	@Override
	public String getName() {
		return check("Name") ? jsonObject.getString("Name") : null;
	}

	@Override
	public Date getScheduleStart() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getScheduleFinish() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getOwnerId() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public String getOwnerRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IExternalTask> getChildren() {
		return null;
	}

	@Override
	public TaskStatus getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ITaskActual> getActuals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMilestone() {
		return false;
	}

}
