package org.pikater.web.vaadin.gui.server.ui_visualization.thumbnail;

import org.pikater.web.sharedresources.ResourceExpiration;
import org.pikater.web.sharedresources.ResourceRegistrar;
import org.pikater.web.sharedresources.download.ImageDownloadResource;
import org.pikater.web.vaadin.gui.server.components.imageviewer.ImageViewer;
import org.pikater.web.vaadin.gui.server.components.popups.MyPopup;
import org.pikater.web.vaadin.gui.server.layouts.matrixlayout.thumbnail.Thumbnail;
import org.pikater.web.visualisation.definition.result.AbstractDSVisSubresult;

import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;

public abstract class ChartThumbnail extends Thumbnail
{
	private static final long serialVersionUID = 8481225003085234704L;
	
	private final String downloadURL;
	private final int imageWidth;
	private final int imageHeight; 
	
	public ChartThumbnail(AbstractDSVisSubresult<?> imageResult, final int imageWidth, final int imageHeight)
	{
		super();
		
		// data source
		this.downloadURL = ResourceRegistrar.getDownloadURL(ResourceRegistrar.registerResource(VaadinSession.getCurrent(), new ImageDownloadResource(
				imageResult.getFile(),
				ResourceExpiration.ON_SESSION_END,
				imageResult.getImageType().toMimeType(),
				imageWidth,
				imageHeight
		)));
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		
		// must be called
		init();
	}

	@Override
	protected Component getContent()
	{
		Image result = new Image(null, new ExternalResource(downloadURL));
		result.setDescription("Double click to enlarge.");
		result.addClickListener(new MouseEvents.ClickListener()
		{
			private static final long serialVersionUID = 5830107000487540250L;

			@Override
			public void click(ClickEvent event)
			{
				if(event.isDoubleClick())
				{
					ImageViewer viewer = new ImageViewer(downloadURL, imageWidth, imageHeight);
					viewer.setSizeFull();

					MyPopup popup = new MyPopup(getContentCaption(), viewer);
					popup.setCaptionCentered();
					popup.setWidth("800px");
					popup.setHeight("800px");
					popup.show();
				}
			}
		});
		return result;
	}
}