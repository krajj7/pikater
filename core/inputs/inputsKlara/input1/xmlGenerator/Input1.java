package xmlGenerator;

import jade.util.leap.ArrayList;

import java.io.FileNotFoundException;

import org.pikater.core.agents.experiment.computing.Agent_WekaCA;
import org.pikater.core.agents.system.Agent_GUIKlara;
import org.pikater.core.dataStructures.options.StepanuvOption;
import org.pikater.core.dataStructures.options.types.OptionValue;
import org.pikater.core.ontology.description.ComputationDescription;
import org.pikater.core.ontology.description.ComputingAgent;
import org.pikater.core.ontology.description.DataSourceDescription;
import org.pikater.core.ontology.description.FileDataProvider;
import org.pikater.core.ontology.description.FileDataSaver;


public final class Input1 {

	public static void main(String[] args) throws FileNotFoundException {
		
		System.out.println("Exporting Ontology input1 to Klara's input XML configuration file.");

		
		StepanuvOption optionEM = new StepanuvOption();
		optionEM.setName("evaluation_method");
		optionEM.setOption( new OptionValue(new String("CrossValidation")) );	
		
		StepanuvOption optionSearch = new StepanuvOption();
		optionSearch.setName("search_method");
		optionSearch.setOption( new OptionValue(new String("ChooseXValues")) );	
		
		StepanuvOption optionComMethod = new StepanuvOption();
		optionComMethod.setName("computing_method");
		optionComMethod.setOption( new OptionValue(new String("RBFNetwork")) );
		
		
		StepanuvOption optionOutput = new StepanuvOption();
		optionOutput.setName("output");
		optionOutput.setOption( new OptionValue(new String("evaluation_only")) );	
		
		
		StepanuvOption optionS = new StepanuvOption();
		optionS.setName("S");
		optionS.setOption( new OptionValue(new Integer(1)) );
		
		StepanuvOption optionM = new StepanuvOption();
		optionM.setName("M");
		optionM.setOption( new OptionValue(new Float(0.4)) );	

		ArrayList options = new ArrayList();
		options.add(optionS.toOption());
		options.add(optionM.toOption());
		options.add(optionEM.toOption());
		options.add(optionSearch.toOption());
		options.add(optionComMethod.toOption());
		options.add(optionOutput.toOption());
		
        FileDataProvider fileDataProvider = new FileDataProvider();
        fileDataProvider.setFileURI("weather.arff");

        DataSourceDescription fileDataSource = new DataSourceDescription();
        fileDataSource.setDataProvider(fileDataProvider);

		ComputingAgent comAgent = new ComputingAgent();
		comAgent.setOptions(options);
		comAgent.setTrainingData(fileDataSource);
		comAgent.setTestingData(fileDataSource);
		comAgent.setModelClass(Agent_WekaCA.class.getName());


		DataSourceDescription computingDataSource = new DataSourceDescription();
		computingDataSource.setDataProvider(comAgent);

        FileDataSaver saver = new FileDataSaver();
        saver.setDataSource(computingDataSource);

        ComputationDescription comDescription = new ComputationDescription();
		comDescription.setRootElement(saver);

		


		String fileName = Agent_GUIKlara.filePath + "input1"
				+ System.getProperty("file.separator")
				+ "input1.xml";

		comDescription.exportXML(fileName);


	}

}
