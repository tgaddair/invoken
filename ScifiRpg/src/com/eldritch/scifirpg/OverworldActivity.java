package com.eldritch.scifirpg;

import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.eldritch.scifirpg.R;
import com.eldritch.scifirpg.model.Encounter;
import com.eldritch.scifirpg.model.EncounterModel;
import com.eldritch.scifirpg.model.GameSettings;
import com.eldritch.scifirpg.model.Occupation;
import com.eldritch.scifirpg.model.Person;
import com.eldritch.scifirpg.model.Person.Gender;
import com.eldritch.scifirpg.model.locations.EncounterLocation;
import com.eldritch.scifirpg.model.locations.Location;
import com.eldritch.scifirpg.view.EncounterLocationView;
import com.eldritch.scifirpg.view.LocationView;

public class OverworldActivity extends Activity {
	private TableLayout tableLayout;
	private Person pc;
	private Location currentLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overworld);
        
        pc = new Person("John", "Holliday", "Doc", Gender.MALE, Occupation.Gunslinger);
        GameSettings.newGame(pc);
        Location world = new Location("world");
        
        setLocation(world);
        
        // TODO(taddair): This should go in the enter() method for 'world'
        TableLayout.LayoutParams rp = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
        TableRow.LayoutParams cp = new TableRow.LayoutParams(0, LayoutParams.MATCH_PARENT);
        
        tableLayout = (TableLayout) findViewById(R.id.mainMap);
        tableLayout.setStretchAllColumns(true);
        tableLayout.setWeightSum(3);
        
        Collection<Encounter> encounters = new ArrayList<Encounter>();
        EncounterLocation dungeon = EncounterLocation.newBuilder()
        		.setName("dungeon")
        		.setParent(world)
        		.setEncounterModel(new EncounterModel(encounters))
        		.build();
        
        TableRow row = new TableRow(this);
        row.setGravity(Gravity.CENTER);
        row.addView(new EncounterLocationView(dungeon, this), cp);
        row.addView(new LocationView(new Location("hills", world), this), cp);
        row.addView(new LocationView(new Location("hills", world), this), cp);
        tableLayout.addView(row, rp);
        
        row = new TableRow(this);
        row.setGravity(Gravity.CENTER);
        row.addView(new LocationView(new Location("hills", world), this), cp);
        row.addView(new LocationView(new Location("town", world), this), cp);
        row.addView(new LocationView(new Location("creek", world), this), cp);
        tableLayout.addView(row, rp);
        
        row = new TableRow(this);
        row.setGravity(Gravity.CENTER);
        row.addView(new LocationView(new Location("hills", world), this), cp);
        row.addView(new LocationView(new Location("hills", world), this), cp);
        row.addView(new LocationView(new Location("hills", world), this), cp);
        tableLayout.addView(row, rp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_overworld, menu);
        return true;
    }
    
    public void setLocation(Location location) {
    	currentLocation = location;
    	location.enter();
    	
    	String text = String.format("Moved to %s", location.getName());
    	Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
