package org.pikater.shared.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pikater.core.ontology.subtrees.experiment.Experiment;
import org.pikater.core.ontology.subtrees.model.Model;
import org.pikater.shared.database.exceptions.NoResultException;
import org.pikater.shared.database.exceptions.UserNotFoundException;
import org.pikater.shared.database.jpa.JPAAgentInfo;
import org.pikater.shared.database.jpa.JPABatch;
import org.pikater.shared.database.jpa.JPADataSetLO;
import org.pikater.shared.database.jpa.JPAExperiment;
import org.pikater.shared.database.jpa.JPAExternalAgent;
import org.pikater.shared.database.jpa.JPAFilemapping;
import org.pikater.shared.database.jpa.JPAResult;
import org.pikater.shared.database.jpa.JPARole;
import org.pikater.shared.database.jpa.JPAUser;
import org.pikater.shared.database.jpa.PikaterPriviledge;
import org.pikater.shared.database.jpa.daos.AbstractDAO.EmptyResultAction;
import org.pikater.shared.database.jpa.daos.DAOs;
import org.pikater.shared.database.jpa.status.JPABatchStatus;
import org.pikater.shared.database.jpa.status.JPAExperimentStatus;
import org.pikater.shared.database.postgre.MyPGConnection;
import org.pikater.shared.database.util.ResultExporter;
import org.pikater.shared.database.util.ResultFormatter;
import org.pikater.shared.database.views.base.query.SortOrder;
import org.pikater.shared.database.views.tableview.batches.BatchTableDBView;
import org.pikater.shared.database.views.tableview.datasets.DataSetTableDBView;
import org.pikater.shared.database.views.tableview.externalagents.ExternalAgentTableDBView;
import org.pikater.shared.database.views.tableview.users.UsersTableDBView;

public class DatabaseTest {

	public void test() {
		testDBConnection();
		testLastBatchWithExperiments();
		//testDatasetRetrievalBasedOnResult();
		//testExperimentsWithModels();
		//testResultsWithModelsAndAgentNames();
		//testExternalAgentView();
		//testBatchResultRetrieval();
		//testDatasetViewFunctions();
		//listBestResult();
		//listUserUploadedDatasets();
		//testModelRemoval(); //removes very recent (older than 1 day) models!!!
		//exportResults();
		//listVisibleAndApprovedDatasets();
		//removeResult();
		//listDataSetsWithResults();
		//listDataSets();
		//addExperiment();
		//listExternalAgents();
		//listDataSetWithExclusion();
		//listResults();
		//listUserAndRoles();
		//listBatches();
		//listExperiments();
		//listFileMappings();
		//listAgentInfos();
	}

	private void testLastBatchWithExperiments() {
		JPABatch batch = DAOs.batchDAO.getByID(134601);
		p(batch.getName() + " : " + batch.getStatus());
		for (JPAExperiment exp : batch.getExperiments()) {
			p(exp.getId() + " . " + exp.getStatus());
		}

	}

	protected void testDatasetRetrievalBasedOnResult() {

		JPAResult result = DAOs.resultDAO.getByID(122205);
		p("---- Result ID " + result.getId() + " ----");
		List<JPADataSetLO> outputs = result.getOutputs();
		p("------- No. of outputs " + outputs.size() + " --------");
		for (JPADataSetLO dslo : outputs) {
			p(dslo.getId() + ". " + dslo.getHash());
		}

	}

	protected void testResultsWithModelsAndAgentNames() {
		String agentName = "org.pikater.core.agents.experiment.computing.Agent_WekaRBFNetworkCA";
		JPABatch batch = DAOs.batchDAO.getByID(119752);
		List<JPAExperiment> experiments = DAOs.experimentDAO.getByBatchWithModel(batch, 0, 100);
		JPAExperiment experiment = experiments.get(experiments.size() - 1);

		List<JPAResult> results = DAOs.resultDAO.getByExperimentAndAgentNameWithModel(experiment, agentName, 0, 100);
		int resultCount = DAOs.resultDAO.getByExperimentAndAgentNameWithModelCount(experiment, agentName);

		p("----- List Size :   " + results.size() + "  ----");
		p("----- Result Count: " + resultCount + "  ----");
		for (JPAResult result : results) {
			p(result.getId() + ". " + result.getAgentName() + " " + result.getFinish() + " " + result.getErrorRate());
		}

	}

	protected void testExperimentsWithModels() {
		List<JPABatch> batches = DAOs.batchDAO.getAll();

		JPABatch batch = batches.get(batches.size() - 1);

		List<JPAExperiment> experiments = DAOs.experimentDAO.getByBatchWithModel(batch, 0, 100);
		p("----------------- Batch ID " + batch.getId() + " -------------");
		for (JPAExperiment experiment : experiments) {
			p(experiment.getId() + ". " + experiment.getStatus());
			p("    -----------  Experiment ID " + experiment.getId() + " ( " + experiment.getResults().size() + " results )--------");
			List<JPAResult> resultsWithModels = DAOs.resultDAO.getByExperimentWithModel(experiment, 0, 100);
			p("    -- No. of results with models : " + resultsWithModels.size());
		}
	}

	protected void testExternalAgentView() {
		List<JPAExternalAgent> agents;
		int allAgentsCount = 0;

		agents = DAOs.externalAgentDAO.getByVisibility(0, 100, ExternalAgentTableDBView.Column.AGENT_CLASS, SortOrder.ASCENDING, true);
		allAgentsCount = DAOs.externalAgentDAO.getByVisibilityCount(true);
		p("-------   Query Count = " + allAgentsCount + "   ------");
		p("-------    List Count = " + agents.size() + "   ------");
		for (JPAExternalAgent ea : agents) {
			p(ea.getId() + ". " + ea.getAgentClass() + " : " + ea.getDescription());
		}
		p("");
		JPAUser owner = DAOs.userDAO.getByLogin("sj").get(0);
		p("----------  For User " + owner.getLogin() + " ------------");
		agents = DAOs.externalAgentDAO.getByOwnerAndVisibility(owner, 0, 100, ExternalAgentTableDBView.Column.AGENT_CLASS, SortOrder.ASCENDING, true);
		allAgentsCount = DAOs.externalAgentDAO.getByOwnerAndVisibilityCount(owner, true);
		p("-------   Query Count = " + allAgentsCount + "   ------");
		p("-------    List Count = " + agents.size() + "   ------");
		for (JPAExternalAgent ea : agents) {
			p(ea.getId() + ". " + ea.getAgentClass() + " : " + ea.getDescription());
		}
	}

	protected void testBatchResultRetrieval() {
		JPABatch batch = DAOs.batchDAO.getAll().get(0);

		p("-------   ID = " + batch.getId() + "   ------");

		List<JPAResult> batchResultList = DAOs.batchDAO.getByIDwithResults(batch.getId(), 0, 100);
		int resultCount = DAOs.batchDAO.getBatchResultCount(batch);

		p("-------   Query Count = " + resultCount + "   ------");
		p("-------    List Count = " + batchResultList.size() + "   ------");

		for (JPAResult result : batchResultList) {
			p(result.getAgentName() + "  " + result.getErrorRate());
		}
		p("------------------------------------");

	}

	protected void testDatasetViewFunctions() {
		List<JPADataSetLO> allDatasets;
		int allDatasetCount = 0;

		allDatasets = DAOs.dataSetDAO.getUserUploadVisible(0, 100, DataSetTableDBView.Column.CREATED, SortOrder.DESCENDING);
		allDatasetCount = DAOs.dataSetDAO.getUserUploadVisibleCount();

		p("-----  Query count : " + allDatasetCount + "  --------");
		p("-----  List  count : " + allDatasets.size() + "  --------");

		JPAUser owner = DAOs.userDAO.getByLogin("sp").get(0);

		allDatasets = DAOs.dataSetDAO.getByOwnerUserUploadVisible(owner, 0, 100, DataSetTableDBView.Column.CREATED, SortOrder.DESCENDING);
		allDatasetCount = DAOs.dataSetDAO.getByOwnerUserUploadVisibleCount(owner);

		p("-----  Query count : " + allDatasetCount + "  --------");
		p("-----  List  count : " + allDatasets.size() + "  --------");

	}

	protected void listBestResult() {
		JPAExperiment exp = DAOs.experimentDAO.getByID(116652, EmptyResultAction.NULL);
		JPAResult result = DAOs.resultDAO.getByExperimentBestResult(exp);
		p("Best result for experiment ID " + exp.getId());
		if (result == null) {
			p("no results yet");
		} else {
			p(result.getErrorRate() + " " + result.getCreatedModel());
		}
		p("----------------");

	}

	protected void listUserUploadedDatasets() {
		List<JPADataSetLO> dslos = DAOs.dataSetDAO.getAllUserUploaded();
		p("No. of found DataSets: " + dslos.size());
		for (JPADataSetLO dslo : dslos) {
			p(dslo.getId() + ". " + dslo.getDescription() + "    " + dslo.getCreated() + "   DT:" + (dslo.getGlobalMetaData() != null ? dslo.getGlobalMetaData().getNumberofInstances() : "no_gmd"));
		}
		p("------------");
		p("");

	}

	protected void testModelRemoval() {
		DAOs.modelDAO.removeOldModels((short) 1);
	}

	protected void testDBConnection() {
		System.out.println("Database connected: " + MyPGConnection.isConnectionToCurrentPGDBEstablished());
	}

	protected void exportResults() {
		//JPABatch batch=DAOs.batchDAO.getByID(87801, EmptyResultAction.NULL);
		ResultExporter exp = new ResultExporter(System.err);
		exp.export(117301);
		exp.flush();
	}

	protected void listVisibleAndApprovedDatasets() {
		List<JPADataSetLO> dslos = DAOs.dataSetDAO.getUserUploadVisible(0, 5, DataSetTableDBView.Column.APPROVED, SortOrder.DESCENDING);
		p("No. of found visible DataSets: " + dslos.size());
		p("No. of all visible DataSets: " + DAOs.dataSetDAO.getUserUploadVisibleCount());
		for (JPADataSetLO dslo : dslos) {
			p(dslo.getId() + ". " + dslo.getHash() + "    " + dslo.getCreated() + "   DT:" + dslo.getGlobalMetaData().getNumberofInstances());
		}
		p("------------");
		p("");

		dslos = DAOs.dataSetDAO.getByOwnerUserUploadVisible(DAOs.userDAO.getByLogin("sp").get(0), 0, 5, DataSetTableDBView.Column.APPROVED, SortOrder.DESCENDING);
		p("No. of found visible DataSets for user sp: " + dslos.size());
		for (JPADataSetLO dslo : dslos) {
			p(dslo.getId() + ". " + dslo.getHash() + "    " + dslo.getCreated() + "   DT:" + dslo.getGlobalMetaData().getNumberofInstances());
		}
		p("------------");
		p("");

	}

	protected void listDataSetsWithResults() {
		//List<JPADataSetLO> dslos= DAOs.dataSetDAO.getAllWithResults();
		List<String> exlist = new ArrayList<String>();
		exlist.add("28c7b9febbecff6ce207bcde29fc0eb8");
		List<JPADataSetLO> dslos = DAOs.dataSetDAO.getAllWithResultsExcludingHashesWithMetadata(exlist);
		p("No. of found DataSets: " + dslos.size());
		for (JPADataSetLO dslo : dslos) {
			p(dslo.getId() + ". " + dslo.getHash() + "    " + dslo.getCreated() + "   DT:" + dslo.getGlobalMetaData().getNumberofInstances());
		}
		p("------------");
		p("");

	}

	public void removeResult() {
		int resultId = 76453;
		System.out.println("Deleting result no.: " + resultId);
		//option1 : DAOs.resultDAO.deleteResultEntityByID(resultId);

		//option2:
		JPAResult res1 = DAOs.resultDAO.getByID(resultId);
		DAOs.resultDAO.deleteEntity(res1);
	}

	public void addExperiment() {
		JPAUser user = DAOs.userDAO.getByID(5856, EmptyResultAction.NULL);
		JPABatch jpaBatch = new JPABatch("test_batch_1", "For test purposes", "<?xml version=\"1.0\"?><batch><nope/></batch>", user);

		DAOs.batchDAO.storeEntity(jpaBatch);

		Experiment exp = new Experiment();
		exp.setBatchID(jpaBatch.getId());//62901);
		exp.setStatus(JPAExperimentStatus.FAILED.name());

		int id1 = DAOs.batchDAO.addExperimentToBatch(exp);
		System.out.println("Saved experiment with ID: " + id1);

		int modelID = 12345678;
		Experiment exp2 = new Experiment();
		exp2.setBatchID(jpaBatch.getId());//62901);
		exp2.setStatus(JPAExperimentStatus.COMPUTING.name());
		exp2.setModel(modelID);

		int id2 = DAOs.batchDAO.addExperimentToBatch(exp2);
		System.out.println("Saved experiment with ID: " + id2);

		int experimentID = id1;//63152;
		JPAResult jparesult = new JPAResult();
		jparesult.setAgentName("DBtest");
		jparesult.setNote("Example result from DB test");
		jparesult.setStart(new Date());
		jparesult.setFinish(new Date());

		int resultID = DAOs.experimentDAO.addResultToExperiment(experimentID, jparesult);
		System.out.println("Persisted JPAResult for experiment ID " + experimentID + " with ID: " + resultID);

		Model model = new Model(resultID, "java.lang.Object", new byte[1]);
		modelID = DAOs.resultDAO.setModelForResult(model);
		System.out.println("Saved model with ID: " + modelID);
	}

	public void listDataSets() {
		List<JPADataSetLO> dslos = DAOs.dataSetDAO.getAllUserUpload(0, 5, DataSetTableDBView.Column.APPROVED, SortOrder.ASCENDING);
		p("No. of found DataSets: " + dslos.size());
		for (JPADataSetLO dslo : dslos) {
			p(dslo.getId() + ". from " + dslo.getSource().name() + " ext: " + DAOs.filemappingDAO.getSingleExternalFilename(dslo) + " int: " + dslo.getHash() + "    " + dslo.getCreated() + "   DT:"
					+ (dslo.getGlobalMetaData() != null ? dslo.getGlobalMetaData().getNumberofInstances() : -1));
		}
		p("------------");
		p("");
	}

	public void listExternalAgents() {
		List<JPAExternalAgent> agents = DAOs.externalAgentDAO.getAll();//0,5,ExternalAgentTableDBView.Column.AGENT_CLASS,SortOrder.DESCENDING);
		p("No. of found External Agents: " + agents.size());
		for (JPAExternalAgent ag : agents) {
			p(ag.getId() + ". " + ag.getAgentClass() + "    " + ag.getOwner().getLogin() + "  " + ag.getCreated() + "   " + ag.getDescription() + " ");
		}
		p("------------");
		p("");
	}

	public void listDataSetWithExclusion() {
		List<String> exList = new ArrayList<String>();
		List<JPADataSetLO> wDslos = DAOs.dataSetDAO.getByDescription("weather.arff");
		//Example for ResultFormatter
		ResultFormatter<JPADataSetLO> dsloFormatter = new ResultFormatter<JPADataSetLO>(wDslos);
		JPADataSetLO wdslo;
		try {
			wdslo = dsloFormatter.getSingleResult();
			exList.add(wdslo.getHash());
		} catch (NoResultException e) {
			System.err.println("weather.arff doesn't exist in the database: " + e.getMessage());
		}

		List<JPADataSetLO> iDslos = DAOs.dataSetDAO.getByDescription("iris.arff");
		if (!iDslos.isEmpty()) {
			JPADataSetLO idslo = iDslos.get(0);
			exList.add(idslo.getHash());
		}
		List<JPADataSetLO> dslos = DAOs.dataSetDAO.getAllExcludingHashesWithMetadata(exList);

		p("No. of found DataSets: " + dslos.size());
		System.out.print("Excluded: ");
		for (String s : exList) {
			System.out.print(s + " ");
		}
		System.out.println();
		for (JPADataSetLO dslo : dslos) {
			p(dslo.getId() + ". " + dslo.getHash() + "    " + dslo.getCreated());
		}
		p("------------");
		p("");
	}

	public void listResults() {
		List<JPAResult> results = DAOs.resultDAO.getAll();
		for (JPAResult res : results) {
			p(res.getId() + ". " + res.getAgentName() + "    " + res.getStart());
		}
		p("------------");
		p("");
	}

	public void listUserAndRoles() {
		List<JPARole> roles = DAOs.roleDAO.getAll();
		p("No. of Roles in the system : " + roles.size());
		for (JPARole r : roles) {
			p(r.getId() + ". " + r.getName() + " : " + r.getRole().getDescription());
		}
		p("---------------------");
		p("");

		List<JPAUser> users = DAOs.userDAO.getAll(0, 5, UsersTableDBView.Column.RESET_PSWD, SortOrder.ASCENDING);
		p("No. of Users in the system : " + users.size());
		for (JPAUser r : users) {
			p(r.getId() + ". " + r.getLogin() + " : " + r.getStatus() + " - " + r.getEmail() + "   " + r.getCreated().toString() + " : SB = " + r.hasPrivilege(PikaterPriviledge.SAVE_BOX)
					+ " : SDS = " + r.hasPrivilege(PikaterPriviledge.SAVE_DATA_SET));
		}
		p("---------------------");
		p("");
	}

	public void listBatches() {
		List<JPABatch> batches;//=DAOs.batchDAO.getAll();

		//batches=DAOs.batchDAO.getAll(0, 10,AbstractBatchTableDBView.Column.NAME);
		JPAUser user = new ResultFormatter<JPAUser>(DAOs.userDAO.getByLogin("klara")).getSingleResult();
		//batches=DAOs.batchDAO.getByOwner(user, 0, 10,AbstractBatchTableDBView.Column.CREATED,SortOrder.ASCENDING);
		batches = DAOs.batchDAO.getByOwnerAndStatus(user, JPABatchStatus.CREATED, 0, 10, BatchTableDBView.Column.CREATED, SortOrder.ASCENDING);
		p("No. of Batches in the system : " + DAOs.batchDAO.getByOwnerAndStatusCount(user, JPABatchStatus.CREATED));
		for (JPABatch b : batches) {
			p(b.getOwner().getLogin() + ":" + b.getId() + ". " + b.getName() + " : " + b.getCreated() + " - " + b.getFinished());
		}
		p("---------------------");
		p("");
	}

	public void listExperiments() {
		List<JPAExperiment> exps = DAOs.experimentDAO.getAll();
		p("No. of Experiments " + exps.size());
		for (JPAExperiment exp : exps) {
			p(exp.getId() + ". " + exp.getStatus() + " : " + exp.getStarted() + " - " + exp.getFinished());
		}

		p("---------------------");
		p("");
	}

	public void listFileMappings() {
		List<JPAFilemapping> fms = DAOs.filemappingDAO.getAll();
		p("No. of FileMappings " + fms.size());
		for (JPAFilemapping fm : fms) {
			p(fm.getId() + ". " + fm.getInternalfilename() + " - " + fm.getExternalfilename());
		}

		p("---------------------");
		p("");
	}

	public void listAgentInfos() {
		JPAUser user = DAOs.userDAO.getByID(5859, EmptyResultAction.NULL);
		List<JPAAgentInfo> ais = DAOs.agentInfoDAO.getByExternalAgentOwner(user);
		p("No. of AgentInfos " + ais.size());
		for (JPAAgentInfo ai : ais) {
			p(ai.getId() + ". " + ai.getAgentClass() + " '" + ai.getCreationTime() + "' - " + ai.getName() + " : " + ai.getDescription());
		}

		p("---------------------");
		p("");
	}

	private void p(String s) {
		System.out.println(s);
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, UserNotFoundException {
		DatabaseTest dt = new DatabaseTest();
		dt.test();
		System.out.println("End of Database Testing");
	}

}
