package org.pikater.core.agents.system.planner;

import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import org.pikater.core.agents.system.Agent_Planner;
import org.pikater.core.agents.system.managerAgent.ManagerAgentService;
import org.pikater.core.configuration.Arguments;
import org.pikater.core.ontology.AgentManagementOntology;
import org.pikater.core.ontology.TaskOntology;
import org.pikater.core.ontology.subtrees.management.ComputerInfo;
import org.pikater.core.ontology.subtrees.management.GetComputerInfo;
import org.pikater.core.ontology.subtrees.task.ExecuteTask;
import org.pikater.core.ontology.subtrees.task.Task;

public class PlannerCommunicator {

	private Agent_Planner agent;
	
	public PlannerCommunicator(Agent_Planner agent) {
		this.agent = agent;
	}
	
	public AID[] getAllAgents(String agentType) {

		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType(agentType);
		template.addServices(serviceDescription);

		AID[] foundAgents = null;
		try {
			DFAgentDescription[] result = DFService.search(agent, template);
			foundAgents = new AID[result.length];
			for (int i = 0; i < result.length; ++i) {
				foundAgents[i] = result[i].getName();
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		return foundAgents;
	}
	
	/** Request creation or loading of a CA */
	public AID prepareCA(Task task, AID agentManagerAID) {
		AID caAID;

		if (task.getAgent().getModel() != null) {
			caAID = ManagerAgentService.loadAgent(agent, task.getAgent().getModel(), agentManagerAID);
			agent.log("CA model " + task.getAgent().getModel() + " resurrected");
		} else {
			String CAtype = task.getAgent().getType();
			agent.log("Sending request to create CA " + CAtype);
			caAID = ManagerAgentService.createAgent(
					agent, CAtype, CAtype, new Arguments(), agentManagerAID);
			//agent.log("CA " + CAtype + " created by " + agentManagerAID.getName());
			agent.log("CA " + CAtype + " created");
		}
		return caAID;
	}

	public void sendExecuteTask(Task task, AID agentManagerAID) {

		ExecuteTask executeTask = new ExecuteTask();
		executeTask.setTask(task);
		
		AID caAID = prepareCA(task, agentManagerAID);

		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(caAID);
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.setLanguage(agent.getCodec().getName());
		msg.setOntology(TaskOntology.getInstance().getName());

		Action executeAction = new Action(agent.getAID(), executeTask);
		try {
			agent.getContentManager().fillContent(msg, executeAction);
		} catch (CodecException e) {
			agent.logError(e.getMessage(), e);
		} catch (OntologyException e) {
			agent.logError(e.getMessage(), e);
		}

		agent.addBehaviour(new OneTaskComputingCABehaviour(agent, msg));
	}
	
	public ComputerInfo getComputerInfo(AID managerAgentAID) {
		
		GetComputerInfo getComputerInfo = new GetComputerInfo();

		agent.log("Sending request to getComputerInfo ");

		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(managerAgentAID);
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.setLanguage(agent.getCodec().getName());
		msg.setOntology(AgentManagementOntology.getInstance().getName());

		Action executeAction = new Action(agent.getAID(), getComputerInfo);
		try {
			agent.getContentManager().fillContent(msg, executeAction);
		} catch (CodecException e) {
			agent.logError(e.getMessage(), e);
		} catch (OntologyException e) {
			agent.logError(e.getMessage(), e);
		}

		ACLMessage reply = null;
		try {
			 reply = FIPAService.doFipaRequestClient(agent, msg);

		} catch (FIPAException e) {
			agent.logError(e.getMessage());
			return null;
		}
		
		try {
			Result result = (Result) agent.getContentManager().extractContent(reply);
			ComputerInfo computerInfo = (ComputerInfo) result.getValue();
			return computerInfo;
			
		} catch (UngroundedException e) {
			agent.logError(e.getMessage(), e);
		} catch (CodecException e) {
			agent.logError(e.getMessage(), e);
		} catch (OntologyException e) {
			agent.logError(e.getMessage(), e);
		}
		
		return null;
	}

}
