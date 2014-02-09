package com.eldritch.scifirpg.view;

import android.content.Intent;

import com.eldritch.scifirpg.EncounterActivity;
import com.eldritch.scifirpg.OverworldActivity;
import com.eldritch.scifirpg.model.locations.EncounterLocation;

public class EncounterLocationView extends LocationView<EncounterLocation> {

	public EncounterLocationView(EncounterLocation loc, OverworldActivity context) {
		super(loc, context);
	}
	
	@Override
	protected void handleSetLocation() {
		Intent i = new Intent(getOverworld(), EncounterActivity.class);
		getOverworld().startActivity(i);
	}
}
