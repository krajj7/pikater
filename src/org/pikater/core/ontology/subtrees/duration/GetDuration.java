package org.pikater.core.ontology.subtrees.duration;

import jade.content.AgentAction;

public class GetDuration implements AgentAction {
	
	private static final long serialVersionUID = -6983656424501719246L;
	
	Duration duration;

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

}
