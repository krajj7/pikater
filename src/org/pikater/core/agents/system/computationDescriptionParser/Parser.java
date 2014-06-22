package org.pikater.core.agents.system.computationDescriptionParser;

import jade.content.Concept;
import org.pikater.core.agents.system.Agent_Manager;
import org.pikater.core.agents.system.computationDescriptionParser.dependencyGraph.*;
import org.pikater.core.agents.system.computationDescriptionParser.dependencyGraph.ComputationStrategies.CAStartComputationStrategy;
import org.pikater.core.agents.system.computationDescriptionParser.edges.DataSourceEdge;
import org.pikater.core.agents.system.computationDescriptionParser.edges.EdgeValue;
import org.pikater.core.agents.system.computationDescriptionParser.edges.ErrorEdge;
import org.pikater.core.agents.system.computationDescriptionParser.edges.OptionEdge;
import org.pikater.core.ontology.subtrees.batchDescription.*;
import org.pikater.core.ontology.subtrees.option.Option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Parser {
    private ComputationGraph computationGraph=new ComputationGraph();
    private HashMap<Concept,ComputationNode> alreadyProcessed=new HashMap<>();
    private Agent_Manager agent = null;

    public Parser(Agent_Manager agent_) {
        this.agent = agent_;
    }

    public void parseRoot(IDataSaver dataSaver) {
        agent.log("Ontology Parser - IDataSaver");
        //Ontology root is Leaf in Computation
        parseSaver(dataSaver);
    }

    private void parseSaver(IDataSaver dataSaver) {
        if (dataSaver instanceof FileDataSaver) {
            agent.log("Ontology Matched - FileDataSaver");

            FileDataSaver fileDataSaver = (FileDataSaver) dataSaver;
            DataSourceDescription dataSource = fileDataSaver.getDataSource();
            //TODO: update strategy to file saving one
            FileSaverNode saverNode=new FileSaverNode(new DummyComputationStrategy());
            computationGraph.addNode(saverNode);
            alreadyProcessed.put(dataSaver,saverNode);
            parseDataSourceDescription(dataSource, saverNode, "file");
        } else {
            agent.logError("Ontology Parser - Error unknown IDataSaver");
        }
    }

    private void parseDataSourceDescription(DataSourceDescription dataSource, ComputationNode child, String connectionName) {
        agent.log("Ontology Parser - DataSourceDescription");

        IDataProvider dataProvider = dataSource.getDataProvider();
        this.parseDataProvider(dataProvider, child, connectionName);
    }

    public void parseDataProvider(IDataProvider dataProvider, ComputationNode child, String connectionName) {
        agent.log("Ontology Parser - IDataProvider");
        ComputationNode parent;
        if (dataProvider instanceof FileDataProvider) {
            agent.log("Ontology Matched - FileDataProvider");

            FileDataProvider fileData = (FileDataProvider) dataProvider;
            this.parseFileDataProvider(fileData, child, connectionName);
            return;
        }  else if (dataProvider instanceof ComputingAgent) {
            agent.log("Ontology Matched - ComputingAgent");

            ComputingAgent computingAgent = (ComputingAgent) dataProvider;
            parent=parseComputing(computingAgent);
        }
        else if (dataProvider instanceof CARecSearchComplex)
        {
            agent.log("Ontology Matched - CARecSearchComplex");

            CARecSearchComplex complex = (CARecSearchComplex) dataProvider;
            parent=parseComplex(complex);
        }
        else if (dataProvider instanceof DataProcessing) {
            agent.log("Ontology Matched - DataProcessing");

            //TODO:  parseSaver(postprocessing) DataProcessing postprocessing = (DataProcessing) dataProvider;;
            return;
        } else {
            agent.log("Ontology Matched - Error unknown IDataProvider");
            return;
        }
        //handle parent - set him as file receiver
        ComputationOutputBuffer<EdgeValue> fileBuffer=new StandardBuffer<>(parent,child);
        parent.addBufferToOutput(connectionName,fileBuffer);
        child.addInput(connectionName,fileBuffer);
    }

    public void parseErrors(ErrorDescription errorDescription, ComputationNode child) {
        agent.log("Ontology Parser - IErrorProvider");
        IErrorProvider errorProvider=errorDescription.getProvider();

        ComputationNode errorNode=null;
        //uncomment after the bug with references is fixed
//        if (alreadyProcessed.containsKey(errorProvider))
//        {
//            errorNode=alreadyProcessed.get(errorProvider);
//        }
//        else  {
//
//            agent.log("Error provider was not parsed at the moment parseErrors was called");
//            return;
//        }

    //hack -delete after reference bug is fixed
        for (Concept c : alreadyProcessed.keySet())
        {
            if (c instanceof ComputingAgent)
            {
                ComputingAgent ca=(ComputingAgent)c;
                if (ca.getAgentType().equals(((ComputingAgent)errorProvider).getAgentType()) )
                {
                    errorNode=alreadyProcessed.get(c);
                    break;
                }
            }
        }
        StandardBuffer<ErrorEdge> buffer=new StandardBuffer<>(errorNode,child);
        errorNode.addBufferToOutput(errorDescription.getType(),buffer);
        child.addInput(errorDescription.getType(),buffer);
    }

    //This is the root of all parsing
    public void parseRoots(ComputationDescription comDescription) {
        agent.log("Ontology Parser - ComputationDescription");

        List<FileDataSaver> elements = comDescription.getRootElements();
        for (FileDataSaver fileSaverI : elements) {
        	this.parseRoot(fileSaverI);
        }
    }

    //Processes a node that is in the beginning of computation - reads file
    public void parseFileDataProvider(FileDataProvider file, ComputationNode child, String connectionName) {
        agent.log("Ontology Parser - FileDataProvider");
        if (!alreadyProcessed.containsKey(file))
        {
            alreadyProcessed.put(file, null);
            DataSourceEdge fileEdge=new DataSourceEdge();
            fileEdge.setFile(true);
            fileEdge.setDataSourceId(file.getFileURI());
            ComputationOutputBuffer<EdgeValue> buffer=new NeverEndingBuffer<EdgeValue>(fileEdge);
            buffer.setTarget(child);
            child.addInput(connectionName,buffer);
        }
    }

    public ModelComputationNode parseComputing(IComputingAgent computingAgent)
    {
        agent.log("Ontology Parser - Computing Agent Simple");

        if (!alreadyProcessed.containsKey(computingAgent))
        {
            ModelComputationNode node= new ModelComputationNode();
            CAStartComputationStrategy strategy=new CAStartComputationStrategy(agent,node.getId(),1,node);
            node.setStartBehavior(strategy);
            alreadyProcessed.put(computingAgent,node);

        }
        ModelComputationNode computingNode = (ModelComputationNode) alreadyProcessed.get(computingAgent);
        computationGraph.addNode(computingNode);

        ComputingAgent computingAgentO = (ComputingAgent) computingAgent;
        computingNode.setModelClass(computingAgentO.getAgentType());
        ArrayList<Option> options=new ArrayList<Option>();
        for (Option o:computingAgentO.getOptions())
        {
            options.add(o);
        }
        addOptionsToInputs(computingNode,options);
        fillDataSources(computingAgentO,computingNode);

        return computingNode;
    }

    public ComputationNode parseComplex(CARecSearchComplex complex) {
        agent.log("Ontology Parser - CARecSearchComplex");

        ComputationNode computingNode;
        IComputingAgent iComputingAgent = complex.getComputingAgent();
        if (iComputingAgent instanceof CARecSearchComplex)
        {
            computingNode=parseComplex((CARecSearchComplex)iComputingAgent);
        }
        else
        {
            computingNode=parseComputing(iComputingAgent);
        }
        addOptionsToInputs(computingNode,complex.getOptions());

        Recommend recommenderO = complex.getRecommender();
        if (recommenderO!=null)
        {
            parseRecommender(recommenderO, computingNode);
        }
        if ( iComputingAgent instanceof ComputingAgent) {
            fillDataSources((ComputingAgent) iComputingAgent, computingNode);
        }

        Search searchAgentO = complex.getSearch();
        if (searchAgentO!=null)
        {
            return parseSearch(searchAgentO, computingNode, complex.getErrors());
        }
        return computingNode;
    }

    public SearchComputationNode parseSearch(Search search, ComputationNode child, List<ErrorDescription> errors) {
        agent.log("Ontology Parser - Search");

        if (!alreadyProcessed.containsKey(search))
        {
            alreadyProcessed.put(search, new SearchComputationNode());

        }
        SearchComputationNode searchNode= (SearchComputationNode) alreadyProcessed.get(search);
        StandardBuffer searchBuffer=new StandardBuffer(searchNode,child);
        searchNode.addBufferToOutput("searchedoptions",searchBuffer);
        child.addInput("searchedoptions",searchBuffer);
        computationGraph.addNode(searchNode);
        alreadyProcessed.put(search,searchNode);
        addOptionsToInputs(searchNode, search.getOptions());
        for (ErrorDescription error:errors)
        {
            parseErrors(error,searchNode);
        }

        return searchNode;
    }

    public void parseRecommender(Recommend recommender, ComputationNode child) {
        agent.log("Ontology Parser - Recommender");

        RecommenderComputationNode recNode=new RecommenderComputationNode();
        StandardBuffer recBuffer=new StandardBuffer(recNode,child);
        recNode.addBufferToOutput("recommender",recBuffer);
        child.addInput("recommender",recBuffer);
        computationGraph.addNode(recNode);
        alreadyProcessed.put(recommender,recNode);
        List<Option> options = recommender.getOptions();

        addOptionsToInputs(recNode, options);
        recNode.setRecommenderClass(recommender.getRecommenderClass());
    }

    private void fillDataSources(ComputingAgent compAgent,ComputationNode node) {
        DataSourceDescription trainingData = compAgent.getTrainingData();
        DataSourceDescription testingData = compAgent.getTestingData();
        DataSourceDescription validationData = compAgent.getValidationData();

        if (trainingData!=null) {
            parseDataSourceDescription(trainingData, node, "training");
        }
        if (testingData!=null) {
            parseDataSourceDescription(testingData, node, "testing");
        }
        if (validationData!=null) {
            parseDataSourceDescription(validationData, node, "validation");
        }
    }

    public ComputationGraph getComputationGraph() {
        return computationGraph;
    }

    private void addOptionsToInputs(ComputationNode node,List<Option> options)
    {
        OptionEdge option=new OptionEdge();
        option.setOptions(options);
        OneShotBuffer optionBuffer=new OneShotBuffer(option);
        node.addInput("options",optionBuffer);
    }
}
