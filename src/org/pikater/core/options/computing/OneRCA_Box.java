package org.pikater.core.options.computing;

import org.pikater.core.agents.experiment.computing.Agent_WekaOneRCA;
import org.pikater.core.ontology.subtrees.agentinfo.AgentInfo;
import org.pikater.core.ontology.subtrees.batchdescription.ComputingAgent;
import org.pikater.core.ontology.subtrees.newoption.base.NewOption;
import org.pikater.core.ontology.subtrees.newoption.restrictions.RangeRestriction;
import org.pikater.core.ontology.subtrees.newoption.values.IntegerValue;
import org.pikater.core.options.OptionsHelper;
import org.pikater.core.options.SlotsHelper;

public class OneRCA_Box {
	

	public static AgentInfo get() {

		/**
		# Specify the minimum number of objects in a bucket (default: 6).
		$ B int 1 1 r 1 100
		**/
		NewOption optionB = new NewOption("B", new IntegerValue(6), new RangeRestriction(
				new IntegerValue(1),
				new IntegerValue(100))
		);
		optionB.setDescription("Specify the minimum number of objects in a bucket");
		

		AgentInfo agentInfo = new AgentInfo();
		agentInfo.importAgentClass(Agent_WekaOneRCA.class);
		agentInfo.importOntologyClass(ComputingAgent.class);
	
		agentInfo.setName("OneR");
		agentInfo.setDescription("One R Method");

		agentInfo.addOption(optionB);
		agentInfo.addOptions(OptionsHelper.getCAOptions());

		// Slots Definition
		agentInfo.setInputSlots(SlotsHelper.getInputSlots_CA());
		agentInfo.setOutputSlots(SlotsHelper.getOutputSlots_CA());

		return agentInfo;
	}

}
