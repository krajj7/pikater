package org.pikater.core.options.search;

import org.pikater.core.agents.experiment.search.Agent_ChooseXValues;
import org.pikater.core.ontology.subtrees.agentinfo.AgentInfo;
import org.pikater.core.ontology.subtrees.batchdescription.Search;
import org.pikater.core.ontology.subtrees.newoption.base.NewOption;
import org.pikater.core.ontology.subtrees.newoption.restrictions.RangeRestriction;
import org.pikater.core.ontology.subtrees.newoption.values.IntegerValue;
import org.pikater.core.options.SlotsHelper;

public class ChooseXValue_Box {
	
	public static AgentInfo get() {

		NewOption optionN = new NewOption("N", new IntegerValue(5), new RangeRestriction(
				new IntegerValue(1),
				new IntegerValue(2000))
		); 
		optionN.setDescription("Number of values to try for each option");
		
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.importAgentClass(Agent_ChooseXValues.class);
		agentInfo.importOntologyClass(Search.class);
	
		agentInfo.setName("ValueChoosing");
		agentInfo.setDescription("Search which Choose X Values");

		agentInfo.addOption(optionN);
		
		// Slot Definition
		agentInfo.setOutputSlots(SlotsHelper.getOutputSlots_Search());

		return agentInfo;
	}

}
