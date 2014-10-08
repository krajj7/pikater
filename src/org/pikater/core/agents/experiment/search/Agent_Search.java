package org.pikater.core.agents.experiment.search;

import java.util.List;
import java.util.ArrayList;

import org.pikater.core.CoreConstant;
import org.pikater.core.agents.experiment.Agent_AbstractExperiment;
import org.pikater.core.ontology.AgentInfoOntology;
import org.pikater.core.ontology.SearchOntology;
import org.pikater.core.ontology.subtrees.newOption.NewOptions;
import org.pikater.core.ontology.subtrees.newOption.base.NewOption;
import org.pikater.core.ontology.subtrees.option.GetOptions;
import org.pikater.core.ontology.subtrees.search.ExecuteParameters;
import org.pikater.core.ontology.subtrees.search.GetParameters;
import org.pikater.core.ontology.subtrees.search.SearchSolution;
import org.pikater.core.ontology.subtrees.search.searchItems.SearchItem;
import org.pikater.core.ontology.subtrees.task.Eval;
import org.pikater.core.ontology.subtrees.task.Evaluation;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

/**
 * 
 * Abstract Searcher class, gives interface and skeleton of implementation 
 *
 */
public abstract class Agent_Search extends Agent_AbstractExperiment {	

	private static final long serialVersionUID = 8637677510056974015L;
	
	private Codec codec = new SLCodec();
	private Ontology ontology = SearchOntology.getInstance();
	
	protected int queryBlockSize = 1;	

	private String conversationID;	
	private List<NewOption> searchOptions = null;
	private List<SearchItem> schema = null;
	
	/**
	 * Generates new solution
	 * 
	 * @return List of Options
	 */
	protected abstract List<SearchSolution> generateNewSolutions(
			List<SearchSolution> solutions, float[][] evaluations);
	
	/**
	 * Detects the end of searching
	 */
	protected abstract boolean isFinished();
	
	/**
	 * Updates finished state
	 * 
	 */
	protected abstract float updateFinished(float[][] evaluations);
	
	/**
	 * Load the appropriate options before sending the first parameters
	 */
	protected abstract void loadSearchOptions();
	
	
	/**
	 * Get Sets of ontologies which is using this agent
	 */
	@Override
	public List<Ontology> getOntologies() {
			
		List<Ontology> ontologies = new ArrayList<Ontology>();
		ontologies.add(SearchOntology.getInstance());
		ontologies.add(AgentInfoOntology.getInstance());
		
		return ontologies;
	}
	
	/**
	 * Setup of agent
	 */
	protected void setup() {

		initDefault();
		
		registerWithDF("Search");
		
		addBehaviour(new RequestServer(this));
		
		addAgentInfoBehaviour(getAgentInfo());

	}

	/**
	 * Get schema
	 */
	protected List<SearchItem> getSchema() {
		if(schema != null) {
			return schema;
		} else {
			return new ArrayList<SearchItem>();
		}
		
	}
	/**
	 * Get options
	 */
	protected NewOptions getSearchOptions() {

		if(searchOptions != null) {
			return new NewOptions(searchOptions);
		} else {
			return new NewOptions();
		}
	}
		
	/**
	 * Converts List of Evals to an array of values,
	 * at the moment only error_rate
	 * 
	 */
	private float[] namedEvalsToFitness(Evaluation eval) {
		
		float[] res = new float[3];
		
		Eval e0 = eval.exportEvalByName(
				CoreConstant.Error.ERROR_RATE.name());
		res[0] = e0.getValue();
		
		Eval e1 = eval.exportEvalByName(
				CoreConstant.Error.ROOT_MEAN_SQUARED.name());
		res[1] = e1.getValue();
		
		Eval e2 = eval.exportEvalByName(
				CoreConstant.Error.KAPPA_STATISTIC.name());
		res[2] = e2.getValue();
		
		return res;
	}

	/**
	 * Convert array of fitness to list of Eval
	 * 
	 */
	private List<Eval> fitnessToNamedEvals(float[] fitness) {
		
		List<Eval> evals = new ArrayList<Eval>();
		
		Eval evalErrorRate = new Eval();
		evalErrorRate.setName(CoreConstant.Error.ERROR_RATE.name());
		evalErrorRate.setValue(fitness[0]);
		evals.add(evalErrorRate);
		
		Eval evalMeanSquared = new Eval();
		evalMeanSquared.setName(CoreConstant.Error.ROOT_MEAN_SQUARED.name());			
		evalMeanSquared.setValue(fitness[1]);				
		evals.add(evalMeanSquared);
		
		Eval evalKappa = new Eval();
		evalKappa.setName(CoreConstant.Error.KAPPA_STATISTIC.name());
		evalKappa.setValue(fitness[2]);
		evals.add(evalKappa);
		
		return evals;
	}
	
	private class RequestServer extends AchieveREResponder {
		
		private static final long serialVersionUID = 6214306716273574418L;
		
		GetOptions getOptionAction;
		GetParameters getNextParametersAction;
		
		public RequestServer(Agent a) {
			super(a,
					MessageTemplate.and(MessageTemplate.MatchProtocol(
							FIPANames.InteractionProtocol.FIPA_REQUEST),
					MessageTemplate.and(MessageTemplate.MatchPerformative(
							ACLMessage.REQUEST),
					MessageTemplate.and(MessageTemplate.MatchLanguage(
							codec.getName()),
					MessageTemplate.MatchOntology(
							ontology.getName()))))
							);
		
			
			this.registerPrepareResultNotification(new Behaviour(a) {

				private static final long serialVersionUID = -5801676857376453194L;

				boolean cont;
				List<SearchSolution> solutionsNew = null;
				float[][] evaluations = null;
				int queriesToProcess = 0;
				
				@Override
				public void action() {
					
					cont = false;
					
					if (getOptionAction != null) {
						ACLMessage request = (ACLMessage)
								getDataStore().get(REQUEST_KEY);
						ACLMessage reply =
								getParameters(request);
						
						getDataStore().put(RESULT_NOTIFICATION_KEY, reply);
						return;
						
					} else if (getNextParametersAction != null) {
						cont = true;
						ACLMessage requestMsg = (ACLMessage)
								getDataStore().get(REQUEST_KEY);
						
						// finished or starts new cycle of query
						if (queriesToProcess == 0) {
							
							if(solutionsNew != null) {
								updateFinished(evaluations);
							}

							if (isFinished()) {
								cont = false;
								finishedEvaluations();
								
							} else {
								newWaveOfEvaluation(requestMsg);
							}
							
						} else {
							waitingForEvaluation();
						}
							
					}
					//handle informs as query results
				}

				/**
				 * All evaluations finished
				 */
				private void finishedEvaluations() {
					
					solutionsNew = null;
					evaluations = null;
					
					// send the best error back to manager and 
					// to a recommender, if recommender's present

					ACLMessage originalRequest =
							(ACLMessage)getDataStore().get(REQUEST_KEY); 
					ACLMessage reply = originalRequest.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					
					try {			

						Action action = (Action)myAgent.getContentManager()
								.extractContent(originalRequest);								
						
						Evaluation evaluation = new Evaluation();
						float[] f = new float[3];
						f[0] = updateFinished(evaluations);
						evaluation.setEvaluations(fitnessToNamedEvals(f));
						
						Result result = new Result(action, evaluation);			

						getContentManager().fillContent(reply, result);
						
					} catch (CodecException e) {
						logException(e.getMessage(), e);
					} catch (OntologyException e) {
						logException(e.getMessage(), e);
					}
																					
					getDataStore().put(RESULT_NOTIFICATION_KEY, reply);
				}
				
				/**
				 * New wave of evaluation - generation of query
				 * 
				 */
				private void newWaveOfEvaluation(ACLMessage requestMsg) {
					
					solutionsNew = generateNewSolutions(solutionsNew, evaluations);
					if(solutionsNew != null) {
						evaluations = new float[solutionsNew.size()][];
					} else
						solutionsNew = new ArrayList<>();
					queriesToProcess = solutionsNew.size();
					
					for (int i = 0; i < solutionsNew.size(); i++){
						//sends queries
						ExecuteParameters execParameters =
								new ExecuteParameters();

						List<SearchSolution> solutionList =
								new ArrayList<SearchSolution>(1);
						
						solutionList.add(solutionsNew.get(i));
						execParameters.setSolutions(solutionList);

						Action action = new Action();
						action.setAction(execParameters);
						action.setActor(getAID());

						ACLMessage query = new ACLMessage(ACLMessage.QUERY_REF);
						query.addReceiver(requestMsg.getSender());
						query.setLanguage(codec.getName());
						query.setOntology(ontology.getName());
						query.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
						
						//query and answer identification
						query.setConversationId(conversationID + "_" + Integer.toString(i));
						try {
							getContentManager().fillContent(query, action);
						} catch (CodecException e) {
							logException(e.getMessage(), e);
						} catch (OntologyException e) {
							logException(e.getMessage(), e);
						}
						myAgent.send(query);

					}
				}
				
				/**
				 * Waiting for evaluation
				 */
				private void waitingForEvaluation() {
					
					MessageTemplate template =
							MessageTemplate.MatchPerformative(
									ACLMessage.INFORM);
					ACLMessage response = myAgent.receive(template);
					
					if(response == null) {
						// any inform message -wait
						block();
					} else {
						// received evaluation - answer for query
						
						if (response.getConversationId().split("_").length <= 2) {
							// other informs than containing CA results (error)
							return;
						}
						
						// connect inform with the right query by using ID
						int id = Integer.parseInt(
								response.getConversationId().split("_")[2]);

						try {
							Result res = (Result)
									getContentManager().extractContent(response);
							Evaluation eval = (Evaluation) res.getValue();
							evaluations[id] = namedEvalsToFitness(eval);
							
						} catch (UngroundedException e) {
							logException(e.getMessage(), e);
						} catch (CodecException e) {
							logException(e.getMessage(), e);
						} catch (OntologyException e) {
							logException(e.getMessage(), e);
						}
							queriesToProcess--;
					}

				}

				@Override
				public boolean done() {
					return !cont;
				}
				
			});
		}
		
		@Override
		protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException{
			
			getOptionAction = null;
			getNextParametersAction = null;
			
			try {
				ContentElement content =
						getContentManager().extractContent(request);

				Concept a = ((Action) content).getAction();
				
				if (a instanceof GetOptions) {
					getOptionAction = (GetOptions) a;
					return null;
					
				} else if (a instanceof GetParameters){
					getNextParametersAction = (GetParameters) a;
					
					// setting options
					searchOptions = getNextParametersAction.getSearchOptions();
					schema = getNextParametersAction.getSchema();														
					loadSearchOptions();
					
					conversationID = request.getConversationId(); 
							
					ACLMessage agree = request.createReply();
					agree.setPerformative(ACLMessage.AGREE);
					agree.setContent(Integer.toString(queryBlockSize));
					return agree;
				}				
			} catch (UngroundedException e) {
				logException(e.getMessage(), e);
			} catch (CodecException e) {
				logException(e.getMessage(), e);
			} catch (OntologyException e) {
				logException(e.getMessage(), e);
			}
			throw new NotUnderstoodException("Not understood");
		}

	}
		

	/**
	 * Handle request GetParameters
	 * @param request - message
	 * @return message
	 */
	protected ACLMessage getParameters(ACLMessage request) {
		
		org.pikater.core.ontology.subtrees.management.Agent agent =
				new org.pikater.core.ontology.subtrees.management.Agent();
		
		agent.setName(getLocalName());
		agent.setType(getAgentType());
		agent.setOptions(getAgentInfo().getOptions().getOptions());

		ACLMessage reply = request.createReply();
		reply.setPerformative(ACLMessage.INFORM);
		
		try {
			// Prepare the content
			ContentElement content = getContentManager()
					.extractContent(request);
			Result result = new Result((Action) content, agent);

			getContentManager().fillContent(reply, result);
			

		} catch (CodecException e) {
			logException(e.getMessage(), e);
			reply.setPerformative(ACLMessage.FAILURE);
			reply.setContent(e.getMessage());
		} catch (OntologyException e) {
			logException(e.getMessage(), e);
			reply.setPerformative(ACLMessage.FAILURE);
			reply.setContent(e.getMessage());
		}
		
		return reply;
	}

}
