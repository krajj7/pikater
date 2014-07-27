package org.pikater.core.agents.system;

import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;
import jade.proto.SubscriptionResponder.Subscription;
import jade.proto.SubscriptionResponder.SubscriptionManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pikater.core.AgentNames;
import org.pikater.core.agents.PikaterAgent;
import org.pikater.core.agents.system.computationDescriptionParser.dependencyGraph.SearchComputationNode;
import org.pikater.core.agents.system.computationDescriptionParser.edges.SolutionEdge;
import org.pikater.core.agents.system.manager.ComputationCollectionItem;
import org.pikater.core.agents.system.manager.ParserBehaviour;
import org.pikater.core.ontology.AccountOntology;
import org.pikater.core.ontology.AgentManagementOntology;
import org.pikater.core.ontology.BatchOntology;
import org.pikater.core.ontology.ExperimentOntology;
import org.pikater.core.ontology.FilenameTranslationOntology;
import org.pikater.core.ontology.RecommendOntology;
import org.pikater.core.ontology.ResultOntology;
import org.pikater.core.ontology.SearchOntology;
import org.pikater.core.ontology.TaskOntology;
import org.pikater.core.ontology.subtrees.file.TranslateFilename;
import org.pikater.core.ontology.subtrees.search.ExecuteParameters;
import org.pikater.core.ontology.subtrees.search.SearchSolution;


public class Agent_Manager extends PikaterAgent {

	private static final long serialVersionUID = -5140758757320827589L;

    protected Set<Subscription> subscriptions = new HashSet<>();
	private int problem_i = 0;
	public HashMap<Integer, ComputationCollectionItem> computationCollection =
			new HashMap<>();
	
	public HashMap<String, ACLMessage> searchMessages =
			new HashMap<>();
	
	public ComputationCollectionItem getComputation(Integer id){
		return computationCollection.get(id);
	}
	
	@Override
	public List<Ontology> getOntologies() {
		
		List<Ontology> ontologies = new ArrayList<>();
		ontologies.add(AccountOntology.getInstance());
		ontologies.add(RecommendOntology.getInstance());
		ontologies.add(SearchOntology.getInstance());
		ontologies.add(BatchOntology.getInstance());
		ontologies.add(ExperimentOntology.getInstance());
		ontologies.add(TaskOntology.getInstance());
		ontologies.add(FilenameTranslationOntology.getInstance());
		ontologies.add(AgentManagementOntology.getInstance());
		ontologies.add(ResultOntology.getInstance());
		
		return ontologies;
	}
	
	
	protected void setup() {

    	initDefault();

    	registerWithDF(AgentNames.MANAGER);
		doWait(3000);
				
		MessageTemplate subscriptionTemplate = 
						MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
								MessageTemplate.MatchPerformative(ACLMessage.CANCEL));
		
		addBehaviour(new ParserBehaviour(this));
				
		addBehaviour(new SubscriptionResponder(this, subscriptionTemplate, new subscriptionManager()));
		
		addBehaviour(new RequestServer(this)); // TODO - prijimani zprav od Searche (pamatovat si id nodu), od Planera
		
	} // end setup
		
			
	public class subscriptionManager implements SubscriptionManager {
		public boolean register(Subscription s) {
			subscriptions.add(s);
			return true;
		}

		public boolean deregister(Subscription s) {
			subscriptions.remove(s);
			return true;
		}
	}
	
	
	protected class RequestServer extends CyclicBehaviour {

		private static final long serialVersionUID = -6257623790759885083L;

		Ontology ontology = SearchOntology.getInstance();
		
		MessageTemplate getSchemaFromSearchTemplate =
				MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_QUERY),
				MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF),
				MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
				MessageTemplate.MatchOntology(ontology.getName()))));

		
		public RequestServer(Agent agent) {			
			super(agent);
		}

		@Override 
		public void action() {
			ACLMessage query = receive(getSchemaFromSearchTemplate);
			
			if (query != null) {
				log(": a query message received from " + query.getSender().getName());
				
				searchMessages.put(query.getConversationId(), query);				
				
				try {
					ContentElement content = getContentManager().extractContent(query);
					if (((Action) content).getAction() instanceof ExecuteParameters) {
						// manager received new options from search to execute
						ExecuteParameters ep = (ExecuteParameters) (((Action) content).getAction());

						// => fill CA's queue
						String[] ids = query.getConversationId().split("_");
						int graphId = Integer.parseInt(ids[0]);
						int nodeId = Integer.parseInt(ids[1]);
						int computationId = Integer.parseInt(ids[2]);
											
						SearchComputationNode searchNode = 
								(SearchComputationNode) computationCollection.get(graphId)
									.getProblemGraph().getNode(nodeId); 


                        for (SearchSolution ss:ep.getSolutions())
                        {
                            SolutionEdge se = new SolutionEdge();
                            se.setComputationID(computationId);
                            se.setOptions(ss);
                            searchNode.addToOutputAndProcess(se, "searchedoptions");
                        }
				    }
					else{
						logError("unknown message received.");
					}
				} catch (UngroundedException e1) {
					logError(e1.getMessage(), e1);
				} catch (CodecException e1) {
					logError(e1.getMessage(), e1);
				} catch (OntologyException e1) {
					logError(e1.getMessage(), e1);
				}
			}
			else {
				block();
			}

			/*
			ACLMessage result_msg = request.createReply();
			result_msg.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			send(result_msg);
			*/
			return;

		}
	}

	
	public void sendSubscription(ACLMessage result, ACLMessage originalMessage) {
        //TODO: get rid of this, probable te information will be taken form somewhere else
        if (true)
        {
            return;
        }
		// Prepare the subscription message to the request originator
		ACLMessage msgOut = originalMessage.createReply();
		msgOut.setPerformative(result.getPerformative());
		
		// copy content of inform message to a subscription
		try {
            ContentElement content= getContentManager().extractContent(result);
            //TODO: bad ontology
			getContentManager().fillContent(msgOut, content );
		} catch (UngroundedException e) {
			logError(e.getMessage(), e);
		} catch (CodecException e) {
			logError(e.getMessage(), e);
		} catch (OntologyException e) {
			logError(e.getMessage(), e);
		}

		// go through every subscription
		java.util.Iterator<Subscription> it = subscriptions.iterator();
		while (it.hasNext()) {
			Subscription subscription = (Subscription) it.next();

			if (subscription.getMessage().getConversationId().equals(
					"subscription" + originalMessage.getConversationId())) {
				subscription.notify(msgOut);
			}
		}
		
	} // end sendSubscription
		

	public String getHashOfFile(String nameOfFile, int userID) {
		
		TranslateFilename translate = new TranslateFilename();
		translate.setExternalFilename(nameOfFile);
		translate.setUserID(userID);

		// create a request message
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setSender(getAID());
		msg.addReceiver(new AID(AgentNames.DATA_MANAGER, false));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		msg.setLanguage(getCodec().getName());
		msg.setOntology(FilenameTranslationOntology.getInstance().getName());
		// We want to receive a reply in 30 secs
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
		//msg.setConversationId(problem.getGui_id() + agent.getLocalName());

		Action a = new Action();
		a.setAction(translate);
		a.setActor(getAID());

		try {
			getContentManager().fillContent(msg, a);

		} catch (CodecException ce) {
			ce.printStackTrace();
		} catch (OntologyException oe) {
			oe.printStackTrace();
		}


		ACLMessage reply = null;
		try {
			reply = FIPAService.doFipaRequestClient(this, msg);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ContentElement content = null;
		String fileHash = null;

		try {
			content = getContentManager().extractContent(reply);
		} catch (UngroundedException e1) {
			logError(e1.getMessage(), e1);
		} catch (CodecException e1) {
			logError(e1.getMessage(), e1);
		} catch (OntologyException e1) {
			logError(e1.getMessage(), e1);
		}

		if (content instanceof Result) {
			Result result = (Result) content;
			
			fileHash = (String) result.getValue();
		}
		
		return fileHash;
	}

	
	public AID getAgentByType(String agentType) {
		return (AID)getAgentByType(agentType, 1).get(0);
	}

	
	public List getAgentByType(String agentType, int n) {
		// returns list of AIDs
		
		List Agents = new ArrayList(); // List of AIDs
		
		// Make the list of agents of given type
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(agentType);
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			log("Found the following " + agentType + " agents:");
			
			for (int i = 0; i < result.length; ++i) {
				AID aid = result[i].getName();
				if (Agents.size() < n){
					Agents.add(aid);
				}
			}
			
			while (Agents.size() < n) {
				// create agent
				AID aid = createAgent(agentType);
				Agents.add(aid);
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
			return null;
		}
		
		return Agents;
		
	} // end getAgentByType
		
	
	private String getXMLValue(float value) {
		if (value < 0) {
			return "NA";
		}
		return Double.toString(value);
	}

	
    private String getDateTimeXML() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    

	protected String generateProblemID() {		
		return Integer.toString(problem_i++);
	}
	
}
