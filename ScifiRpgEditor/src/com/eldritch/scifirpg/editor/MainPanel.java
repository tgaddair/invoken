package com.eldritch.scifirpg.editor;

import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JComponent;

import com.eldritch.scifirpg.editor.tables.ActorTable;
import com.eldritch.scifirpg.editor.tables.AugmentationTable;
import com.eldritch.scifirpg.editor.tables.EncounterTable;
import com.eldritch.scifirpg.editor.tables.ItemTable;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

public class MainPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public MainPanel() {
		super(new GridLayout(1, 1));

		JTabbedPane tabbedPane = new JTabbedPane();
		ImageIcon icon = null;

		JComponent panel1 = new AssetTablePanel(new ActorTable());
		tabbedPane.addTab("Actors", icon, panel1, "Edit and create actors");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		JComponent panel2 = new AssetTablePanel(new EncounterTable());
		tabbedPane.addTab("Encounters", icon, panel2, "Edit and create encounters");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		JComponent panel3 = new AssetTablePanel(new ItemTable());
		tabbedPane.addTab("Items", icon, panel3, "Edit and create items");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

		JComponent panel4 = new AssetTablePanel(new AugmentationTable());
		tabbedPane.addTab("Augmentations", icon, panel4, "Edit and create augmentations");
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

		// Add the tabbed pane to this panel.
		add(tabbedPane);

		// The following line enables to use scrolling tabs.
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		setPreferredSize(new Dimension(1200, 800));
	}
}
