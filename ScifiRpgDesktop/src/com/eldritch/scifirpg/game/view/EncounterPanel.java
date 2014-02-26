package com.eldritch.scifirpg.game.view;

import javax.swing.JPanel;

import com.eldritch.scifirpg.proto.Locations.Encounter;

public class EncounterPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final Encounter encounter;

	public EncounterPanel(Encounter encounter) {
		this.encounter = encounter;
	}
}
