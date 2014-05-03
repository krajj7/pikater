package org.pikater.core.agents.experiment.computing;

import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.RBFNetwork;
import weka.core.Instances;

import java.util.Date;
import java.util.Random;

import org.pikater.core.ontology.agentInfo.AgentInfo;
import org.pikater.core.ontology.messages.DataInstances;
import org.pikater.core.ontology.messages.Eval;
import org.pikater.core.ontology.messages.EvaluationMethod;
import org.pikater.core.ontology.messages.Instance;

public abstract class Agent_WekaAbstractCA extends Agent_ComputingAgent {

	private static final long serialVersionUID = -3594051562022044000L;
	
	private String DURATION_SERVICE_REGRESSION = "DurationServiceRegression";
	private String DurationServiceRegression_output_prefix = "  --d-- ";

	
	protected abstract Classifier getClassifierClass();

	protected abstract AgentInfo getAgentInfo();

	@Override
	protected void getParameters() {
		//TODO
	}

	@Override
	public String getAgentType() {
		
		Classifier classifier = new RBFNetwork();
		
		return classifier.getClass().getName();
	}
	
	@Override
	protected Date train(org.pikater.core.ontology.messages.Evaluation evaluation) throws Exception {
		working = true;
						
		if (getLocalName().equals(DURATION_SERVICE_REGRESSION)){
				log(DurationServiceRegression_output_prefix, 2);
		}
		log("Training...", 2);
				

		if(getClassifierClass() == null)
			throw new Exception(getLocalName() + ": Weka classifier class hasn't been created (Wrong type?).");
		if (OPTIONS.length > 0) {
			getClassifierClass().setOptions(OPTIONS);
		}
		
		long start = System.currentTimeMillis();
		getClassifierClass().buildClassifier(train);
		long end = System.currentTimeMillis();
		long duration = end - start;
		if (duration < 1) { duration = 1; } 
		
		List evals = new ArrayList();
		
		Eval s = new Eval();
		s.setName("start");
		s.setValue(start);
		evals.add(s);
		
		Eval d = new Eval();
		d.setName("duration");
		d.setValue(duration);
		evals.add(d);

		if (getLocalName().equals(DURATION_SERVICE_REGRESSION)){
			log(DurationServiceRegression_output_prefix, 2);
		}
		log("start: " + new Date(start) + " : duration: " + duration, 2);
		
		state = states.TRAINED; // change agent state
		OPTIONS = getClassifierClass().getOptions();

		// write out net parameters
		if (getLocalName().equals(DURATION_SERVICE_REGRESSION)){
                log(DurationServiceRegression_output_prefix+getOptions(),2);
		}
		else{
			log(getOptions(), 1);
		}
		
		working = false;
		
		// add evals to Evaluation
		List evaluations_new = evaluation.getEvaluations();
		
		Iterator itr = evals.iterator();
		while (itr.hasNext()) {
			Eval eval = (Eval) itr.next();
			evaluations_new.add(eval);
		}
		
		evaluation.setEvaluations(evaluations_new);
		
		return new Date(start);
	}


	private Evaluation test(EvaluationMethod evaluation_method) throws Exception{
		working = true;
		log("Testing...", 2);

		// evaluate classifier and print some statistics
		Evaluation eval = new Evaluation(train);

		log("Evaluation method: \t", 2);
		
		if (evaluation_method.getName().equals("CrossValidation") ){
			int folds = -1; 
			Iterator itr = evaluation_method.getOptions().iterator();
			while (itr.hasNext()) {
				org.pikater.core.ontology.messages.option.Option next = (org.pikater.core.ontology.messages.option.Option) itr.next();
				if (next.getName().equals("F")){
					folds = Integer.parseInt( (String)next.getValue() );
				}
			}
			if (folds == -1){
					folds = 5;
				  // TODO read default value from file (if necessary)
			}
			log(folds + "-fold cross validation.", 2);
			eval.crossValidateModel(
					getClassifierClass(),
					test,
					folds, new Random(1));
		}
		else{ // name = Standard
			log("Standard weka evaluation.", 2);
			eval.evaluateModel(getClassifierClass(), test);
		}
				
		log("Error rate: " + eval.errorRate()+" ", 1);
		log(eval.toSummaryString(getLocalName() + " agent: "
				+ "\nResults\n=======\n", false), 2);

		working = false;
		return eval;
	}

	@Override
	protected void evaluateCA(EvaluationMethod evaluation_method,
			org.pikater.core.ontology.messages.Evaluation evaluation) throws Exception{
		
		float default_value = Float.MAX_VALUE;
		Evaluation eval = test(evaluation_method);
		
		List evaluations = new ArrayList();
		Eval ev = new Eval();
		ev.setName("error_rate");
		ev.setValue((float) eval.errorRate());
		evaluations.add(ev);
		
		ev = new Eval();
		ev.setName("kappa_statistic");
		try {
			ev.setValue((float) eval.kappa());
		} catch (Exception e) {
			ev.setValue(default_value);
		}
		evaluations.add(ev);

		ev = new Eval();
		ev.setName("mean_absolute_error");
		try {
			ev.setValue((float) eval.meanAbsoluteError());
		} catch (Exception e) {
			ev.setValue(default_value);
		}
		evaluations.add(ev);

		ev = new Eval();
		ev.setName("relative_absolute_error");
		try {
			ev.setValue((float) eval.relativeAbsoluteError());
		} catch (Exception e) {
			ev.setValue(default_value);
		}
		evaluations.add(ev);
		
		ev = new Eval();
		ev.setName("root_mean_squared_error");
		try {
			ev.setValue((float) eval.rootMeanSquaredError());
		} catch (Exception e) {
			ev.setValue(default_value);
		}
		evaluations.add(ev);

		ev = new Eval();
		ev.setName("root_relative_squared_error");
		try {
			ev.setValue((float) eval.rootRelativeSquaredError());
		} catch (Exception e) {
			ev.setValue(default_value);
		}
		evaluations.add(ev);
		
		
		List evaluations_new = evaluation.getEvaluations();
		
		Iterator itr = evaluations.iterator();
		while (itr.hasNext()) {
			Eval _ev = (Eval) itr.next();
			evaluations_new.add(_ev);
		}
		
		evaluation.setEvaluations(evaluations_new);		
	}

	@Override
	protected DataInstances getPredictions(Instances test,
			DataInstances onto_test) {

		//Evaluation eval = test();
		double pre[] = new double[test.numInstances()];
		for (int i = 0; i < test.numInstances(); i++) {
			try {
				pre[i] = getClassifierClass().classifyInstance(test.instance(i));
			} catch (Exception e) {
				pre[i] = Integer.MAX_VALUE;
			}
		}

		// copy results to the DataInstancs
		int i = 0;
		Iterator itr = onto_test.getInstances().iterator();
		while (itr.hasNext()) {
			Instance next_instance = (Instance) itr.next();
			next_instance.setPrediction(pre[i]);
			i++;
		}

		return onto_test;
	}

}