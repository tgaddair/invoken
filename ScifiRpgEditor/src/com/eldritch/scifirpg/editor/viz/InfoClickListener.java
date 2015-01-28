package com.eldritch.scifirpg.editor.viz;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import prefuse.controls.ControlAdapter;
import prefuse.controls.Control;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class InfoClickListener extends ControlAdapter implements Control {
	public void itemClicked(VisualItem item, MouseEvent e) {
		if (item instanceof NodeItem) {
			String text = ((String) item.get("text"));

			JPopupMenu jpub = new JPopupMenu();
			jpub.add("Text: " + text);
			jpub.show(e.getComponent(), (int) e.getX(), (int) e.getY());
		}
	}
}
