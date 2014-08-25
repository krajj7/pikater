package org.pikater.shared.database.views.tableview.batches.experiments.results;

import java.util.ArrayList;
import java.util.List;

import org.pikater.shared.database.jpa.JPAExperiment;
import org.pikater.shared.database.jpa.JPAResult;
import org.pikater.shared.database.jpa.JPAUser;
import org.pikater.shared.database.jpa.daos.DAOs;
import org.pikater.shared.database.views.base.query.QueryConstraints;
import org.pikater.shared.database.views.base.query.QueryResult;

public class ResultWithModelTableDBView extends ResultTableDBView {

	public ResultWithModelTableDBView(JPAUser user, JPAExperiment experiment){
		super(user, experiment);
	}
	
	@Override
	public QueryResult queryUninitializedRows(QueryConstraints constraints) {
		
		List<JPAResult> results = DAOs.resultDAO.getByExperimentWithModel(this.getExperiment(), constraints.getOffset(), constraints.getMaxResults()); 
		int resultCount=DAOs.resultDAO.getByExperimentWithModelCount(this.getExperiment());
		
		List<ResultTableDBRow> resultRows = new ArrayList<ResultTableDBRow>();
		for(JPAResult result : results)
		{
			resultRows.add(new ResultTableDBRow(result));
		}
		
		return new QueryResult(resultRows, resultCount);
	}
	
}
