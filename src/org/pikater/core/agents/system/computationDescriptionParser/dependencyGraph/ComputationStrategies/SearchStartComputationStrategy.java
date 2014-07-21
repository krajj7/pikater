package org.pikater.core.agents.system.computationDescriptionParser.dependencyGraph.ComputationStrategies;

import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import org.pikater.core.agents.system.Agent_Manager;
import org.pikater.core.agents.system.computationDescriptionParser.ComputationOutputBuffer;
import org.pikater.core.agents.system.computationDescriptionParser.dependencyGraph.ComputationNode;
import org.pikater.core.agents.system.computationDescriptionParser.dependencyGraph.SearchComputationNode;
import org.pikater.core.agents.system.computationDescriptionParser.dependencyGraph.StartComputationStrategy;
import org.pikater.core.agents.system.computationDescriptionParser.edges.OptionEdge;
import org.pikater.core.agents.system.manager.StartGettingParametersFromSearch;
import org.pikater.core.ontology.SearchOntology;
import org.pikater.core.ontology.subtrees.management.Agent;
import org.pikater.core.ontology.subtrees.newOption.NewOptionList;
import org.pikater.core.ontology.subtrees.newOption.ValuesForOption;
import org.pikater.core.ontology.subtrees.newOption.base.NewOption;
import org.pikater.core.ontology.subtrees.newOption.base.Value;
import org.pikater.core.ontology.subtrees.newOption.values.QuestionMarkRange;
import org.pikater.core.ontology.subtrees.newOption.values.QuestionMarkSet;
import org.pikater.core.ontology.subtrees.newOption.values.interfaces.IValueData;
import org.pikater.core.ontology.subtrees.search.GetParameters;
import org.pikater.core.ontology.subtrees.search.searchItems.IntervalSearchItem;
import org.pikater.core.ontology.subtrees.search.searchItems.SearchItem;
import org.pikater.core.ontology.subtrees.search.searchItems.SetSItem;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: Klara
 * Date: 18.5.2014
 * Time: 11:13
 */
public class SearchStartComputationStrategy implements StartComputationStrategy{
	Agent_Manager myAgent;
	int computationId;
	int graphId;
	SearchComputationNode computationNode;


	public SearchStartComputationStrategy (Agent_Manager manager, int computationId,
			int graphId, SearchComputationNode computationNode){
		myAgent = manager;
		this.computationId = computationId;
        this.graphId = graphId;
        this.computationNode = computationNode;
	}

	public void execute(ComputationNode computation){
		ACLMessage originalRequest = myAgent.getComputation(graphId).getMessage();
		
		Agent search = getSearchFromNode();
		AID searchAID = myAgent.createAgent(search.getType());

		myAgent.addBehaviour(new StartGettingParametersFromSearch(myAgent, originalRequest, prepareRequest(searchAID), this));
    }


	public void finished(){
		// TODO mozna? Je tady neco potreba?
	}
	

	private ACLMessage prepareRequest(AID receiver){
		// prepare request for the search agent

		Map<String, ComputationOutputBuffer> inputs = computationNode.getInputs();

		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(receiver);
		msg.setLanguage(myAgent.getCodec().getName());
		msg.setOntology(SearchOntology.getInstance().getName());
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.setConversationId(Integer.toString(graphId)+"_"+Integer.toString(computationId));
		GetParameters gp = new GetParameters();
        OptionEdge childOptions= (OptionEdge)inputs.get("childoptions").getNext();
		List<SearchItem> schema = convertOptionsToSchema(childOptions.getOptions());
		gp.setSchema(schema);
		gp.setSearch_options((ArrayList<NewOption>)inputs.get("options").getNext());

        Action a = new Action();
		a.setAction(gp);
		a.setActor(myAgent.getAID());

		try {
			myAgent.getContentManager().fillContent(msg, a);
		} catch (CodecException e) {
			myAgent.logError(e.getMessage(), e);
			e.printStackTrace();
		} catch (OntologyException e) {
			myAgent.logError(e.getMessage(), e);
			e.printStackTrace();
		}

		return msg;		
	}

	private Agent getSearchFromNode(){

		Map<String,ComputationOutputBuffer> inputs = computationNode.getInputs();

		Agent agent = new Agent();
		agent.setType(computationNode.getModelClass());
		
		OptionEdge optionEdge = (OptionEdge)inputs.get("options").getNext();
	    NewOptionList options = new NewOptionList(optionEdge.getOptions());
		agent.setOptions(options.getOptions());

		return agent;
	}


	private void addOptionToSchema(NewOption opt, List schema){
        ValuesForOption values = (opt.getValuesWrapper());
		for (Value value:values.getValues()) {
            IValueData typedValue = value.getCurrentValue();
            if (typedValue instanceof QuestionMarkRange)
            {
                QuestionMarkRange questionMarkRange = (QuestionMarkRange) typedValue;
                IntervalSearchItem itm = new IntervalSearchItem();
                itm.setNumber_of_values_to_try(questionMarkRange.getCountOfValuesToTry());
                itm.setMin(questionMarkRange.getMin());
                itm.setMax(questionMarkRange.getMax());
                schema.add(itm);
            }
            else if (typedValue instanceof QuestionMarkSet)
            {
                QuestionMarkSet questionMarkSet = (QuestionMarkSet) typedValue;
                SetSItem itm = new SetSItem();
                itm.setNumber_of_values_to_try(questionMarkSet.getCountOfValuesToTry());
                itm.setSet(questionMarkSet.getValues());
                schema.add(itm);
            }
        }
	}


	//Create schema of solutions from options (Convert options->schema)

	private List convertOptionsToSchema(List<NewOption> options){
		List<NewOption> new_schema = new ArrayList<>();

		if(options==null)
			return new_schema;
		java.util.Iterator<NewOption> itr = options.iterator();
		while (itr.hasNext()) {
			NewOption opt = itr.next();
			if(opt.isMutable())
				addOptionToSchema(opt, new_schema);
		}
		return new_schema;
	}

}
