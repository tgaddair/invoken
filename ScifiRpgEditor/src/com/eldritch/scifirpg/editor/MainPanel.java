package com.eldritch.scifirpg.editor;

import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JComponent;

import com.eldritch.scifirpg.editor.tables.ActorTable;
import com.eldritch.scifirpg.editor.tables.AssetTable;
import com.eldritch.scifirpg.editor.tables.AugmentationTable;
import com.eldritch.scifirpg.editor.tables.ContainerTable;
import com.eldritch.scifirpg.editor.tables.EncounterTable;
import com.eldritch.scifirpg.editor.tables.FactionTable;
import com.eldritch.scifirpg.editor.tables.ItemTable;
import com.eldritch.scifirpg.editor.tables.LocationTable;
import com.eldritch.scifirpg.editor.tables.MissionTable;
import com.eldritch.scifirpg.editor.tables.RoomTable;
import com.eldritch.scifirpg.editor.tables.TerminalTable;

import java.awt.Dimension;
import java.awt.GridLayout;

public class MainPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public static final ActorTable ACTOR_TABLE = new ActorTable();
	
	public static final FactionTable FACTION_TABLE = new FactionTable();
	
	public static final LocationTable LOCATION_TABLE = new LocationTable();
	
	public static final EncounterTable ENCOUNTER_TABLE = new EncounterTable();
	
	public static final RoomTable ROOM_TABLE = new RoomTable();
	
	public static final ContainerTable CONTAINER_TABLE = new ContainerTable();
	
	public static final TerminalTable TERMINAL_TABLE = new TerminalTable();
	
	public static final ItemTable ITEM_TABLE = new ItemTable();
	
	public static final AugmentationTable AUGMENTATION_TABLE = new AugmentationTable();
	
	public static final MissionTable MISSION_TABLE = new MissionTable();

	public MainPanel() {
		super(new GridLayout(1, 1));

		JTabbedPane tabbedPane = new JTabbedPane();
		addTable(ACTOR_TABLE, tabbedPane);
		addTable(FACTION_TABLE, tabbedPane);
		addTable(LOCATION_TABLE, tabbedPane);
		addTable(ENCOUNTER_TABLE, tabbedPane);
		addTable(ROOM_TABLE, tabbedPane);
		addTable(CONTAINER_TABLE, tabbedPane);
		addTable(TERMINAL_TABLE, tabbedPane);
		addTable(ITEM_TABLE, tabbedPane);
		addTable(AUGMENTATION_TABLE, tabbedPane);
		addTable(MISSION_TABLE, tabbedPane);

		// Add the tabbed pane to this panel.
		add(tabbedPane);

		// The following line enables to use scrolling tabs.
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		setPreferredSize(new Dimension(1200, 800));
	}
	
	private void addTable(AssetTable<?> table, JTabbedPane tabbedPane) {
		JComponent panel = new AssetTablePanel(table);
		ImageIcon icon = null;
		String assetName = table.getAssetName() + "s";
		tabbedPane.addTab(assetName, icon, panel, "Edit and create " + assetName.toLowerCase());
	}
}
