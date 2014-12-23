package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;

public class CameraHub extends ClickActivator {
	private Location location = null;
	
	public CameraHub(NaturalVector2 position) {
		super(position);
	}

	@Override
	public void activate(Agent agent, Location location) {
		if (location.hasSecurityCamera()) {
			agent.setCamera(location.getFirstSecurityCamera());
		}
	}

	@Override
	public void register(Location location) {
		this.location = location;
	}
}
