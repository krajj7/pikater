package org.pikater.web.vaadin.gui.client.components.kineticcomponent;

import org.pikater.web.vaadin.gui.shared.kineticcomponent.ClickMode;
import org.pikater.web.vaadin.gui.shared.kineticcomponent.visualstyle.KineticBoxSettings;

import com.vaadin.shared.AbstractComponentState;

/** 
 * Server settings and variables shared to the client.
 * 
 * @author SkyCrawl
 */
public class KineticComponentState extends AbstractComponentState {
	private static final long serialVersionUID = 7400546695911691608L;

	/*
	 * Toolbar settings.
	 */
	public ClickMode clickMode;
	public boolean boxManagerBoundWithSelection;
	public boolean box_iconsVisible;
	public double box_scale;

	/*
	 * Other programmatic fields shared. 
	 */
	// public boolean serverThinksThatSchemaIsModified; // currently not used

	public KineticComponentState() {
		this.clickMode = getDefaultClickMode();
		this.boxManagerBoundWithSelection = getBoxManagerBoundWithSelectionByDefault();
		this.box_iconsVisible = getBoxIconsVisibleByDefault();
		this.box_scale = getDefaultScale();

		// this.serverThinksThatSchemaIsModified = false;
	}

	public KineticBoxSettings toBoxVisualSettings() {
		return new KineticBoxSettings(box_iconsVisible, box_scale);
	}

	//----------------------------------------------------
	// DEFAULT VALUE DECLARATION

	public static ClickMode getDefaultClickMode() {
		return ClickMode.SELECTION;
	}

	public static boolean getBoxManagerBoundWithSelectionByDefault() {
		return true;
	}

	public static boolean getBoxIconsVisibleByDefault() {
		return true;
	}

	public static double getDefaultScale() {
		return 1;
	}
}