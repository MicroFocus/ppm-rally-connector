package com.ppm.integration.agilesdk.connector.agilecentral.model;

import java.util.Date;

import net.sf.json.JSONObject;

import com.ppm.integration.agilesdk.pm.ExternalTask;
import com.ppm.integration.agilesdk.provider.UserProvider;

public class HierarchicalRequirement extends Entity {

	private Iteration iteration;
	private User user;
	private final UserProvider userProvider;

	public HierarchicalRequirement(JSONObject jsonObject, UserProvider userProvider) {
		super(jsonObject);
		this.userProvider = userProvider;
	}

	public String getIterationUUID() {
		JSONObject iteration = this.jsonObject.getJSONObject("Iteration");
		if (!iteration.isNullObject()) {
			return iteration.getString("_refObjectUUID");
		}
		return null;
	}
	
	public String getProjectUUID() {
		JSONObject project = this.jsonObject.getJSONObject("Project");
		if (!project.isNullObject()) {
			return project.getString("_refObjectUUID");
		}
		return null;
	}
	
	public String getOwnerUUID() {
		JSONObject iteration = this.jsonObject.getJSONObject("Owner");
		if (!iteration.isNullObject()) {
			return iteration.getString("_refObjectUUID");
		}
		return null;
	}

	public void setIteration(Iteration iteration) {
		this.iteration = iteration;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getScheduleStart() {
		return iteration.getScheduleStart();
	}

	public Date getScheduleFinish() {
		return iteration.getScheduleFinish();
	}

	@Override
	public long getOwnerId() {
		com.hp.ppm.user.model.User u = userProvider.getByEmail(this.user.getEmailAddress());
		return u == null ? -1 : u.getUserId();
	}

	@Override
	public String getOwnerRole() {		
		//change
		return this.user == null ? null : this.user.getRole();
	}

	@Override
	public TaskStatus getStatus() {
		String state = (check("ScheduleState") ? jsonObject.getString("ScheduleState") : null);
        ExternalTask.TaskStatus result = ExternalTask.TaskStatus.UNKNOWN;
		//Defined,In-Progress,Completed,Accepted
		switch (state){
		case "Defined":
                result = ExternalTask.TaskStatus.READY;
			break;
		case "In-Progress":
                result = ExternalTask.TaskStatus.IN_PROGRESS;
			break;
		case "Completed":
		case "Accepted":
                result = ExternalTask.TaskStatus.COMPLETED;
			break;
		}
		return result;
	}
	
	public int getChildrenCount(){
		JSONObject tasks = this.jsonObject.getJSONObject("Children");
		if(!tasks.isNullObject()){
			return tasks.getInt("Count");
		}
		return 0;
	}
	
}
