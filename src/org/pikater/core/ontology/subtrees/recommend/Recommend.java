package org.pikater.core.ontology.subtrees.recommend;

import org.pikater.core.agents.system.computationDescriptionParser.edges.ErrorEdge;
import org.pikater.core.ontology.subtrees.data.Data_;
import org.pikater.core.ontology.subtrees.management.Agent;

import jade.content.AgentAction;
import org.pikater.core.ontology.subtrees.task.Evaluation;

public class Recommend implements AgentAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4556943676301959461L;
	
	private Data_ data; 
	private Agent recommender;
    private Evaluation previousError;
	
	public Data_ getData() {
		return data;
	}
	public void setData(Data_ data) {
		this.data = data;
	}
	public Agent getRecommender() {
		return recommender;
	}
	public void setRecommender(Agent recommender) {
		this.recommender = recommender;
	}

    public Evaluation getPreviousError() {
        return previousError;
    }

    public void setPreviousError(Evaluation previousError) {
        this.previousError = previousError;
    }
}
