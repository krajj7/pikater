package org.pikater.core.options;

import org.pikater.core.agents.experiment.search.Agent_ChooseXValues;
import org.pikater.core.ontology.subtrees.agentInfo.AgentInfo;
import org.pikater.core.ontology.subtrees.batchDescription.Search;
import org.pikater.core.ontology.subtrees.newOption.base.NewOption;
import org.pikater.core.ontology.subtrees.newOption.restrictions.RangeRestriction;
import org.pikater.core.ontology.subtrees.newOption.values.IntegerValue;

public class ChooseXValue_SearchBox {
	
	public static AgentInfo get() {

		NewOption optionN = new NewOption("N", new IntegerValue(5), new RangeRestriction(
				new IntegerValue(1),
				new IntegerValue(2000))
		); 
		optionN.setDescription("Number of values to try for each option");
		
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.importAgentClass(Agent_ChooseXValues.class);
		agentInfo.importOntologyClass(Search.class);
	
		agentInfo.setName("ChooseX-Values");
		agentInfo.setDescription("Search which Choose X Values");

		agentInfo.addOption(optionN);
		
		// Slot Definition
		agentInfo.setOutputSlots(AAA_SlotHelper.getSearcherOutputSlots());

		return agentInfo;
	}

}
