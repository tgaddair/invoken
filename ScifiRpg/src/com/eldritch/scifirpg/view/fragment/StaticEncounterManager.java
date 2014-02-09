package com.eldritch.scifirpg.view.fragment;

import com.eldritch.scifirpg.EncounterActivity;
import com.eldritch.scifirpg.R;
import com.eldritch.scifirpg.model.encounters.StaticEncounter;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StaticEncounterManager extends EncounterLayoutManager<StaticEncounter> {
	public StaticEncounterManager(StaticEncounter encounter, EncounterActivity activity) {
		super(encounter, activity);
	}
	
	@Override
	public void loadLayout() {
		getActivity().setContentView(R.layout.static_encounter);
		
        TextView titleView = (TextView) getActivity().findViewById(R.id.titleView);
        titleView.setText(getEncounter().getTitle());
        
        TextView descriptionView = (TextView) getActivity().findViewById(R.id.descriptionView);
        descriptionView.setText(getEncounter().getDescription());
	}
}
