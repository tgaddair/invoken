package com.eldritch.invoken.actor.items;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Direction;

public class RangedWeapon extends Item {
	private static Map<Direction, Animation> animations = new HashMap<Direction, Animation>();
	
	public RangedWeapon(com.eldritch.scifirpg.proto.Items.Item item) {
		super(1 / 32f * 48, 1 / 32f * 48);
		animations = Agent.getAnimations("sprite/weapons/shotgun.png");
	}
	
	@Override
	protected Animation getAnimation(Agent agent) {
		return animations.get(agent.getDirection());
	}
}
