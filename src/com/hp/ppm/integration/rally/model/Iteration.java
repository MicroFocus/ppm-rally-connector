package com.hp.ppm.integration.rally.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.hp.ppm.integration.pm.IExternalTask;

public class Iteration extends Entity {

	private final List<IExternalTask> hierarchicalRequirements = new ArrayList<IExternalTask>();

	public Iteration(JSONObject jsonObject) {
		super(jsonObject);
	}

	public void addHierarchicalRequirement(HierarchicalRequirement hierarchicalRequirement) {	
		if (hierarchicalRequirement.getIterationUUID() != null && this.getUUID().equals(hierarchicalRequirement.getIterationUUID())){
			//if(hierarchicalRequirement.getTasksCount() != 0)
			hierarchicalRequirement.setIteration(this);
			this.hierarchicalRequirements.add(hierarchicalRequirement);			
		}
	}

	@Override
	public Date getScheduleStart() {
		return convertToDate(check("StartDate") ? jsonObject.getString("StartDate") : null);
	}

	@Override
	public Date getScheduleFinish() {
		return convertToDate(check("EndDate") ? jsonObject.getString("EndDate") : null);
	}

	@Override
	public TaskStatus getStatus() {
		String status = (check("State") ? jsonObject.getString("State") : null);
		IExternalTask.TaskStatus result = IExternalTask.TaskStatus.UNKNOWN;
		//Planning,Committed,Accepted
		switch (status){
		case "Planning":
			result = IExternalTask.TaskStatus.IN_PLANNING;
			break;
		case "Committed":
			result = IExternalTask.TaskStatus.READY;
			break;
		case "Accepted":
			result = IExternalTask.TaskStatus.COMPLETED;
			break;
		}
		return result;
	}

	@Override
	public List<IExternalTask> getChildren() {
		return hierarchicalRequirements;		
	}

}
