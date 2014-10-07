package com.yejq.spring.quartz.model;

import java.util.List;

/**
 * @author jim.ye
 */
public class TiggerGroup {

	private String groupName;
	private List<TriggerModel> triggerModels;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<TriggerModel> getTriggerModels() {
		return triggerModels;
	}

	public void setTriggerModels(List<TriggerModel> triggerModels) {
		this.triggerModels = triggerModels;
	}


}
