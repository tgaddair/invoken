package com.eldritch.scifirpg.view;

import com.eldritch.scifirpg.OverworldActivity;
import com.eldritch.scifirpg.model.locations.Location;

import android.view.View;
import android.widget.Button;

public class LocationView extends Button {
	private final OverworldActivity overworld;
	private final Location location;

	public LocationView(Location loc, OverworldActivity context) {
		super(context);
		this.overworld = context;
		this.location = loc;
		
		setText(location.getShortDescription());
		setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	overworld.setLocation(location);
            }
        });
	}

}
