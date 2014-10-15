package org.pikater.core.agents.experiment.search;


import org.pikater.core.ontology.subtrees.agentInfo.AgentInfo;
import org.pikater.core.ontology.subtrees.newOption.NewOptions;
import org.pikater.core.ontology.subtrees.newOption.base.NewOption;
import org.pikater.core.ontology.subtrees.newOption.values.FloatValue;
import org.pikater.core.ontology.subtrees.newOption.values.IntegerValue;
import org.pikater.core.ontology.subtrees.newOption.values.StringValue;
import org.pikater.core.ontology.subtrees.newOption.values.interfaces.IValueData;
import org.pikater.core.ontology.subtrees.search.SearchSolution;
import org.pikater.core.ontology.subtrees.search.searchItems.SearchItem;
import org.pikater.core.options.search.EASearch_Box;
import org.pikater.core.utilities.evolution.MergingReplacement;
import org.pikater.core.utilities.evolution.Population;
import org.pikater.core.utilities.evolution.RandomNumberGenerator;
import org.pikater.core.utilities.evolution.Replacement;
import org.pikater.core.utilities.evolution.individuals.Individual;
import org.pikater.core.utilities.evolution.individuals.SearchItemIndividual;
import org.pikater.core.utilities.evolution.multiobjective.NSGAFitnessEvaluator;
import org.pikater.core.utilities.evolution.operators.OnePtXOver;
import org.pikater.core.utilities.evolution.operators.Operator;
import org.pikater.core.utilities.evolution.operators.SearchItemIndividualMutation;
import org.pikater.core.utilities.evolution.selectors.BestIndividualsSelector;
import org.pikater.core.utilities.evolution.selectors.Selector;
import org.pikater.core.utilities.evolution.selectors.TournamentSelector;
import org.pikater.core.utilities.evolution.surrogate.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Agent_EASearch extends Agent_Search {
    /*
     * Implementation of Genetic algorithm search
     * Half uniform crossover, tournament selection
     * Options:
     * -E float
     * minimum error rate (default 0.1)
     * 
     * -M int 
     * maximal number of generations (default 10)
     * 
     * -I int
     * maximal number of evaluated configurations (default 100)
     * 
     * -T float
     * Mutation rate (default 0.2)
     * 
     * -F float
     * Mutation rate per field in individual (default 0.2)
     * 
     * -X float
     * Crossover probability (default 0.5)
     * 
     * -P int
     * population size (default 10)
     * 
     * -L float
     * The percentage of elite individuals (default 0.1)
     * 
     * 
     */

    //fitness is the error rate - the lower, the better!
    SearchItemIndividualArchive archive = new SearchItemIndividualArchive();
    Population parents;
    Population offspring;
    Population toEvaluate = new Population();
    Population evaluated = new Population();
    Replacement replacement = new MergingReplacement();
    ArrayList<Selector> environmentalSelectors;
    ArrayList<Selector> matingSelectors;
    ArrayList<Operator> operators;
    boolean multiobjective = true;
    boolean surrogate = false;
    double eliteSize = 0.1;
    int popSize = 10;
    double mutProb = 0.0;
    double mutProbPerField = 0.0;
    double xOverProb = 0.0;
    private int genNumber = 0;
    private double bestError = Double.MAX_VALUE;
    private int maxGeneration = 5;
    private double goalError = 0.1;
    private int maxEval = 100;
    protected Random rng = RandomNumberGenerator.getInstance().getRandom();
    /**
     *
     */
    private static final long serialVersionUID = -387458001824777077L;
    

    @Override
    protected String getAgentType() {
        return "EASearch";
    }
    
	@Override
	protected AgentInfo getAgentInfo() {

		return EASearch_Box.get();
	}

    @Override
    protected boolean isFinished() {
        //number of generations, best error rate
        
        if (genNumber >= maxGeneration) {
            return true;
        }

        if (bestError <= goalError) {
            return true;
        }
        
        if (archive.size() >= maxEval) 
            return true;
        
        return false;
    }
    
    @Override
    protected List<SearchSolution> generateNewSolutions(
    		List<SearchSolution> solutions, float[][] evaluations) {

        offspring = new Population();
        offspring.setPopulationSize(popSize);
        
        genNumber++;
        
        if (evaluations == null) {
            //create new population
            
            matingSelectors = new ArrayList<Selector>();
            environmentalSelectors = new ArrayList<Selector>();
            operators = new ArrayList<Operator>();
            archive = new SearchItemIndividualArchive();
            
            if (!multiobjective) {
                environmentalSelectors.add(new TournamentSelector());            
            }
            else {
                environmentalSelectors.add(new BestIndividualsSelector());
                eliteSize = 0.0;
            }
                
            operators.add(new OnePtXOver(xOverProb));
            
            SearchItemIndividualMutation mutationOperator =
            		new SearchItemIndividualMutation(
            				mutProb, mutProbPerField, 0.3);
            operators.add(mutationOperator);
            
            if (surrogate && !multiobjective) {
            	SurrogateMutationOperator operator =
            			new SurrogateMutationOperator(
            					archive, 0.25,
            					new FitnessModelValueProvider(),
            					new IdentityNormalizer()
            					);
                operators.add(operator);
            }
            
            if (surrogate && multiobjective) {
            	SurrogateMutationOperator operator =
            			new SurrogateMutationOperator(
            					archive, 0.25,
            					new ASMMOMAModelValueProvider(),
            					new LogarithmicNormalizer()
            					);
                operators.add(operator);
            }
            
            parents = new Population();
            parents.setPopulationSize(popSize);
            
            List<SearchItem> schema = getSchema();
            
            SearchItemIndividual sampleIndividual =
            		new SearchItemIndividual(schema.size());
            
            for (int i = 0; i < schema.size(); i++) {
                sampleIndividual.set(i, new StringValue(""));
                sampleIndividual.setSchema(i, schema.get(i));
            }
            
            parents.setSampleIndividual(sampleIndividual);
            
            genNumber = 0;
            bestError = Double.MAX_VALUE;
            parents.createRandomInitialPopulation();
            
            return populationToList(parents);
        }
        
        Population matingPool = new Population();

        if (matingSelectors.size() > 0) {
            int mateSel = matingSelectors.size();
            int toSelect = parents.getPopulationSize()/mateSel;
            for (int i = 0; i < matingSelectors.size(); i++) {
                Population sel = new Population();
                matingSelectors.get(i).select(toSelect, parents, sel);
                matingPool.addAll((Population)sel.clone());
            }

            int missing = parents.getPopulationSize()
            		-matingPool.getPopulationSize();
            
            if (missing > 0) {
                Population sel = new Population();
                Selector selector =
                		matingSelectors.get(matingSelectors.size()-1);
                selector.select(toSelect, parents, sel);
                matingPool.addAll((Population)sel.clone());
            }
        } else {
            matingPool = (Population)parents.clone();
        }
        
        offspring = null;
        for (Operator o : operators) {
            offspring = new Population();
            o.operate(matingPool, offspring);
            matingPool = offspring;
        }
        
        toEvaluate.clear();
        evaluated.clear();
        
        for (int i = 0; i < offspring.getPopulationSize(); i++) {
            if (archive.contains((SearchItemIndividual)offspring.get(i))) {
            	
            	SearchItemIndividual itemI =
            			(SearchItemIndividual) offspring.get(i);
            	double fitness = archive.getFitness(itemI);
            	
                offspring.get(i).setFitnessValue(fitness);
                evaluated.add(offspring.get(i));
                continue;
            }
            toEvaluate.add(offspring.get(i));
        }
        
        return populationToList(toEvaluate);
        
    }

    private List<SearchSolution> populationToList(Population pop) {
        
        List<SearchSolution> ret = new ArrayList<SearchSolution>();
        for (Individual i : pop.getSortedIndividuals()) {
            SearchItemIndividual si = (SearchItemIndividual)i;
            List<IValueData> vals = new ArrayList<IValueData>();
            
            for (int j = 0; j < si.length(); j++) {
                vals.add(si.get(j));
            }
            
            SearchSolution ss = new SearchSolution();
            ss.setValues(vals);
            ret.add(ss);
        }
        return ret;
    }
    
    @Override
    protected float updateFinished(float[][] evaluations) {        
        //assign evaluations to the population as fitnesses		
        if (evaluations == null) {
            for (int i = 0; i < popSize; i++) {
                offspring.get(i).setFitnessValue(1);
            }
            return (float) bestError;
        }
        
        //initial generation -- evaluate the random population
        if (genNumber == 0) {
            for (int i = 0; i < evaluations.length; i++) {
                parents.get(i).setFitnessValue(evaluations[i][0]);
                
                SearchItemIndividual itemI =
                		(SearchItemIndividual)parents.get(i);		
                itemI.setObjectives(evaluations[i]);
                if (evaluations[i][0] < bestError) {
                    bestError = evaluations[i][0];
                }
                archive.add((SearchItemIndividual)parents.get(i));
            }
            return (float) bestError;
        }
        
        for (int i = 0; i < evaluations.length; i++) {
            toEvaluate.get(i).setFitnessValue(evaluations[i][0]);
            
            SearchItemIndividual itemI =
            		(SearchItemIndividual)toEvaluate.get(i);
            itemI.setObjectives(evaluations[i]);
            if (evaluations[i][0] < bestError) {
                bestError = evaluations[i][0];
            }
            archive.add((SearchItemIndividual)toEvaluate.get(i));
        }
        
        offspring.clear();
        offspring.addAll(toEvaluate);
        offspring.addAll(evaluated);
        
        Population selected = new Population();

        ArrayList<Individual> sortedOld = parents.getSortedIndividuals();
        for (int i = 0; i < eliteSize*parents.getPopulationSize(); i++) {
            selected.add(sortedOld.get(i));
        }
        
        Population combined = replacement.replace(parents, offspring);
        
        if (multiobjective) {
            NSGAFitnessEvaluator NSGAfit = new NSGAFitnessEvaluator();
            NSGAfit.evaluate(combined);
        }
        
        int envSel = environmentalSelectors.size();
        int toSelect = (parents.getPopulationSize()
        		-selected.getPopulationSize())/envSel;
        for (int i = 0; i < environmentalSelectors.size(); i++) {
            Population sel = new Population();
            environmentalSelectors.get(i).select(toSelect, combined, sel);
            selected.addAll((Population)sel.clone());
        }

        int missing = parents.getPopulationSize() - selected.getPopulationSize();
        if (missing > 0) {
            Population sel = new Population();
            
            Selector selector = environmentalSelectors
            		.get(environmentalSelectors.size() - 1);
            selector.select(toSelect, combined, sel);
            selected.addAll((Population)sel.clone());
        }

        parents.clear();
        parents.addAll(selected);
        
        return (float) bestError;
    }

    @Override
    protected void loadSearchOptions() {
    	
        popSize = 10;
        mutProb = 0.2;
        xOverProb = 0.5;
        maxGeneration = 5;
        goalError = 0.02;

        NewOptions options = getSearchOptions();
        
        if (options.containsOptionWithName("E")) {
	        NewOption optionE = options.fetchOptionByName("E");
	        FloatValue valueE =
	        		(FloatValue) optionE.toSingleValue().getCurrentValue();
	        goalError = valueE.getValue();
        }
        if (options.containsOptionWithName("M")) {
	        NewOption optionM = options.fetchOptionByName("M");
	        IntegerValue valueM =
	        		(IntegerValue) optionM.toSingleValue().getCurrentValue();
	        maxGeneration = valueM.getValue();
        }
        if (options.containsOptionWithName("T")) {
	        NewOption optionT = options.fetchOptionByName("T");
	        FloatValue valueT =
	        		(FloatValue) optionT.toSingleValue().getCurrentValue();
	        mutProb = valueT.getValue();
        }
        if (options.containsOptionWithName("X")) {
	        NewOption optionX = options.fetchOptionByName("X");
	        FloatValue valueX =
	        		(FloatValue) optionX.toSingleValue().getCurrentValue();
	        xOverProb = valueX.getValue();
        }
        if (options.containsOptionWithName("P")) {
	        NewOption optionP = options.fetchOptionByName("P");
	        IntegerValue valueP =
	        		(IntegerValue) optionP.toSingleValue().getCurrentValue();
	        popSize = valueP.getValue();
        }
        if (options.containsOptionWithName("I")) {
	        NewOption optionI = options.fetchOptionByName("I");
	        IntegerValue valueI =
	        		(IntegerValue) optionI.toSingleValue().getCurrentValue();
	        maxEval = valueI.getValue();
        }
        if (options.containsOptionWithName("F")) {
	        NewOption optionF = options.fetchOptionByName("F");
	        FloatValue valueF =
	        		(FloatValue) optionF.toSingleValue().getCurrentValue();
	        mutProbPerField = valueF.getValue();
        }

        if (options.containsOptionWithName("L")) {
	        NewOption optionL = options.fetchOptionByName("L");
	        FloatValue valueL =
	        		(FloatValue) optionL.toSingleValue().getCurrentValue();
	        eliteSize = valueL.getValue();
        }
        
        queryBlockSize = popSize;

    }

}