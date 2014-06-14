package com.eldritch.invoken.actor.items;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.actor.Agent.Direction;
import com.eldritch.invoken.actor.Human;
import com.eldritch.invoken.actor.Inventory;
import com.google.common.base.Strings;

public class RangedWeapon extends Item {
	private final Map<Direction, Animation> animations = new HashMap<Direction, Animation>();
	
	public RangedWeapon(com.eldritch.scifirpg.proto.Items.Item item) {
		super(item, 48);
		if (!Strings.isNullOrEmpty(item.getAsset())) {
		    animations.putAll(Human.getAnimations(item.getAsset()));
		}
	}
	
	@Override
	public void equipFrom(Inventory inventory) {
		inventory.setRangedWeapon(this);
	}
	
	@Override
	public void unequipFrom(Inventory inventory) {
		if (inventory.getRangedWeapon() == this) {
			inventory.setRangedWeapon(null);
		}
	}
	
	@Override
	protected Animation getAnimation(Activity activity, Direction direction) {
		return animations.get(direction);
	}
}
