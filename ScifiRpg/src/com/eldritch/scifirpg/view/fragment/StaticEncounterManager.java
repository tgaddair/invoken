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
	public View getLayout() {
        RelativeLayout layout = (RelativeLayout) getActivity().findViewById(R.layout.static_encounter);
        
        TextView titleView = (TextView) layout.findViewById(R.id.titleView);
        titleView.setText(getEncounter().getTitle());
        
        TextView descriptionView = (TextView) layout.findViewById(R.id.descriptionView);
        descriptionView.setText(getEncounter().getDescription());
        
        return layout;
	}
}
