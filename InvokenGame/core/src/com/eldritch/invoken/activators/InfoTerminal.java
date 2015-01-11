package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;

public class InfoTerminal extends ClickActivator {
	public InfoTerminal(NaturalVector2 position) {
		super(position);
	}

	@Override
	public void activate(Agent agent, Location location) {
	}

	@Override
	public void register(Location location) {
	}
}
