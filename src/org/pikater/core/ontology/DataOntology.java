package org.pikater.core.ontology;


import org.pikater.core.ontology.subtrees.datainstance.DataInstances;
import org.pikater.core.ontology.subtrees.dataset.DatasetInfo;
import org.pikater.core.ontology.subtrees.file.GetFile;
import org.pikater.core.ontology.subtrees.data.GetData;
import org.pikater.core.ontology.subtrees.externalagent.GetExternalAgentJar;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class DataOntology extends BeanOntology {

	private static final long serialVersionUID = -2595494753081736728L;

	private DataOntology() {
		super("DataOntology");

		String getFilePackage = GetFile.class.getPackage().getName();
		String getDataPackage = GetData.class.getPackage().getName();
		String getDatasetInfoPackage = DatasetInfo.class.getPackage().getName();
		try {
			add(getFilePackage);
			add(getDatasetInfoPackage);
			add(GetExternalAgentJar.class);
			add(DataInstances.class);
			
			add(getDataPackage);
			
		} catch (BeanOntologyException e) {
			e.printStackTrace();
		}
	}

	static DataOntology theInstance = new DataOntology();

	public static Ontology getInstance() {
		return theInstance;
	}
}
