package org.pikater.core.ontology;

import jade.content.onto.BeanOntology;
import jade.content.onto.Ontology;

import org.pikater.core.ontology.subtrees.agent.AgentClass;
import org.pikater.core.ontology.subtrees.agent.NewAgent;
import org.pikater.core.ontology.subtrees.agentinfo.AgentInfo;
import org.pikater.core.ontology.subtrees.agentinfo.GetOptions;
import org.pikater.core.ontology.subtrees.model.Model;
import org.pikater.core.ontology.subtrees.newoption.base.NewOption;
import org.pikater.core.ontology.subtrees.newoption.base.ValueType;
import org.pikater.core.ontology.subtrees.newoption.restrictions.IRestriction;
import org.pikater.core.ontology.subtrees.newoption.values.BooleanValue;
import org.pikater.shared.logging.core.ConsoleLogger;


public class AgentInfoOntology extends BeanOntology {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7595856728415263726L;

	private AgentInfoOntology() {
        super("AgentInfoOntology");

        String optionPackage = NewOption.class.getPackage().getName();
        String restrictionPackage = IRestriction.class.getPackage().getName();
        String typePackage = ValueType.class.getPackage().getName();        
        String valuePackage = BooleanValue.class.getPackage().getName();
        
        String agentInfoPackage = AgentInfo.class.getPackage().getName();
        String modelPackage = Model.class.getPackage().getName();
        
        String agentPackage = NewAgent.class.getPackage().getName();
        
        try {
            add(optionPackage);
            add(restrictionPackage);
            add(typePackage);
            add(valuePackage);
            
            add(agentInfoPackage);
            add(modelPackage);
            
            add(agentPackage);

            add(GetOptions.class);
            add(AgentClass.class);
            
        } catch (Exception e) {
        	ConsoleLogger.logThrowable("Unexpected error occured:", e);
        }
    }

    static AgentInfoOntology theInstance = new AgentInfoOntology();

    public static Ontology getInstance() {
        return theInstance;
    }
}
