package com.eldritch.scifirpg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.eldritch.scifirpg.model.Effect;
import com.eldritch.scifirpg.model.EncounterModel;
import com.eldritch.scifirpg.model.Resolution;
import com.eldritch.scifirpg.model.encounters.Encounter;
import com.eldritch.scifirpg.model.encounters.StaticEncounter;
import com.eldritch.scifirpg.model.locations.EncounterLocation;
import com.eldritch.scifirpg.model.locations.Location;
import com.eldritch.scifirpg.view.fragment.StaticEncounterManager;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class EncounterActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Location world = new Location("world");
		List<Encounter> encounters = new ArrayList<Encounter>();
		StaticEncounter encounter = new StaticEncounter("Title", "Description", new Resolution(new ArrayList<Effect>()), 1.0, false);
		encounters.add(encounter);
		EncounterLocation dungeon = EncounterLocation.newBuilder()
        		.setName("dungeon")
        		.setParent(world)
        		.setEncounterModel(new EncounterModel(encounters))
        		.build();
		
		StaticEncounterManager manager = new StaticEncounterManager(encounter, this);
		manager.loadLayout();
		
		//setContentView(R.layout.activity_encounter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.encounter, menu);
		return true;
	}

}
