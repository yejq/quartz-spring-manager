package com.yejq.spring.quartz.model;

import org.quartz.Trigger;

/**
 * @author jim.ye
 */
public class TriggerModel {

	private Trigger trigger = null;
	private int status = 0;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

}
