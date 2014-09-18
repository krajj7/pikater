package org.pikater.core.agents.system.computationDescriptionParser.dependencyGraph.ComputationStrategies;

import java.util.Map;

import org.pikater.core.agents.system.Agent_Manager;
import org.pikater.core.agents.system.computationDescriptionParser.ComputationOutputBuffer;
import org.pikater.core.agents.system.computationDescriptionParser.dependencyGraph.ComputationNode;
import org.pikater.core.agents.system.computationDescriptionParser.dependencyGraph.StartComputationStrategy;
import org.pikater.core.agents.system.computationDescriptionParser.edges.DataSourceEdge;

/**
 * User: Kuba
 * Date: 29.6.2014
 * Time: 16:12
 */
public class FileSavingStrategy implements StartComputationStrategy {
    Agent_Manager myAgent;
    int computationId;
    int graphId;
    ComputationNode computationNode;

    public FileSavingStrategy (Agent_Manager manager, int computationId,
                                       int graphId, ComputationNode computationNode){
        myAgent = manager;
        this.computationId = computationId;
        this.graphId = graphId;
        this.computationNode = computationNode;
    }

    @Override
    public void execute(ComputationNode computation) {
        myAgent.logInfo("This will be the place where the data will be saved.");
        Map<String,ComputationOutputBuffer> map = computationNode.getInputs();
        ComputationOutputBuffer buffer = map.get("file");
        if (buffer != null) {
        	DataSourceEdge dataToSave= (DataSourceEdge)buffer.getNext();
        }
        computationNode.computationFinished();
    }
}
