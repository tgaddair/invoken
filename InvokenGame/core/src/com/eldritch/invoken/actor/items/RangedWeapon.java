package com.eldritch.invoken.actor.items;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.actor.Agent.Direction;
import com.eldritch.invoken.actor.Inventory;

public class RangedWeapon extends Item {
	private static Map<Direction, Animation> animations = new HashMap<Direction, Animation>();
	
	public RangedWeapon(com.eldritch.scifirpg.proto.Items.Item item) {
		super(item, 48);
		animations = Agent.getAnimations("sprite/items/weapons/shotgun.png");
	}
	
	@Override
	public void equipFrom(Inventory inventory) {
		inventory.setWeapon(this);
	}
	
	@Override
	public void unequipFrom(Inventory inventory) {
		if (inventory.getWeapon() == this) {
			inventory.setWeapon(null);
		}
	}
	
	@Override
	protected Animation getAnimation(Activity activity, Direction direction) {
		return animations.get(direction);
	}
}
