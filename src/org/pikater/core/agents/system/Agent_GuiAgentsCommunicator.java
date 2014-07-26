package org.pikater.core.agents.system;


import jade.content.lang.Codec.CodecException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

import org.pikater.core.AgentNames;
import org.pikater.core.agents.PikaterAgent;
import org.pikater.core.agents.system.guiAgentsCommunicator.GuiCommunicator;
import org.pikater.core.agents.system.manager.ManagerCommunicator;
import org.pikater.core.ontology.AccountOntology;
import org.pikater.core.ontology.BatchOntology;
import org.pikater.core.ontology.DataOntology;
import org.pikater.core.ontology.MetadataOntology;
import org.pikater.core.ontology.subtrees.batch.Batch;
import org.pikater.core.ontology.subtrees.batch.ExecuteBatch;
import org.pikater.core.ontology.subtrees.batch.batchStatuses.BatchStatuses;
import org.pikater.core.ontology.subtrees.batchDescription.ComputationDescription;

public class Agent_GuiAgentsCommunicator extends PikaterAgent {
	
	private static final long serialVersionUID = 7226837600070711675L;

	@Override
	public java.util.List<Ontology> getOntologies() {
		java.util.List<Ontology> ontologies = new java.util.ArrayList<Ontology>();
		ontologies.add(AccountOntology.getInstance());
		ontologies.add(MetadataOntology.getInstance());		
		ontologies.add(BatchOntology.getInstance());
		ontologies.add(DataOntology.getInstance());
		return ontologies;
	}

	@Override
	protected void setup() {

	  	System.out.println("Agent: " +getLocalName() + " starts.");

		initDefault();
		registerWithDF(AgentNames.GUI_AGENTS_COMMUNICATOR);

		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		
		addBehaviour(new AchieveREResponder(this, mt) {

			private static final long serialVersionUID = 1L;

			@Override
			protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
				try {
					Action a = (Action) getContentManager().extractContent(request);
					
					/**
					 * ExecuteBatch actions
					 */
					if (a.getAction() instanceof ExecuteBatch) {
						return respondToExecuteBatch(request, a);
					}
				
				} catch (OntologyException e) {
					logError("Problem extracting content: " + e.getMessage(), e);
				} catch (CodecException e) {
					logError("Codec problem: " + e.getMessage(), e);
				} catch (Exception e) {
					logError(e.getMessage(), e);
				}
	
				ACLMessage failure = request.createReply();
				failure.setPerformative(ACLMessage.FAILURE);
				logError("Failure responding to request: " + request.getContent());
				return failure;
			}
	
		});
	}
	
	private ACLMessage respondToExecuteBatch(ACLMessage request, Action a) {
		
		ExecuteBatch exeBatch = (ExecuteBatch) a.getAction();
        ComputationDescription compDescription = exeBatch.getDescription();

        String batchName = null;
        String batchNote = null;
        int batchOwnerID = -1;
        int batchPriority = -1;

		AID klaraGUI = new AID(AgentNames.GUI_KLARA_AGENT, false);
		String senderName = request.getSender().getLocalName() + "Agent";
		
			if (senderName.equals(klaraGUI.getLocalName()) ) {
				
				GuiCommunicator communicator = new GuiCommunicator();
				
				batchName = "Klara's Batch";
				batchNote = "Inputed by GuiKlara Agent";
				batchOwnerID = communicator.getUserID(this, "klara");
				batchPriority = 9;
			} else {
				
				logError("Not permitted sender");
				ACLMessage failure = request.createReply();
				failure.setPerformative(ACLMessage.FAILURE);
				return failure;
			}
			           
			log("Agent recieved ComputingDescription from " + request.getSender().getName() );
			

            Batch batch = new Batch();
            batch.setName(batchName);
            batch.setNote(batchNote);
            batch.setStatus(BatchStatuses.WAITING);
            batch.setPriority(batchPriority);
            batch.setOwnerID(batchOwnerID);
            batch.setDescription(compDescription);

            // send received ComputationDescription as Batch to DataManger to save to DB
            ManagerCommunicator communicator = new ManagerCommunicator();
            int batchId =  communicator.saveBatch(this, batch);
            log("BatchId: " + batchId + " saved");

            
            // send only NewBatch
            GuiCommunicator guiCommunicator = new GuiCommunicator();
            guiCommunicator.sendNewBatchInfoToManager(this, batchOwnerID, batchId);
            		
            
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent("OK");
            return reply;
	}
	
}



