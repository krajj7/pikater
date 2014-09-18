package org.pikater.core.options.virtual;

import org.pikater.core.CoreConstant;
import org.pikater.core.agents.experiment.virtual.Agent_VirtualFileInputBoxProvider;
import org.pikater.core.ontology.subtrees.agentInfo.AgentInfo;
import org.pikater.core.ontology.subtrees.batchDescription.FileDataProvider;
import org.pikater.core.ontology.subtrees.newOption.base.NewOption;
import org.pikater.core.ontology.subtrees.newOption.values.StringValue;
import org.pikater.core.options.SlotsHelper;

public class FileInput_Box {
	
	public static AgentInfo get() {

		AgentInfo agentInfo = new AgentInfo();
		agentInfo.importAgentClass(Agent_VirtualFileInputBoxProvider.class);
		agentInfo.importOntologyClass(FileDataProvider.class);
	
		agentInfo.setName("FileInput");
		agentInfo.setDescription("This box provides a data source to other boxes.");

		NewOption optionIN = new NewOption(CoreConstant.Misc.FILEURI.get(), new StringValue("inputFile.ARFF"));
		optionIN.setDescription("File name");
		
		agentInfo.addOption(optionIN);
		
		agentInfo.setOutputSlots(
				SlotsHelper.getOutputSlot_FileInput());

		return agentInfo;
	}

}