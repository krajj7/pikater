package org.pikater.web.vaadin.gui.server.components.dbviews;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

import org.pikater.shared.database.views.tableview.base.AbstractTableRowDBView;
import org.pikater.shared.database.views.tableview.base.ITableColumn;
import org.pikater.shared.database.views.tableview.batches.experiments.ExperimentTableDBRow;
import org.pikater.shared.database.views.tableview.batches.experiments.ExperimentTableDBView;
import org.pikater.shared.quartz.jobs.InterruptibleJobHelper;
import org.pikater.shared.util.IOUtils;
import org.pikater.web.HttpContentType;
import org.pikater.web.quartzjobs.results.ExportExperimentResultsJob;
import org.pikater.web.sharedresources.ResourceExpiration;
import org.pikater.web.sharedresources.ResourceRegistrar;
import org.pikater.web.sharedresources.download.IDownloadResource;
import org.pikater.web.vaadin.gui.server.components.dbviews.base.AbstractDBViewRoot;
import org.pikater.web.vaadin.gui.server.components.popups.dialogs.ProgressDialog;
import org.pikater.web.vaadin.gui.server.components.popups.dialogs.ProgressDialog.IProgressDialogResultHandler;
import org.pikater.web.vaadin.gui.server.components.popups.dialogs.ProgressDialog.IProgressDialogTaskResult;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbstractComponent;

public class ExperimentDBViewRoot extends AbstractDBViewRoot<ExperimentTableDBView>
{
	public ExperimentDBViewRoot(ExperimentTableDBView view)
	{
		super(view);
	}

	@Override
	public int getColumnSize(ITableColumn column)
	{
		ExperimentTableDBView.Column specificColumn = (ExperimentTableDBView.Column) column;
		switch(specificColumn)
		{
			case STATUS:
				return 100;
			
			case STARTED:
			case FINISHED:
				return 75;
			
			case MODEL_STRATEGY:
			case BEST_MODEL:
			case RESULTS:
				return 100;
			
			default:
				throw new IllegalStateException("Unknown state: " + specificColumn.name());
		}
	}
	
	@Override
	public ITableColumn getExpandColumn()
	{
		return null;
	}
	
	@Override
	public void onCellCreate(ITableColumn column, AbstractComponent component)
	{
	}

	@Override
	public void approveAction(ITableColumn column, AbstractTableRowDBView row, Runnable action)
	{
		final ExperimentTableDBRow specificRow = (ExperimentTableDBRow) row;
		
		ExperimentTableDBView.Column specificColumn = (ExperimentTableDBView.Column) column;
		if(specificColumn == ExperimentTableDBView.Column.BEST_MODEL)
		{
			// TODO: talk with Peter about this
		}
		else if(specificColumn == ExperimentTableDBView.Column.RESULTS)
		{
			final File tmpFile = IOUtils.createTemporaryFile("results", ".csv");
			
			// download, don't run action
			ProgressDialog.show("Export progress...", new ProgressDialog.IProgressDialogTaskHandler()
			{
				private InterruptibleJobHelper underlyingTask;
				
				@Override
				public void startTask(IProgressDialogResultHandler contextForTask) throws Throwable
				{
					// start the task and bind it with the progress dialog
					underlyingTask = new InterruptibleJobHelper();
					underlyingTask.startJob(ExportExperimentResultsJob.class, new Object[]
					{
						specificRow.getExperiment(),
						tmpFile,
						contextForTask
					});
				}
				
				@Override
				public void abortTask()
				{
					underlyingTask.abort();
				}
				
				@Override
				public void onTaskFinish(IProgressDialogTaskResult result)
				{
					UUID resultsDownloadResourceUI = ResourceRegistrar.registerResource(VaadinSession.getCurrent(), new IDownloadResource()
					{
						@Override
						public ResourceExpiration getLifeSpan()
						{
							return ResourceExpiration.ON_FIRST_PICKUP;
						}

						@Override
						public InputStream getStream() throws Throwable
						{
							return new FileInputStream(tmpFile);
						}

						@Override
						public long getSize()
						{
							return tmpFile.length();
						}

						@Override
						public String getMimeType()
						{
							return HttpContentType.TEXT_CSV.toString();
						}

						@Override
						public String getFilename()
						{
							return tmpFile.getName();
						}
					});
					Page.getCurrent().setLocation(ResourceRegistrar.getDownloadURL(resultsDownloadResourceUI));
				}
			});
		}
		else
		{
			throw new IllegalStateException(String.format("Action '%s' has to be approved before being executed", specificColumn.name())); 
		}
	}
}