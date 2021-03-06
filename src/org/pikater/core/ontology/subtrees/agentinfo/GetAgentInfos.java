package org.pikater.core.ontology.subtrees.agentinfo;

import jade.content.AgentAction;

/**
 * Request for {@link AgentInfo} objects for all computation
 * related agents approved to be used - all internal agents
 * and approved user agents.
 * 
 * @author stepan
 */
public class GetAgentInfos implements AgentAction {

	private static final long serialVersionUID = 5582405682331716949L;

	private int userID;

	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	
}
