package xmlGenerator;

import org.pikater.core.agents.system.Agent_GUIKlara;
import org.pikater.core.ontology.subtrees.batchDescription.ComputationDescription;
import org.pikater.core.ontology.subtrees.batchDescription.DataPreProcessing;
import org.pikater.core.ontology.subtrees.batchDescription.DataSourceDescription;
import org.pikater.core.ontology.subtrees.batchDescription.FileDataSaver;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public final class Input10 {

	public static ComputationDescription createDescription() {
		
		// Specify a datasource
		DataSourceDescription fileDataSource1 = new DataSourceDescription(
				"weather.arff");
		DataSourceDescription fileDataSource2 = new DataSourceDescription(
				"weather2.arff");

		// PreProcessing
		DataPreProcessing processing = new DataPreProcessing();
		processing.addDataSources(fileDataSource1);
		processing.addDataSources(fileDataSource2);

		DataSourceDescription dataSourceSunny = new DataSourceDescription();
		dataSourceSunny.setDataOutputType("Data-Sunny");
		dataSourceSunny.setDataProvider(processing);

		// Save Sunny data
		FileDataSaver saverSunny = new FileDataSaver();
		saverSunny.setDataSource(dataSourceSunny);

		DataSourceDescription dataSourceOvercast = new DataSourceDescription();
		dataSourceOvercast.setDataOutputType("Data-Overcast");
		dataSourceOvercast.setDataProvider(processing);

		// Save Overcast data
		FileDataSaver saverOvercast = new FileDataSaver();
		saverOvercast.setDataSource(dataSourceOvercast);

		DataSourceDescription dataSourceRainy = new DataSourceDescription();
		dataSourceRainy.setDataOutputType("Data-Rainy");
		dataSourceRainy.setDataProvider(processing);

		// Save Overcast data
		FileDataSaver saverRainy = new FileDataSaver();
		saverRainy.setDataSource(dataSourceRainy);
		
		// Our requirements for the description are ready, lets create new
		// computation description
		List<FileDataSaver> roots = new ArrayList<FileDataSaver>();
		roots.add(saverSunny);
		roots.add(saverOvercast);
		roots.add(saverRainy);

		ComputationDescription comDescription = new ComputationDescription();
		comDescription.setRootElements(roots);

		return comDescription;
	}
	
	public static void main(String[] args) throws FileNotFoundException {

		System.out
				.println("Exporting Ontology input10 to Klara's input XML configuration file.");

		ComputationDescription comDescription = createDescription();

		String fileName = Agent_GUIKlara.filePath + "input10"
				+ System.getProperty("file.separator") + "input.xml";

		comDescription.exportXML(fileName);
	}
}