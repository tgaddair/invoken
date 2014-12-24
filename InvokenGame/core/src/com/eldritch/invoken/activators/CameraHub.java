package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.aug.Crack;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;

public class CameraHub extends ClickActivator {
	public CameraHub(NaturalVector2 position) {
		super(position);
	}

	@Override
	public void activate(Agent agent, Location location) {
		if (agent.isToggled(Crack.class) && location.hasSecurityCamera()) {
			agent.setCamera(location.getFirstSecurityCamera());
		}
	}

	@Override
	public void register(Location location) {
	}
}
