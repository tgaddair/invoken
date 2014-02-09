package com.eldritch.scifirpg;

import java.util.ArrayList;
import java.util.Collection;

import com.eldritch.scifirpg.model.EncounterModel;
import com.eldritch.scifirpg.model.encounters.Encounter;
import com.eldritch.scifirpg.model.locations.EncounterLocation;
import com.eldritch.scifirpg.model.locations.Location;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class EncounterActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Location world = new Location("world");
		Collection<Encounter> encounters = new ArrayList<Encounter>();
		EncounterLocation dungeon = EncounterLocation.newBuilder()
        		.setName("dungeon")
        		.setParent(world)
        		.setEncounterModel(new EncounterModel(encounters))
        		.build();
		
		setContentView(R.layout.activity_encounter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.encounter, menu);
		return true;
	}

}
