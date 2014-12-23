package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;

public class CameraHub extends ClickActivator {
	public CameraHub(NaturalVector2 position) {
		super(position.x, position.y);
	}

	@Override
	public void activate(Agent agent, Location location) {
		System.out.println("activated CameraHub");
	}
}
