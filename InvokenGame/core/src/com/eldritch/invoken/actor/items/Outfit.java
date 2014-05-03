package com.eldritch.invoken.actor.items;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.actor.Agent.Direction;

public class Outfit extends Item {
	private final Map<Activity, Map<Direction, Animation>> animations;
	
	public Outfit(com.eldritch.scifirpg.proto.Items.Item item) {
		super(Agent.PX, Agent.PX);
		animations = Agent.getAllAnimations(item.getAsset());
	}
	
	@Override
	protected Animation getAnimation(Agent agent) {
		return null;
		//return animations.get(agent.getDirection());
	}
}
